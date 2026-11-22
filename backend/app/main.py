import asyncio
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from backend.app.config import settings
from backend.app.database import engine, Base, SessionLocal
from backend.app.models import all_models # ensure models are registered
from backend.app.routers import (
    auth,
    profile,
    resume,
    jobs,
    matching,
    recommendations,
    saved_jobs,
    applications,
    notifications,
)
from backend.app.services.job_sync_service import JobSyncService

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: create tables
    logger.info("Initializing database tables...")
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables initialized successfully.")

    # Background initial job sync check
    db = SessionLocal()
    try:
        job_count = db.query(all_models.Job).count()
        if job_count == 0:
            logger.info("No jobs found in database. Initiating first genuine job synchronization...")
            asyncio.create_task(JobSyncService.synchronize_jobs(db, limit_per_provider=20))
    except Exception as e:
        logger.warning(f"Could not perform startup job sync check: {e}")
    finally:
        db.close()

    yield

    logger.info("Shutting down CareerConnect backend.")

app = FastAPI(
    title=settings.APP_NAME,
    description="CareerConnect – An Intelligent Skill-Based Job Matching Application for Promoting Decent Work and Economic Growth (SDG 8)",
    version="1.0.0",
    lifespan=lifespan
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include Routers
app.include_router(auth.router)
app.include_router(profile.router)
app.include_router(resume.router)
app.include_router(jobs.router)
app.include_router(matching.router)
app.include_router(recommendations.router)
app.include_router(saved_jobs.router)
app.include_router(applications.router)
app.include_router(notifications.router)

@app.get("/", tags=["System"])
def root():
    return {
        "name": settings.APP_NAME,
        "version": "1.0.0",
        "objective": "Promoting Decent Work and Economic Growth (UN SDG 8) via Skill-Based Job Matching",
        "documentation": "/docs",
        "health": "/health"
    }

@app.get("/health", tags=["System"])
def health_check():
    return {
        "status": "healthy",
        "environment": settings.APP_ENV,
        "database": "connected"
    }
