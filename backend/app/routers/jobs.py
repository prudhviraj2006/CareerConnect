from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from sqlalchemy import or_, desc, asc
from typing import List, Optional

from backend.app.database import get_db
from backend.app.models.all_models import Job, JobRequiredSkill, JobSource, JobMatch, User
from backend.app.schemas.schemas import JobResponse, JobDetailResponse
from backend.app.services.auth_service import oauth2_scheme, decode_token
from backend.app.services.job_sync_service import JobSyncService

router = APIRouter(prefix="/api/jobs", tags=["Jobs"])

def try_get_user_id_from_token(token: Optional[str], db: Session) -> Optional[int]:
    if not token:
        return None
    try:
        payload = decode_token(token)
        return int(payload.get("sub"))
    except Exception:
        return None

def serialize_job(job: Job, user_id: Optional[int] = None, db: Optional[Session] = None) -> dict:
    source_name = job.source.name if job.source else "Direct Provider"
    skills = [s.skill_name for s in job.required_skills]
    match_score = None

    if user_id and db:
        match_rec = db.query(JobMatch).filter(
            JobMatch.user_id == user_id,
            JobMatch.job_id == job.id
        ).first()
        if match_rec:
            match_score = match_rec.overall_match_score

    return {
        "id": job.id,
        "title": job.title,
        "company_name": job.company_name,
        "company_logo": job.company_logo,
        "location": job.location,
        "employment_type": job.employment_type,
        "experience_level": job.experience_level,
        "experience_years_required": job.experience_years_required,
        "min_education": job.min_education,
        "salary": job.salary,
        "publication_date": job.publication_date,
        "original_url": job.original_url,
        "is_remote": job.is_remote,
        "is_india": bool(job.is_india) if hasattr(job, "is_india") and job.is_india is not None else False,
        "is_active": job.is_active,
        "source_name": source_name,
        "required_skills": skills,
        "match_score": match_score
    }

@router.get("", response_model=List[JobResponse])
def get_jobs(
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100),
    sort_by: str = Query("date", pattern="^(date|relevance)$"),
    location: Optional[str] = None,
    experience_level: Optional[str] = None,
    employment_type: Optional[str] = None,
    is_remote: Optional[bool] = None,
    india_only: Optional[bool] = None,
    db: Session = Depends(get_db)
):
    query = db.query(Job).filter(Job.is_active == True)

    if location:
        query = query.filter(Job.location.ilike(f"%{location}%"))
    if experience_level:
        query = query.filter(Job.experience_level.ilike(f"%{experience_level}%"))
    if employment_type:
        query = query.filter(Job.employment_type.ilike(f"%{employment_type}%"))
    if is_remote is not None:
        query = query.filter(Job.is_remote == is_remote)
    if india_only is True:
        query = query.filter(Job.is_india == True)

    # India-first prioritization: Indian / India-eligible jobs appear first
    if sort_by == "date":
        query = query.order_by(Job.is_india.desc(), Job.publication_date.desc(), Job.id.desc())
    else:
        query = query.order_by(Job.is_india.desc(), Job.id.desc())

    offset = (page - 1) * limit
    jobs = query.offset(offset).limit(limit).all()

    return [JobResponse(**serialize_job(j)) for j in jobs]

@router.get("/search", response_model=List[JobResponse])
def search_jobs(
    q: str = Query(..., min_length=1),
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100),
    location: Optional[str] = None,
    employment_type: Optional[str] = None,
    is_remote: Optional[bool] = None,
    india_only: Optional[bool] = None,
    db: Session = Depends(get_db)
):
    query = db.query(Job).filter(Job.is_active == True)

    # Search in title, company_name, description or required skills
    skill_subquery = db.query(JobRequiredSkill.job_id).filter(
        JobRequiredSkill.skill_name.ilike(f"%{q}%")
    )

    query = query.filter(
        or_(
            Job.title.ilike(f"%{q}%"),
            Job.company_name.ilike(f"%{q}%"),
            Job.description.ilike(f"%{q}%"),
            Job.id.in_(skill_subquery)
        )
    )

    if location:
        query = query.filter(Job.location.ilike(f"%{location}%"))
    if employment_type:
        query = query.filter(Job.employment_type.ilike(f"%{employment_type}%"))
    if is_remote is not None:
        query = query.filter(Job.is_remote == is_remote)
    if india_only is True:
        query = query.filter(Job.is_india == True)

    query = query.order_by(Job.is_india.desc(), Job.publication_date.desc(), Job.id.desc())
    offset = (page - 1) * limit
    jobs = query.offset(offset).limit(limit).all()

    return [JobResponse(**serialize_job(j)) for j in jobs]

@router.get("/{job_id}", response_model=JobDetailResponse)
def get_job_details(job_id: int, db: Session = Depends(get_db)):
    job = db.query(Job).filter(Job.id == job_id).first()
    if not job:
        raise HTTPException(status_code=404, detail="Job not found.")

    base_dict = serialize_job(job)
    base_dict["description"] = job.description
    return JobDetailResponse(**base_dict)

@router.post("/sync")
async def sync_external_jobs(
    limit_per_provider: int = Query(25, ge=5, le=50),
    db: Session = Depends(get_db)
):
    summary = await JobSyncService.synchronize_jobs(db, limit_per_provider=limit_per_provider)
    return {
        "status": "success",
        "message": "Job synchronization cycle complete.",
        "details": summary
    }
