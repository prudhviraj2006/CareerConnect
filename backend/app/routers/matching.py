from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from backend.app.database import get_db
from backend.app.models.all_models import Job, JobMatch, UserProfile, User
from backend.app.schemas.schemas import (
    MatchScoreResponse,
    MatchScoreBreakdown,
    SkillGapResponse
)
from backend.app.services.auth_service import get_current_user
from backend.app.services.skill_matching_service import SkillMatchingService

router = APIRouter(prefix="/api", tags=["Matching & Skill Gap"])

@router.get("/matches/{job_id}", response_model=MatchScoreResponse)
def get_job_match(
    job_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    job = db.query(Job).filter(Job.id == job_id).first()
    if not job:
        raise HTTPException(status_code=404, detail="Job not found.")

    profile = db.query(UserProfile).filter(UserProfile.user_id == current_user.id).first()
    if not profile:
        profile = UserProfile(user_id=current_user.id)
        db.add(profile)
        db.commit()
        db.refresh(profile)

    calc = SkillMatchingService.calculate_match(profile, job)

    breakdown = MatchScoreBreakdown(
        skills_score=calc["skills_score"],
        skills_weight=60.0,
        experience_score=calc["experience_score"],
        experience_weight=20.0,
        education_score=calc["education_score"],
        education_weight=10.0,
        location_preference_score=calc["location_preference_score"],
        location_preference_weight=10.0
    )

    return MatchScoreResponse(
        job_id=job.id,
        overall_match_score=calc["overall_match_score"],
        has_sufficient_info=calc["has_sufficient_info"],
        matching_skills=calc["matching_skills"],
        missing_skills=calc["missing_skills"],
        breakdown=breakdown,
        explanation_summary=calc["explanation_summary"],
        recommendation_tier=calc["recommendation_tier"]
    )

@router.get("/skill-gaps/{job_id}", response_model=SkillGapResponse)
def get_skill_gap(
    job_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    job = db.query(Job).filter(Job.id == job_id).first()
    if not job:
        raise HTTPException(status_code=404, detail="Job not found.")

    profile = db.query(UserProfile).filter(UserProfile.user_id == current_user.id).first()
    user_skills = (profile.technical_skills or []) + (profile.soft_skills or []) if profile else []
    job_skills = [s.skill_name for s in job.required_skills]

    calc = SkillMatchingService.calculate_match(profile, job) if profile else {}
    matching = calc.get("matching_skills", [])
    missing = calc.get("missing_skills", [])

    boost_potential = 0.0
    if job_skills and missing:
        # Each missing skill represents a proportion of 60% skills weight
        boost_potential = round((len(missing) / len(job_skills)) * 60.0, 1)

    advice = "You have all listed required skills for this position! Focus on highlighting past accomplishments."
    if missing:
        advice = f"Acquiring proficiency in {', '.join(missing[:3])} could increase your match compatibility by up to {boost_potential}%."

    return SkillGapResponse(
        job_id=job.id,
        job_title=job.title,
        company_name=job.company_name,
        user_skills=user_skills,
        job_required_skills=job_skills,
        matching_skills=matching,
        missing_skills=missing,
        improvement_advice=advice,
        compatibility_boost_potential=boost_potential
    )
