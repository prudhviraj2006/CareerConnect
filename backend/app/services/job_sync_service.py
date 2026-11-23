import logging
from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional
import httpx
from datetime import datetime, timezone
from sqlalchemy.orm import Session
from bs4 import BeautifulSoup
import re

from backend.app.config import settings
from backend.app.models.all_models import Job, JobSource, JobRequiredSkill, utcnow
from backend.app.services.resume_parser import TECHNICAL_SKILLS_CATALOG, normalize_skill, INDIAN_LOCATIONS

logger = logging.getLogger(__name__)

def clean_html(raw_html: str) -> str:
    """Strip HTML tags and convert to clean readable text."""
    if not raw_html:
        return ""
    # Use simple regex and unescape if bs4 is heavy, or regex
    cleanr = re.compile('<.*?>')
    cleaned = re.sub(cleanr, ' ', raw_html)
    cleaned = re.sub(r'\s+', ' ', cleaned).strip()
    return cleaned

def extract_skills_from_text_and_tags(text: str, tags: List[str]) -> List[str]:
    """Identify genuine technical skills from tags and job description."""
    skills_found = set()
    # 1. From tags
    for tag in tags:
        tag_norm = normalize_skill(tag)
        if tag_norm in TECHNICAL_SKILLS_CATALOG:
            skills_found.add(tag_norm)
        elif len(tag.strip()) > 1:
            # Check if part of catalog
            for cat_skill in TECHNICAL_SKILLS_CATALOG:
                if cat_skill == tag_norm or cat_skill in tag_norm:
                    skills_found.add(cat_skill)

    # 2. From text body
    text_lower = text.lower()
    for cat_skill in TECHNICAL_SKILLS_CATALOG:
        escaped = re.escape(cat_skill)
        pattern = rf"(?<![a-zA-Z0-9]){escaped}(?![a-zA-Z0-9])"
        if re.search(pattern, text_lower):
            skills_found.add(cat_skill)

    return sorted(list(skills_found))

INDIAN_CITY_KEYWORDS = {loc.lower() for loc in INDIAN_LOCATIONS}.union({
    "india", "bharat", "delhi ncr", "delhi-ncr", "bangalore", "bengaluru", "mumbai", "hyderabad",
    "pune", "chennai", "kolkata", "noida", "gurugram", "gurgaon", "ahmedabad", "jaipur"
})

def classify_experience_and_employment(title: str, description: str, raw_job_type: Optional[str] = None) -> Dict[str, Any]:
    """Classifies employment type and experience requirements for freshers, interns and seniors."""
    title_lower = title.lower()
    desc_lower = (description or "").lower()

    # 1. Internship / Trainee detection
    if any(k in title_lower for k in ["intern", "internship", "apprentice", "trainee"]) or \
       (raw_job_type and "intern" in raw_job_type.lower()):
        return {
            "employment_type": "Internship",
            "experience_level": "Internship",
            "experience_years_required": 0.0
        }

    # 2. Fresher / Entry-Level detection
    if any(k in title_lower for k in ["fresher", "entry level", "entry-level", "junior", "graduate", "associate", "0-1 year"]):
        emp = raw_job_type.replace("_", " ").title() if raw_job_type else "Full-time"
        return {
            "employment_type": emp,
            "experience_level": "Entry-level",
            "experience_years_required": 0.0
        }

    # 3. Senior / Lead detection
    if any(k in title_lower for k in ["senior", "lead", "staff", "principal", "architect", "director", "head of", "manager"]):
        emp = raw_job_type.replace("_", " ").title() if raw_job_type else "Full-time"
        return {
            "employment_type": emp,
            "experience_level": "Senior",
            "experience_years_required": 4.0
        }

    # 4. Default: Mid-level
    emp = raw_job_type.replace("_", " ").title() if raw_job_type else "Full-time"
    return {
        "employment_type": emp,
        "experience_level": "Mid-level",
        "experience_years_required": 2.0
    }

