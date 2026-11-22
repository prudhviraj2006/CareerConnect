from typing import Dict, Any, List, Tuple
from sqlalchemy.orm import Session
from datetime import datetime

from backend.app.models.all_models import (
    User, UserProfile, Job, JobMatch, Recommendation, Notification
)
from backend.app.services.resume_parser import normalize_skill

# Configurable Weights
WEIGHT_SKILLS = 0.60
WEIGHT_EXPERIENCE = 0.20
WEIGHT_EDUCATION = 0.10
WEIGHT_PREFERENCES = 0.10

class SkillMatchingService:

    @classmethod
    def calculate_match(cls, profile: UserProfile, job: Job) -> Dict[str, Any]:
        """
        Calculates an objective match score between a user profile and a job listing.
        Returns match score (0-100), breakdown, matching skills, missing skills,
        explanation, and whether sufficient info exists.
        """
        user_skills_set = set()
        if profile.technical_skills:
            for s in profile.technical_skills:
                user_skills_set.add(normalize_skill(s))
        if profile.soft_skills:
            for s in profile.soft_skills:
                user_skills_set.add(normalize_skill(s))

        job_skills_req = [s.normalized_name for s in job.required_skills]
        job_skills_display = [s.skill_name for s in job.required_skills]

        # 1. Check if sufficient info exists
        if not job_skills_req and len(job.description or "") < 50:
            return {
                "overall_match_score": 0.0,
                "has_sufficient_info": False,
                "matching_skills": [],
                "missing_skills": [],
                "skills_score": 0.0,
                "experience_score": 0.0,
                "education_score": 0.0,
                "location_preference_score": 0.0,
                "explanation_summary": "Insufficient information in job posting to calculate an accurate skill match score.",
                "recommendation_tier": "Insufficient Info",
                "detailed_breakdown": {
                    "reason": "The employer did not specify required skills or detailed requirements."
                }
            }

        # 2. Skills Match (60%)
        matching_skills = []
        missing_skills = []

        if job_skills_req:
            for norm_s, disp_s in zip(job_skills_req, job_skills_display):
                if norm_s in user_skills_set:
                    matching_skills.append(disp_s)
                else:
                    missing_skills.append(disp_s)

            skill_ratio = len(matching_skills) / len(job_skills_req)
        else:
            # Fallback if no specific skills tagged: 0% skill match (cannot guess)
            skill_ratio = 0.0

        skills_component = round(skill_ratio * (WEIGHT_SKILLS * 100), 1)

        # 3. Experience Match (20%)
        exp_component = 0.0
        exp_explanation = ""
        user_years = profile.experience_years or 0.0
        req_years = job.experience_years_required

        if req_years is None:
            # Experience not specified by job
            exp_component = round(0.5 * (WEIGHT_EXPERIENCE * 100), 1) # Neutral half score
            exp_explanation = "Job experience requirement not specified."
        elif user_years >= req_years:
            exp_component = round(WEIGHT_EXPERIENCE * 100, 1) # Full 20 pts
            exp_explanation = f"You meet or exceed the required experience ({user_years} yrs vs {req_years} yrs)."
        elif user_years >= (req_years - 1.0):
            exp_component = round(0.75 * (WEIGHT_EXPERIENCE * 100), 1)
            exp_explanation = f"You are close to the required experience ({user_years} yrs vs {req_years} yrs)."
        elif user_years > 0:
            exp_component = round((user_years / req_years) * (WEIGHT_EXPERIENCE * 100), 1)
            exp_explanation = f"Partial experience match ({user_years} yrs vs {req_years} yrs required)."
        else:
            exp_component = 0.0
            exp_explanation = f"Position requires {req_years} years of experience."

        # 4. Education Match (10%)
        edu_component = 0.0
        edu_explanation = ""
        user_edu = (profile.education_level or "").lower()
        job_edu = (job.min_education or "").lower()

        if not job_edu:
            edu_component = round(0.5 * (WEIGHT_EDUCATION * 100), 1)
            edu_explanation = "Education criteria not specified by source."
        elif "bachelor" in job_edu:
            if any(term in user_edu for term in ["bachelor", "master", "phd", "doctorate"]):
                edu_component = round(WEIGHT_EDUCATION * 100, 1)
                edu_explanation = "You satisfy the minimum degree requirement."
            else:
                edu_component = round(0.4 * (WEIGHT_EDUCATION * 100), 1)
                edu_explanation = "Requires a Bachelor's degree or higher."
        elif "master" in job_edu:
            if any(term in user_edu for term in ["master", "phd", "doctorate"]):
                edu_component = round(WEIGHT_EDUCATION * 100, 1)
                edu_explanation = "You satisfy the Master's degree requirement."
            else:
                edu_component = round(0.3 * (WEIGHT_EDUCATION * 100), 1)
                edu_explanation = "Prefers a Master's degree."
        else:
            edu_component = round(WEIGHT_EDUCATION * 100, 1)
            edu_explanation = "Education requirement satisfied."

        # 5. Location and Preference Match (10%)
        pref_component = 0.0
        pref_reasons = []

        # Location check
        user_loc = (profile.preferred_location or "").lower()
        job_loc = (job.location or "").lower()

        if job.is_remote:
            pref_component += 5.0
            pref_reasons.append("Remote job matches flexible location preferences.")
        elif user_loc and user_loc in job_loc:
            pref_component += 5.0
            pref_reasons.append(f"Job is located in your preferred location ({profile.preferred_location}).")
        else:
            pref_component += 2.0
            pref_reasons.append("Location is outside specified preferred location.")

        # Employment type check
        user_emp = (profile.employment_type_preference or "").lower()
        job_emp = (job.employment_type or "").lower()

        if not user_emp or not job_emp or user_emp in job_emp or job_emp in user_emp:
            pref_component += 5.0
            pref_reasons.append("Employment type matches your preferences.")
        else:
            pref_component += 1.0
            pref_reasons.append(f"Employment type is {job.employment_type}.")

        overall_score = round(skills_component + exp_component + edu_component + pref_component, 1)
        overall_score = max(0.0, min(100.0, overall_score))

        # Explanation Summary
        reasons = []
        if matching_skills:
            reasons.append(f"You have {len(matching_skills)} of {len(job_skills_req)} required skills: {', '.join(matching_skills[:3])}.")
        if missing_skills:
            reasons.append(f"Missing {len(missing_skills)} skill(s) like {', '.join(missing_skills[:2])}.")
        reasons.append(exp_explanation)
        reasons.extend(pref_reasons[:1])

        explanation_summary = " ".join(reasons)

        tier = "Low"
        if overall_score >= 75.0:
            tier = "High"
        elif overall_score >= 50.0:
            tier = "Moderate"

        return {
            "overall_match_score": overall_score,
            "has_sufficient_info": True,
            "matching_skills": matching_skills,
            "missing_skills": missing_skills,
            "skills_score": skills_component,
            "experience_score": exp_component,
            "education_score": edu_component,
            "location_preference_score": pref_component,
            "explanation_summary": explanation_summary,
            "recommendation_tier": tier,
            "detailed_breakdown": {
                "skills_detail": f"{len(matching_skills)}/{len(job_skills_req)} matched ({skills_component}/60 pts)",
                "experience_detail": f"{exp_explanation} ({exp_component}/20 pts)",
                "education_detail": f"{edu_explanation} ({edu_component}/10 pts)",
                "preferences_detail": f"{', '.join(pref_reasons)} ({pref_component}/10 pts)"
            }
        }

    @classmethod
    def update_matches_for_user(cls, db: Session, user_id: int):
        """
        Updates JobMatch records and Top Recommendations for a given user.
        """
        user = db.query(User).filter(User.id == user_id).first()
        if not user or not user.profile:
            return

        profile = user.profile
        jobs = db.query(Job).filter(Job.is_active == True).all()

        scored_jobs = []

        for job in jobs:
            match_data = cls.calculate_match(profile, job)

            # Store or update in job_matches table
            match_record = db.query(JobMatch).filter(
                JobMatch.user_id == user_id,
                JobMatch.job_id == job.id
            ).first()

            if not match_record:
                match_record = JobMatch(
                    user_id=user_id,
                    job_id=job.id,
                    overall_match_score=match_data["overall_match_score"],
                    skills_score=match_data["skills_score"],
                    experience_score=match_data["experience_score"],
                    education_score=match_data["education_score"],
                    location_preference_score=match_data["location_preference_score"],
                    matching_skills=match_data["matching_skills"],
                    missing_skills=match_data["missing_skills"],
                    explanation_summary=match_data["explanation_summary"],
                    detailed_breakdown=match_data["detailed_breakdown"],
                    has_sufficient_info=match_data["has_sufficient_info"]
                )
                db.add(match_record)
            else:
                match_record.overall_match_score = match_data["overall_match_score"]
                match_record.skills_score = match_data["skills_score"]
                match_record.experience_score = match_data["experience_score"]
                match_record.education_score = match_data["education_score"]
                match_record.location_preference_score = match_data["location_preference_score"]
                match_record.matching_skills = match_data["matching_skills"]
                match_record.missing_skills = match_data["missing_skills"]
                match_record.explanation_summary = match_data["explanation_summary"]
                match_record.detailed_breakdown = match_data["detailed_breakdown"]
                match_record.has_sufficient_info = match_data["has_sufficient_info"]
                match_record.calculated_at = datetime.utcnow()

            if match_data["has_sufficient_info"] and match_data["overall_match_score"] >= 40.0:
                scored_jobs.append((job, match_data))

        db.commit()

        # Update recommendations: pick top jobs
        scored_jobs.sort(key=lambda x: x[1]["overall_match_score"], reverse=True)

        # Clear old recommendations for user
        db.query(Recommendation).filter(Recommendation.user_id == user_id).delete()

        for idx, (job, match_data) in enumerate(scored_jobs[:15], start=1):
            rec = Recommendation(
                user_id=user_id,
                job_id=job.id,
                match_score=match_data["overall_match_score"],
                recommendation_reason=match_data["explanation_summary"],
                rank_order=idx
            )
            db.add(rec)

        db.commit()
