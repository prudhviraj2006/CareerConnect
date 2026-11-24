import os
from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    APP_NAME: str = "CareerConnect"
    APP_ENV: str = "development"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = True

    # Database
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./careerconnect.db")

    # JWT Security
    SECRET_KEY: str = os.getenv("SECRET_KEY", "careerconnect-super-secure-production-ready-secret-key-32chars")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440 # 24 hours
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    PASSWORD_RESET_EXPIRE_MINUTES: int = 30

    # CORS
    ALLOWED_ORIGINS: str = "*"

    # Uploads
    UPLOAD_DIR: str = os.getenv("UPLOAD_DIR", "./uploads/resumes")
    MAX_UPLOAD_SIZE_MB: int = 10

    # Job Providers
    ENABLE_REMOTIVE: bool = True
    ENABLE_ARBEITNOW: bool = True
    ADZUNA_APP_ID: str = os.getenv("ADZUNA_APP_ID", "")
    ADZUNA_APP_KEY: str = os.getenv("ADZUNA_APP_KEY", "")
    ADZUNA_COUNTRY: str = os.getenv("ADZUNA_COUNTRY", "in")

    # Push Notification & Firebase Service
    FCM_SERVER_KEY: str = ""
    FIREBASE_PROJECT_ID: str = os.getenv("FIREBASE_PROJECT_ID", "careerconnect-4974b")
    FIREBASE_CREDENTIALS_PATH: str = os.getenv("FIREBASE_CREDENTIALS_PATH", "")

    class Config:
        env_file = ".env"
        extra = "allow"

    @property
    def cors_origins(self) -> List[str]:
        if self.ALLOWED_ORIGINS == "*":
            return ["*"]
        return [origin.strip() for origin in self.ALLOWED_ORIGINS.split(",") if origin.strip()]

settings = Settings()