def check_india_eligibility(location: str, candidate_req_loc: str = "") -> (bool, str):
    """
    Determines whether a job is based in India or open to Indian applicants.
    Returns (is_india, display_location).
    """
    combined = f"{location} {candidate_req_loc}".lower()

    # Direct Indian location match
    if any(k in combined for k in INDIAN_CITY_KEYWORDS):
        loc_display = location.strip() if location else "India"
        if "india" not in loc_display.lower() and not any(k in loc_display.lower() for k in ["remote", "worldwide"]):
            loc_display = f"{loc_display}, India"
        return True, loc_display

    # Worldwide / Anywhere Remote match (accessible from India)
    if any(k in combined for k in ["worldwide", "anywhere", "global"]):
        return True, "Remote (Worldwide / India Eligible)"

    # Specific non-Indian restrictions (e.g., USA only, UK, Germany, EMEA)
    if candidate_req_loc:
        return False, f"{candidate_req_loc.strip()} (International)"
    elif location:
        return False, f"{location.strip()} (International)"

    return False, "International"

class BaseJobProvider(ABC):
    name: str
    api_url: str

    @abstractmethod
    async def fetch_jobs(self, limit: int = 50) -> List[Dict[str, Any]]:
        pass

class RemotiveJobProvider(BaseJobProvider):
    name = "Remotive"
    api_url = "https://remotive.com/api/remote-jobs"

    async def fetch_jobs(self, limit: int = 50) -> List[Dict[str, Any]]:
        url = f"{self.api_url}?limit={limit}"
        headers = {"User-Agent": "CareerConnect/1.0 (Skill-Based-Matching-App)"}
        normalized_jobs = []

        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                resp = await client.get(url, headers=headers)
                if resp.status_code != 200:
                    logger.warning(f"Remotive API returned status {resp.status_code}")
                    return []
                data = resp.json()
                raw_jobs = data.get("jobs", [])

                for item in raw_jobs:
                    # Clean text
                    desc_clean = clean_html(item.get("description", ""))
                    tags = item.get("tags", [])
                    extracted_skills = extract_skills_from_text_and_tags(desc_clean, tags)

                    pub_date = None
                    if item.get("publication_date"):
                        try:
                            pub_date = datetime.fromisoformat(item["publication_date"].replace("Z", "+00:00"))
                        except Exception:
                            pub_date = None

                    title_str = item.get("title", "Untitled Position").strip()
                    cand_req_loc = item.get("candidate_required_location") or ""
                    is_india_eligible, loc_display = check_india_eligibility("", cand_req_loc)

                    # Authentic classification of role & experience level
                    role_info = classify_experience_and_employment(title_str, desc_clean, item.get("job_type"))

                    normalized_jobs.append({
                        "external_id": str(item.get("id")),
                        "title": title_str,
                        "company_name": item.get("company_name", "Undisclosed Company").strip(),
                        "company_logo": item.get("company_logo_url") or item.get("company_logo"),
                        "location": loc_display,
                        "employment_type": role_info["employment_type"],
                        "experience_level": role_info["experience_level"],
                        "experience_years_required": role_info["experience_years_required"],
                        "min_education": "Bachelor's Degree",
                        "description": desc_clean[:4000] if desc_clean else "No description provided by source.",
                        "salary": item.get("salary") or None,
                        "publication_date": pub_date,
                        "original_url": item.get("url", ""),
                        "is_remote": True,
                        "is_india": is_india_eligible,
                        "required_skills": extracted_skills
                    })
        except Exception as e:
            logger.error(f"Error fetching from Remotive: {e}")

        return normalized_jobs

