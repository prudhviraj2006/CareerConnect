import datetime
from sqlalchemy import (
    Column, Integer, String, Boolean, Float, Text, DateTime, ForeignKey, UniqueConstraint, JSON
)
from sqlalchemy.orm import relationship
from backend.app.database import Base

def utcnow():
    return datetime.datetime.now(datetime.timezone.utc)

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=True)
    full_name = Column(String(255), nullable=False)
    phone_number = Column(String(50), nullable=True)
    firebase_uid = Column(String(128), unique=True, index=True, nullable=True)
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)
    reset_token = Column(String(255), nullable=True)
    reset_token_expires = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=utcnow)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    # Relationships
    profile = relationship("UserProfile", back_populates="user", uselist=False, cascade="all, delete-orphan")
    resumes = relationship("Resume", back_populates="user", cascade="all, delete-orphan")
    skills = relationship("UserSkill", back_populates="user", cascade="all, delete-orphan")
    saved_jobs = relationship("SavedJob", back_populates="user", cascade="all, delete-orphan")
    applications = relationship("Application", back_populates="user", cascade="all, delete-orphan")
    matches = relationship("JobMatch", back_populates="user", cascade="all, delete-orphan")
    recommendations = relationship("Recommendation", back_populates="user", cascade="all, delete-orphan")
    notifications = relationship("Notification", back_populates="user", cascade="all, delete-orphan")
    notification_preference = relationship("NotificationPreference", back_populates="user", uselist=False, cascade="all, delete-orphan")


class UserProfile(Base):
    __tablename__ = "user_profiles"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    full_name = Column(String(255), nullable=True)
    email = Column(String(255), nullable=True)
    phone_number = Column(String(50), nullable=True)
    education_level = Column(String(100), nullable=True) # e.g., Bachelor's, Master's, High School
    college_university = Column(String(255), nullable=True)
    degree = Column(String(255), nullable=True)
    specialization = Column(String(255), nullable=True)
    graduation_year = Column(Integer, nullable=True)
    technical_skills = Column(JSON, default=list) # List[str]
    soft_skills = Column(JSON, default=list) # List[str]
    work_experience = Column(Text, nullable=True)
    experience_years = Column(Float, default=0.0)
    internship_experience = Column(Text, nullable=True)
    projects = Column(Text, nullable=True)
    certifications = Column(Text, nullable=True)
    preferred_job_role = Column(String(255), nullable=True)
    preferred_location = Column(String(255), nullable=True)
    expected_salary = Column(String(100), nullable=True)
    employment_type_preference = Column(String(100), nullable=True) # Full-time, Part-time, Remote, Contract, Internship
    is_confirmed_by_user = Column(Boolean, default=False)
    created_at = Column(DateTime, default=utcnow)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    user = relationship("User", back_populates="profile")


class Resume(Base):
    __tablename__ = "resumes"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    file_name = Column(String(255), nullable=False)
    file_path = Column(String(500), nullable=False)
    file_type = Column(String(50), nullable=False) # pdf, docx
    file_size = Column(Integer, nullable=False)
    parsed_text = Column(Text, nullable=True)
    parsed_data = Column(JSON, default=dict) # extracted skills, education, experience, keywords
    parse_status = Column(String(50), default="PENDING") # PENDING, SUCCESS, FAILED
    parsing_errors = Column(Text, nullable=True)
    created_at = Column(DateTime, default=utcnow)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    user = relationship("User", back_populates="resumes")


class UserSkill(Base):
    __tablename__ = "user_skills"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    skill_name = Column(String(100), nullable=False)
    normalized_name = Column(String(100), nullable=False, index=True)
    category = Column(String(50), default="technical") # technical or soft
    proficiency_level = Column(String(50), default="intermediate") # beginner, intermediate, advanced, expert
    is_verified_by_user = Column(Boolean, default=True)
    created_at = Column(DateTime, default=utcnow)

    user = relationship("User", back_populates="skills")
    __table_args__ = (UniqueConstraint("user_id", "normalized_name", name="uq_user_skill"),)


class JobSource(Base):
    __tablename__ = "job_sources"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), unique=True, nullable=False) # e.g. "Remotive", "Arbeitnow", "Adzuna"
    api_url = Column(String(500), nullable=True)
    is_active = Column(Boolean, default=True)
    last_synced_at = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=utcnow)

    jobs = relationship("Job", back_populates="source", cascade="all, delete-orphan")


