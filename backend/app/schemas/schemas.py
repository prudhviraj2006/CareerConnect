from pydantic import BaseModel, EmailStr, Field
from typing import List, Optional, Dict, Any
from datetime import datetime

# --- Auth Schemas ---
class UserRegisterRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=6, description="Minimum 6 characters")
    full_name: str = Field(..., min_length=2)
    phone_number: Optional[str] = None

class UserLoginRequest(BaseModel):
    email: EmailStr
    password: str

class GoogleLoginRequest(BaseModel):
    email: EmailStr
    full_name: Optional[str] = "Google User"
    id_token: Optional[str] = None
    photo_url: Optional[str] = None

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    refresh_token: Optional[str] = None
    user: "UserResponse"

class RefreshTokenRequest(BaseModel):
    refresh_token: str

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class ResetPasswordRequest(BaseModel):
    token: str
    new_password: str = Field(..., min_length=6)

class UserResponse(BaseModel):
    id: int
    email: str
    full_name: str
    phone_number: Optional[str] = None
    is_active: bool
    is_verified: bool
    created_at: datetime

    class Config:
        from_attributes = True


# --- Profile Schemas ---
class SkillItem(BaseModel):
    name: str
    proficiency: str = "intermediate" # beginner, intermediate, advanced, expert
    category: str = "technical" # technical, soft

class UserProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    email: Optional[str] = None
    phone_number: Optional[str] = None
    education_level: Optional[str] = None
    college_university: Optional[str] = None
    degree: Optional[str] = None
    specialization: Optional[str] = None
    graduation_year: Optional[int] = None
    technical_skills: List[str] = []
    soft_skills: List[str] = []
    work_experience: Optional[str] = None
    experience_years: float = 0.0
    internship_experience: Optional[str] = None
    preferred_job_role: Optional[str] = None
    preferred_location: Optional[str] = None
    expected_salary: Optional[str] = None
    employment_type_preference: Optional[str] = None
    is_confirmed_by_user: bool = True

class UserProfileResponse(BaseModel):
    id: int
    user_id: int
    full_name: Optional[str] = None
    email: Optional[str] = None
    phone_number: Optional[str] = None
    education_level: Optional[str] = None
    college_university: Optional[str] = None
    degree: Optional[str] = None
    specialization: Optional[str] = None
    graduation_year: Optional[int] = None
    technical_skills: List[str] = []
    soft_skills: List[str] = []
    work_experience: Optional[str] = None
    experience_years: float = 0.0
    internship_experience: Optional[str] = None
    preferred_job_role: Optional[str] = None
    preferred_location: Optional[str] = None
    expected_salary: Optional[str] = None
    employment_type_preference: Optional[str] = None
    is_confirmed_by_user: bool = False
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# --- Resume Schemas ---
class ResumeExtractedData(BaseModel):
    candidate_name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    education_level: Optional[str] = None
    college_university: Optional[str] = None
    degree: Optional[str] = None
    specialization: Optional[str] = None
    graduation_year: Optional[int] = None
    extracted_skills: List[str] = []
    extracted_soft_skills: List[str] = []
    experience_summary: Optional[str] = None
    experience_years: float = 0.0
    relevant_keywords: List[str] = []

class ResumeResponse(BaseModel):
    id: int
    file_name: str
    file_type: str
    file_size: int
    parse_status: str
    parsing_errors: Optional[str] = None
    parsed_data: Optional[Dict[str, Any]] = None
    created_at: datetime

    class Config:
        from_attributes = True

class ResumeConfirmRequest(BaseModel):
    profile_data: UserProfileUpdate


# --- Job Schemas ---
class JobRequiredSkillResponse(BaseModel):
    skill_name: str
    normalized_name: str
    is_mandatory: bool

    class Config:
        from_attributes = True

class JobResponse(BaseModel):
    id: int
    title: str
    company_name: str
    company_logo: Optional[str] = None
    location: Optional[str] = None
    employment_type: Optional[str] = None
    experience_level: Optional[str] = None
    experience_years_required: Optional[float] = None
    min_education: Optional[str] = None
    salary: Optional[str] = None
    publication_date: Optional[datetime] = None
    original_url: str
    is_remote: bool
    is_active: bool
    source_name: Optional[str] = "Direct Provider"
    required_skills: List[str] = []
    match_score: Optional[float] = None

    class Config:
        from_attributes = True

class JobDetailResponse(JobResponse):
    description: str


# --- Matching & Skill Gap Schemas ---
class MatchScoreBreakdown(BaseModel):
    skills_score: float # 0 - 60
    skills_weight: float = 60.0
    experience_score: float # 0 - 20
    experience_weight: float = 20.0
    education_score: float # 0 - 10
    education_weight: float = 10.0
    location_preference_score: float # 0 - 10
    location_preference_weight: float = 10.0

class MatchScoreResponse(BaseModel):
    job_id: int
    overall_match_score: float # 0 - 100
    has_sufficient_info: bool
    matching_skills: List[str]
    missing_skills: List[str]
    breakdown: MatchScoreBreakdown
    explanation_summary: str
    recommendation_tier: str # High, Moderate, Low, Insufficient Info

class SkillGapResponse(BaseModel):
    job_id: int
    job_title: str
    company_name: str
    user_skills: List[str]
    job_required_skills: List[str]
    matching_skills: List[str]
    missing_skills: List[str]
    improvement_advice: str
    compatibility_boost_potential: float


# --- Recommendation Schemas ---
class RecommendationResponse(BaseModel):
    id: int
    job: JobResponse
    match_score: float
    recommendation_reason: str
    rank_order: int
    created_at: datetime

    class Config:
        from_attributes = True


# --- Saved Job Schemas ---
class SavedJobCreate(BaseModel):
    notes: Optional[str] = None

class SavedJobResponse(BaseModel):
    id: int
    job: JobResponse
    notes: Optional[str] = None
    saved_at: datetime

    class Config:
        from_attributes = True


# --- Application Schemas ---
class ApplicationCreate(BaseModel):
    job_id: int
    status: str = "Applied" # Interested, Applied, Interview, Rejected, Offer
    notes: Optional[str] = None

class ApplicationUpdate(BaseModel):
    status: str
    notes: Optional[str] = None

class ApplicationResponse(BaseModel):
    id: int
    job_id: int
    job: JobResponse
    status: str
    notes: Optional[str] = None
    applied_date: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# --- Notification Schemas ---
class NotificationResponse(BaseModel):
    id: int
    title: str
    message: str
    notification_type: str
    reference_id: Optional[int] = None
    is_read: bool
    created_at: datetime

    class Config:
        from_attributes = True

class NotificationPreferenceResponse(BaseModel):
    email_notifications: bool
    push_notifications: bool
    job_matches_enabled: bool
    application_updates_enabled: bool
    recommendations_enabled: bool

    class Config:
        from_attributes = True

class NotificationPreferenceUpdate(BaseModel):
    email_notifications: Optional[bool] = None
    push_notifications: Optional[bool] = None
    job_matches_enabled: Optional[bool] = None
    application_updates_enabled: Optional[bool] = None
    recommendations_enabled: Optional[bool] = None
