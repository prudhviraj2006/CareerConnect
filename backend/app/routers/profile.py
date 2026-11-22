from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import datetime

from backend.app.database import get_db
from backend.app.models.all_models import User, UserProfile, UserSkill
from backend.app.schemas.schemas import UserProfileResponse, UserProfileUpdate
from backend.app.services.auth_service import get_current_user
from backend.app.services.resume_parser import normalize_skill
from backend.app.services.skill_matching_service import SkillMatchingService

router = APIRouter(prefix="/api/profile", tags=["User Profile"])

@router.get("", response_model=UserProfileResponse)
def get_user_profile(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    profile = db.query(UserProfile).filter(UserProfile.user_id == current_user.id).first()
    if not profile:
        profile = UserProfile(
            user_id=current_user.id,
            full_name=current_user.full_name,
            email=current_user.email,
            phone_number=current_user.phone_number
        )
        db.add(profile)
        db.commit()
        db.refresh(profile)
    return UserProfileResponse.model_validate(profile)

@router.put("", response_model=UserProfileResponse)
def update_user_profile(
    req: UserProfileUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    profile = db.query(UserProfile).filter(UserProfile.user_id == current_user.id).first()
    if not profile:
        profile = UserProfile(user_id=current_user.id)
        db.add(profile)

    if req.full_name is not None:
        profile.full_name = req.full_name
        current_user.full_name = req.full_name
    if req.email is not None:
        profile.email = req.email
    if req.phone_number is not None:
        profile.phone_number = req.phone_number
        current_user.phone_number = req.phone_number

    if req.education_level is not None:
        profile.education_level = req.education_level
    if req.college_university is not None:
        profile.college_university = req.college_university
    if req.degree is not None:
        profile.degree = req.degree
    if req.specialization is not None:
        profile.specialization = req.specialization
    if req.graduation_year is not None:
        profile.graduation_year = req.graduation_year

    profile.technical_skills = req.technical_skills
    profile.soft_skills = req.soft_skills

    if req.work_experience is not None:
        profile.work_experience = req.work_experience
    profile.experience_years = req.experience_years
    if req.internship_experience is not None:
        profile.internship_experience = req.internship_experience

    if req.preferred_job_role is not None:
        profile.preferred_job_role = req.preferred_job_role
    if req.preferred_location is not None:
        profile.preferred_location = req.preferred_location
    if req.expected_salary is not None:
        profile.expected_salary = req.expected_salary
    if req.employment_type_preference is not None:
        profile.employment_type_preference = req.employment_type_preference

    profile.is_confirmed_by_user = True
    profile.updated_at = datetime.utcnow()

    # Synchronize user_skills table
    db.query(UserSkill).filter(UserSkill.user_id == current_user.id).delete()
    for s in req.technical_skills:
        db.add(UserSkill(
            user_id=current_user.id,
            skill_name=s,
            normalized_name=normalize_skill(s),
            category="technical",
            is_verified_by_user=True
        ))
    for s in req.soft_skills:
        db.add(UserSkill(
            user_id=current_user.id,
            skill_name=s,
            normalized_name=normalize_skill(s),
            category="soft",
            is_verified_by_user=True
        ))

    db.commit()
    db.refresh(profile)

    # Recalculate matches and recommendations in background/inline
    try:
        SkillMatchingService.update_matches_for_user(db, current_user.id)
    except Exception as e:
        # Don't fail the profile update if matching experiences an issue
        pass

    return UserProfileResponse.model_validate(profile)