class ArbeitnowJobProvider(BaseJobProvider):
    name = "Arbeitnow"
    api_url = "https://www.arbeitnow.com/api/job-board-api"

    async def fetch_jobs(self, limit: int = 50) -> List[Dict[str, Any]]:
        headers = {"User-Agent": "CareerConnect/1.0 (Skill-Based-Matching-App)"}
        normalized_jobs = []

        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                resp = await client.get(self.api_url, headers=headers)
                if resp.status_code != 200:
                    logger.warning(f"Arbeitnow API returned status {resp.status_code}")
                    return []
                data = resp.json()
                raw_jobs = data.get("data", [])

                for item in raw_jobs[:limit]:
                    desc_clean = clean_html(item.get("description", ""))
                    tags = item.get("tags", [])
                    extracted_skills = extract_skills_from_text_and_tags(desc_clean, tags)

                    pub_date = None
                    if item.get("created_at"):
                        try:
                            # Arbeitnow created_at is unix epoch int or string
                            if isinstance(item["created_at"], (int, float)):
                                pub_date = datetime.fromtimestamp(item["created_at"], tz=timezone.utc)
                            else:
                                pub_date = datetime.fromisoformat(str(item["created_at"]))
                        except Exception:
                            pub_date = None

                    title_str = item.get("title", "Untitled Position").strip()
                    raw_loc = item.get("location") or ""
                    is_remote = bool(item.get("remote"))
                    is_india_eligible, loc_display = check_india_eligibility(raw_loc, "Worldwide" if is_remote and not raw_loc else "")

                    job_types = item.get("job_types", [])
                    raw_type = job_types[0] if job_types else None
                    role_info = classify_experience_and_employment(title_str, desc_clean, raw_type)

                    normalized_jobs.append({
                        "external_id": str(item.get("slug")),
                        "title": title_str,
                        "company_name": item.get("company_name", "Undisclosed Company").strip(),
                        "company_logo": None,
                        "location": loc_display,
                        "employment_type": role_info["employment_type"],
                        "experience_level": role_info["experience_level"],
                        "experience_years_required": role_info["experience_years_required"],
                        "min_education": "Bachelor's Degree",
                        "description": desc_clean[:4000] if desc_clean else "No description provided by source.",
                        "salary": None,
                        "publication_date": pub_date,
                        "original_url": item.get("url", ""),
                        "is_remote": is_remote,
                        "is_india": is_india_eligible,
                        "required_skills": extracted_skills
                    })
        except Exception as e:
            logger.error(f"Error fetching from Arbeitnow: {e}")

        return normalized_jobs

class AdzunaJobProvider(BaseJobProvider):
    name = "Adzuna"
    api_url = "https://api.adzuna.com/v1/api/jobs"

    async def fetch_jobs(self, limit: int = 50) -> List[Dict[str, Any]]:
        if not settings.ADZUNA_APP_ID or not settings.ADZUNA_APP_KEY:
            logger.info("Adzuna API credentials not configured; skipping provider.")
            return []

        url = f"{self.api_url}/{settings.ADZUNA_COUNTRY}/search/1"
        params = {
            "app_id": settings.ADZUNA_APP_ID,
            "app_key": settings.ADZUNA_APP_KEY,
            "results_per_page": limit,
            "content-type": "application/json"
        }
        normalized_jobs = []

        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                resp = await client.get(url, params=params)
                if resp.status_code == 200:
                    data = resp.json()
                    for item in data.get("results", []):
                        desc = clean_html(item.get("description", ""))
                        skills = extract_skills_from_text_and_tags(desc, [])
                        location_name = item.get("location", {}).get("display_name", "India")
                        salary_min = item.get("salary_min")
                        salary_max = item.get("salary_max")
                        salary_str = None
                        if salary_min:
                            if settings.ADZUNA_COUNTRY == "in":
                                if salary_min >= 100000:
                                    min_lpa = round(salary_min / 100000, 1)
                                    max_lpa = round((salary_max or salary_min) / 100000, 1)
                                    salary_str = f"₹{min_lpa} - ₹{max_lpa} LPA"
                                else:
                                    salary_str = f"₹{int(salary_min):,}"
                            else:
                                salary_str = f"${salary_min:,.0f} - ${salary_max:,.0f}" if salary_max else f"${salary_min:,.0f}"

                        title_str = item.get("title", "").strip()
                        role_info = classify_experience_and_employment(title_str, desc, item.get("contract_type"))
                        is_india_eligible, loc_display = check_india_eligibility(location_name, "India" if settings.ADZUNA_COUNTRY == "in" else "")

                        normalized_jobs.append({
                            "external_id": str(item.get("id")),
                            "title": title_str,
                            "company_name": item.get("company", {}).get("display_name", "Employer").strip(),
                            "company_logo": None,
                            "location": loc_display,
                            "employment_type": role_info["employment_type"],
                            "experience_level": role_info["experience_level"],
                            "experience_years_required": role_info["experience_years_required"],
                            "min_education": "Bachelor's Degree",
                            "description": desc[:4000],
                            "salary": salary_str,
                            "publication_date": datetime.fromisoformat(item["created"]) if item.get("created") else None,
                            "original_url": item.get("redirect_url", ""),
                            "is_remote": "remote" in desc.lower() or "remote" in title_str.lower(),
                            "is_india": is_india_eligible,
                            "required_skills": skills
                        })
        except Exception as e:
            logger.error(f"Error fetching from Adzuna: {e}")

        return normalized_jobs


