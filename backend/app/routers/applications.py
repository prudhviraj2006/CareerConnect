from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime

from backend.app.database import get_db
from backend.app.models.all_models import Application, Job, User
from backend.app.schemas.schemas import ApplicationCreate, ApplicationUpdate, ApplicationResponse, JobResponse
from backend.app.services.auth_service import get_current_user
from backend.app.services.notification_service import NotificationService
from backend.app.routers.jobs import serialize_job

router = APIRouter(prefix="/api/applications", tags=["Applications"])

VALID_STATUSES = {"Interested", "Applied", "Interview", "Rejected", "Offer"}

@router.post("", response_model=ApplicationResponse, status_code=status.HTTP_201_CREATED)
def create_application_track(
    req: ApplicationCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if req.status not in VALID_STATUSES:
        raise HTTPException(status_code=400, detail=f"Invalid status. Must be one of {VALID_STATUSES}")

    job = db.query(Job).filter(Job.id == req.job_id).first()
    if not job:
        raise HTTPException(status_code=404, detail="Job not found.")

    app = db.query(Application).filter(
        Application.user_id == current_user.id,
        Application.job_id == req.job_id
    ).first()

    if app:
        app.status = req.status
        if req.notes is not None:
            app.notes = req.notes
        app.updated_at = datetime.utcnow()
    else:
        app = Application(
            user_id=current_user.id,
            job_id=req.job_id,
            status=req.status,
            notes=req.notes
        )
        db.add(app)

    db.commit()
    db.refresh(app)

    # In-app notification for status tracking
    NotificationService.send_notification(
        db=db,
        user_id=current_user.id,
        title="Application Tracked",
        message=f"You updated status for {job.title} at {job.company_name} to '{req.status}'.",
        notification_type="APPLICATION_UPDATE",
        reference_id=job.id
    )

    return ApplicationResponse(
        id=app.id,
        job_id=app.job_id,
        job=JobResponse(**serialize_job(job, user_id=current_user.id, db=db)),
        status=app.status,
        notes=app.notes,
        applied_date=app.applied_date,
        updated_at=app.updated_at
    )

@router.get("", response_model=List[ApplicationResponse])
def get_applications(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    apps = db.query(Application).join(Job).filter(
        Application.user_id == current_user.id
    ).order_by(Application.updated_at.desc()).all()

    output = []
    for a in apps:
        output.append(
            ApplicationResponse(
                id=a.id,
                job_id=a.job_id,
                job=JobResponse(**serialize_job(a.job, user_id=current_user.id, db=db)),
                status=a.status,
                notes=a.notes,
                applied_date=a.applied_date,
                updated_at=a.updated_at
            )
        )
    return output

@router.put("/{app_id}", response_model=ApplicationResponse)
def update_application_status(
    app_id: int,
    req: ApplicationUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if req.status not in VALID_STATUSES:
        raise HTTPException(status_code=400, detail=f"Invalid status. Must be one of {VALID_STATUSES}")

    app = db.query(Application).filter(
        Application.id == app_id,
        Application.user_id == current_user.id
    ).first()

    if not app:
        raise HTTPException(status_code=404, detail="Application record not found.")

    old_status = app.status
    app.status = req.status
    if req.notes is not None:
        app.notes = req.notes
    app.updated_at = datetime.utcnow()
    db.commit()
    db.refresh(app)

    NotificationService.send_notification(
        db=db,
        user_id=current_user.id,
        title="Application Status Updated",
        message=f"Application for {app.job.title} changed from '{old_status}' to '{req.status}'.",
        notification_type="APPLICATION_UPDATE",
        reference_id=app.job_id
    )

    return ApplicationResponse(
        id=app.id,
        job_id=app.job_id,
        job=JobResponse(**serialize_job(app.job, user_id=current_user.id, db=db)),
        status=app.status,
        notes=app.notes,
        applied_date=app.applied_date,
        updated_at=app.updated_at
    )
