import os
import shutil
import uuid
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, status
from sqlalchemy.orm import Session

from backend.app.config import settings
from backend.app.database import get_db
from backend.app.models.all_models import User, Resume, UserProfile
from backend.app.schemas.schemas import ResumeResponse, UserProfileResponse, UserProfileUpdate
from backend.app.services.auth_service import get_current_user
from backend.app.services.resume_parser import ResumeParserService
from backend.app.services.skill_matching_service import SkillMatchingService

router = APIRouter(prefix="/api/resume", tags=["Resume Processing"])

ALLOWED_EXTENSIONS = {"pdf", "docx", "doc"}

@router.post("/upload", response_model=ResumeResponse)
async def upload_resume(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if not file.filename:
        raise HTTPException(status_code=400, detail="No file selected.")

    filename = file.filename
    ext = filename.split(".")[-1].lower() if "." in filename else ""
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported file format '{ext}'. Allowed formats are PDF and DOCX."
        )

    # Read and check size
    contents = await file.read()
    file_size = len(contents)
    max_bytes = settings.MAX_UPLOAD_SIZE_MB * 1024 * 1024
    if file_size > max_bytes:
        raise HTTPException(
            status_code=400,
            detail=f"File exceeds maximum allowed size of {settings.MAX_UPLOAD_SIZE_MB}MB."
        )

    # Save to disk
    os.makedirs(settings.UPLOAD_DIR, exist_ok=True)
    saved_filename = f"{current_user.id}_{uuid.uuid4().hex[:8]}.{ext}"
    saved_path = os.path.join(settings.UPLOAD_DIR, saved_filename)

    with open(saved_path, "wb") as f:
        f.write(contents)

    # Parse resume
    parse_status = "SUCCESS"
    parsing_errors = None
    parsed_data = {}
    parsed_text = ""

    try:
        parsed_text = ResumeParserService.extract_resume_text(saved_path, ext)
        parsed_data = ResumeParserService.parse_resume_content(parsed_text)
    except Exception as e:
        parse_status = "FAILED"
        parsing_errors = str(e)

    # Remove previous resume record for user if replacing
    old_resumes = db.query(Resume).filter(Resume.user_id == current_user.id).all()
    for old_r in old_resumes:
        if os.path.exists(old_r.file_path):
            try:
                os.remove(old_r.file_path)
            except Exception:
                pass
        db.delete(old_r)

    resume_record = Resume(
        user_id=current_user.id,
        file_name=filename,
        file_path=saved_path,
        file_type=ext,
        file_size=file_size,
        parsed_text=parsed_text[:10000] if parsed_text else None,
        parsed_data=parsed_data,
        parse_status=parse_status,
        parsing_errors=parsing_errors
    )
    db.add(resume_record)
    db.commit()
    db.refresh(resume_record)

    return ResumeResponse.model_validate(resume_record)

@router.get("", response_model=ResumeResponse)
def get_resume(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    resume = db.query(Resume).filter(Resume.user_id == current_user.id).order_by(Resume.created_at.desc()).first()
    if not resume:
        raise HTTPException(status_code=404, detail="No resume found for current user.")
    return ResumeResponse.model_validate(resume)

@router.delete("", status_code=status.HTTP_200_OK)
def delete_resume(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    resumes = db.query(Resume).filter(Resume.user_id == current_user.id).all()
    if not resumes:
        raise HTTPException(status_code=404, detail="No resume exists to delete.")

    for r in resumes:
        if os.path.exists(r.file_path):
            try:
                os.remove(r.file_path)
            except Exception:
                pass
        db.delete(r)

    db.commit()
    return {"message": "Resume and extracted data deleted successfully."}

@router.post("/confirm", response_model=UserProfileResponse)
def confirm_extracted_resume_data(
    reviewed_profile: UserProfileUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Saves the user-confirmed details into the UserProfile.
    Honors the academic requirement: Only save user-confirmed details.
    """
    profile = db.query(UserProfile).filter(UserProfile.user_id == current_user.id).first()
    if not profile:
        profile = UserProfile(user_id=current_user.id)
        db.add(profile)

    if reviewed_profile.full_name:
        profile.full_name = reviewed_profile.full_name
        current_user.full_name = reviewed_profile.full_name
    if reviewed_profile.phone_number:
        profile.phone_number = reviewed_profile.phone_number
        current_user.phone_number = reviewed_profile.phone_number
    if reviewed_profile.education_level:
        profile.education_level = reviewed_profile.education_level
    if reviewed_profile.college_university:
        profile.college_university = reviewed_profile.college_university
    if reviewed_profile.degree:
        profile.degree = reviewed_profile.degree
    if reviewed_profile.specialization:
        profile.specialization = reviewed_profile.specialization
    if reviewed_profile.graduation_year:
        profile.graduation_year = reviewed_profile.graduation_year

    profile.technical_skills = reviewed_profile.technical_skills
    profile.soft_skills = reviewed_profile.soft_skills
    profile.work_experience = reviewed_profile.work_experience
    profile.experience_years = reviewed_profile.experience_years
    if reviewed_profile.internship_experience is not None:
        profile.internship_experience = reviewed_profile.internship_experience
    if reviewed_profile.projects is not None:
        profile.projects = reviewed_profile.projects
    if reviewed_profile.certifications is not None:
        profile.certifications = reviewed_profile.certifications
    profile.is_confirmed_by_user = True

    db.commit()
    db.refresh(profile)

    # Recalculate matches
    SkillMatchingService.update_matches_for_user(db, current_user.id)

    return UserProfileResponse.model_validate(profile)