class JobSyncService:
    @staticmethod
    def get_registered_providers() -> List[BaseJobProvider]:
        providers = []
        if settings.ENABLE_REMOTIVE:
            providers.append(RemotiveJobProvider())
        if settings.ENABLE_ARBEITNOW:
            providers.append(ArbeitnowJobProvider())
        if settings.ADZUNA_APP_ID and settings.ADZUNA_APP_KEY:
            providers.append(AdzunaJobProvider())
        return providers

    @classmethod
    async def synchronize_jobs(cls, db: Session, limit_per_provider: int = 30) -> Dict[str, Any]:
        """
        Fetches genuine listings from authorized providers, normalizes, dedupes, and updates DB.
        """
        providers = cls.get_registered_providers()
        summary = {
            "providers_polled": len(providers),
            "jobs_fetched": 0,
            "jobs_created": 0,
            "jobs_updated": 0,
            "errors": []
        }

        for provider in providers:
            try:
                # Ensure JobSource record exists
                source = db.query(JobSource).filter(JobSource.name == provider.name).first()
                if not source:
                    source = JobSource(name=provider.name, api_url=provider.api_url, is_active=True)
                    db.add(source)
                    db.commit()
                    db.refresh(source)

                raw_jobs = await provider.fetch_jobs(limit=limit_per_provider)
                summary["jobs_fetched"] += len(raw_jobs)

                for item in raw_jobs:
                    existing_job = db.query(Job).filter(
                        Job.source_id == source.id,
                        Job.external_id == item["external_id"]
                    ).first()

                    if existing_job:
                        # Update fields
                        existing_job.title = item["title"]
                        existing_job.company_name = item["company_name"]
                        existing_job.company_logo = item["company_logo"]
                        existing_job.location = item["location"]
                        existing_job.employment_type = item["employment_type"]
                        existing_job.description = item["description"]
                        existing_job.salary = item["salary"]
                        existing_job.is_remote = item["is_remote"]
                        existing_job.is_india = item.get("is_india", False)
                        existing_job.is_active = True
                        existing_job.original_url = item["original_url"]
                        summary["jobs_updated"] += 1
                        db_job = existing_job
                    else:
                        db_job = Job(
                            source_id=source.id,
                            external_id=item["external_id"],
                            title=item["title"],
                            company_name=item["company_name"],
                            company_logo=item["company_logo"],
                            location=item["location"],
                            employment_type=item["employment_type"],
                            experience_level=item["experience_level"],
                            experience_years_required=item["experience_years_required"],
                            min_education=item["min_education"],
                            description=item["description"],
                            salary=item["salary"],
                            publication_date=item["publication_date"],
                            original_url=item["original_url"],
                            is_remote=item["is_remote"],
                            is_india=item.get("is_india", False),
                            is_active=True
                        )
                        db.add(db_job)
                        db.flush()
                        summary["jobs_created"] += 1

                    # Add required skills
                    req_skills = item.get("required_skills", [])
                    existing_skills = {s.normalized_name for s in db_job.required_skills}
                    seen_in_batch = set()
                    for sk in req_skills:
                        sk_norm = normalize_skill(sk)
                        if sk_norm not in existing_skills and sk_norm not in seen_in_batch:
                            seen_in_batch.add(sk_norm)
                            db_skill = JobRequiredSkill(
                                job_id=db_job.id,
                                skill_name=sk.title(),
                                normalized_name=sk_norm,
                                is_mandatory=True
                            )
                            db.add(db_skill)

                source.last_synced_at = utcnow()
                db.commit()
            except Exception as e:
                db.rollback()
                logger.error(f"Sync error for provider {provider.name}: {e}")
                summary["errors"].append(f"{provider.name}: {str(e)}")

        return summary