class Job(Base):
    __tablename__ = "jobs"

    id = Column(Integer, primary_key=True, index=True)
    source_id = Column(Integer, ForeignKey("job_sources.id", ondelete="SET NULL"), nullable=True)
    external_id = Column(String(255), nullable=False, index=True)
    title = Column(String(255), nullable=False, index=True)
    company_name = Column(String(255), nullable=False, index=True)
    company_logo = Column(String(500), nullable=True)
    location = Column(String(255), nullable=True, index=True)
    employment_type = Column(String(100), nullable=True) # Full-time, Part-time, Contract, Internship
    experience_level = Column(String(100), nullable=True) # Entry-level, Mid-level, Senior
    experience_years_required = Column(Float, nullable=True)
    min_education = Column(String(100), nullable=True)
    description = Column(Text, nullable=False)
    salary = Column(String(150), nullable=True)
    publication_date = Column(DateTime, nullable=True)
    original_url = Column(String(1000), nullable=False)
    is_remote = Column(Boolean, default=False)
    is_india = Column(Boolean, default=False, index=True)
    is_active = Column(Boolean, default=True) # For tracking expired or removed jobs
    created_at = Column(DateTime, default=utcnow)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    source = relationship("JobSource", back_populates="jobs")
    required_skills = relationship("JobRequiredSkill", back_populates="job", cascade="all, delete-orphan")
    matches = relationship("JobMatch", back_populates="job", cascade="all, delete-orphan")
    recommendations = relationship("Recommendation", back_populates="job", cascade="all, delete-orphan")
    saved_jobs = relationship("SavedJob", back_populates="job", cascade="all, delete-orphan")
    applications = relationship("Application", back_populates="job", cascade="all, delete-orphan")

    __table_args__ = (UniqueConstraint("source_id", "external_id", name="uq_source_external_job"),)


class JobRequiredSkill(Base):
    __tablename__ = "job_required_skills"

    id = Column(Integer, primary_key=True, index=True)
    job_id = Column(Integer, ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False)
    skill_name = Column(String(100), nullable=False)
    normalized_name = Column(String(100), nullable=False, index=True)
    is_mandatory = Column(Boolean, default=True)

    job = relationship("Job", back_populates="required_skills")
    __table_args__ = (UniqueConstraint("job_id", "normalized_name", name="uq_job_skill"),)


class JobMatch(Base):
    __tablename__ = "job_matches"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    job_id = Column(Integer, ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False)
    overall_match_score = Column(Float, nullable=False) # 0 to 100
    skills_score = Column(Float, nullable=False)
    experience_score = Column(Float, nullable=False)
    education_score = Column(Float, nullable=False)
    location_preference_score = Column(Float, nullable=False)
    matching_skills = Column(JSON, default=list) # List[str]
    missing_skills = Column(JSON, default=list) # List[str]
    explanation_summary = Column(Text, nullable=False)
    detailed_breakdown = Column(JSON, default=dict)
    has_sufficient_info = Column(Boolean, default=True)
    calculated_at = Column(DateTime, default=utcnow)

    user = relationship("User", back_populates="matches")
    job = relationship("Job", back_populates="matches")

    __table_args__ = (UniqueConstraint("user_id", "job_id", name="uq_user_job_match"),)


class Recommendation(Base):
    __tablename__ = "recommendations"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    job_id = Column(Integer, ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False)
    match_score = Column(Float, nullable=False)
    recommendation_reason = Column(Text, nullable=False)
    rank_order = Column(Integer, default=1)
    is_dismissed = Column(Boolean, default=False)
    created_at = Column(DateTime, default=utcnow)

    user = relationship("User", back_populates="recommendations")
    job = relationship("Job", back_populates="recommendations")

    __table_args__ = (UniqueConstraint("user_id", "job_id", name="uq_user_job_recommendation"),)


class SavedJob(Base):
    __tablename__ = "saved_jobs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    job_id = Column(Integer, ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False)
    notes = Column(Text, nullable=True)
    saved_at = Column(DateTime, default=utcnow)

    user = relationship("User", back_populates="saved_jobs")
    job = relationship("Job", back_populates="saved_jobs")

    __table_args__ = (UniqueConstraint("user_id", "job_id", name="uq_user_saved_job"),)


class Application(Base):
    __tablename__ = "applications"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    job_id = Column(Integer, ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False)
    status = Column(String(50), default="Applied") # Interested, Applied, Interview, Rejected, Offer
    notes = Column(Text, nullable=True)
    applied_date = Column(DateTime, default=utcnow)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    user = relationship("User", back_populates="applications")
    job = relationship("Job", back_populates="applications")

    __table_args__ = (UniqueConstraint("user_id", "job_id", name="uq_user_job_application"),)


class Notification(Base):
    __tablename__ = "notifications"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    title = Column(String(255), nullable=False)
    message = Column(Text, nullable=False)
    notification_type = Column(String(50), default="SYSTEM") # JOB_MATCH, RECOMMENDATION, APPLICATION_UPDATE, SYSTEM
    reference_id = Column(Integer, nullable=True) # e.g. job_id or application_id
    is_read = Column(Boolean, default=False)
    created_at = Column(DateTime, default=utcnow)

    user = relationship("User", back_populates="notifications")


class NotificationPreference(Base):
    __tablename__ = "notification_preferences"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    email_notifications = Column(Boolean, default=True)
    push_notifications = Column(Boolean, default=True)
    job_matches_enabled = Column(Boolean, default=True)
    application_updates_enabled = Column(Boolean, default=True)
    recommendations_enabled = Column(Boolean, default=True)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    user = relationship("User", back_populates="notification_preference")
