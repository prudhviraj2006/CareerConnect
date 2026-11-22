from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from backend.app.database import get_db
from backend.app.models.all_models import SavedJob, Job, User
from backend.app.schemas.schemas import SavedJobResponse, SavedJobCreate, JobResponse
from backend.app.services.auth_service import get_current_user
from backend.app.routers.jobs import serialize_job

router = APIRouter(prefix="/api/saved-jobs", tags=["Saved Jobs"])

@router.post("/{job_id}", response_model=SavedJobResponse, status_code=status.HTTP_201_CREATED)
def save_job(
    job_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    job = db.query(Job).filter(Job.id == job_id).first()
    if not job:
        raise HTTPException(status_code=404, detail="Job not found.")

    saved = db.query(SavedJob).filter(
        SavedJob.user_id == current_user.id,
        SavedJob.job_id == job_id
    ).first()

    if saved:
        return SavedJobResponse(
            id=saved.id,
            job=JobResponse(**serialize_job(job, user_id=current_user.id, db=db)),
            notes=saved.notes,
            saved_at=saved.saved_at
        )

    saved = SavedJob(user_id=current_user.id, job_id=job_id)
    db.add(saved)
    db.commit()
    db.refresh(saved)

    return SavedJobResponse(
        id=saved.id,
        job=JobResponse(**serialize_job(job, user_id=current_user.id, db=db)),
        notes=saved.notes,
        saved_at=saved.saved_at
    )

@router.delete("/{job_id}", status_code=status.HTTP_200_OK)
def remove_saved_job(
    job_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    saved = db.query(SavedJob).filter(
        SavedJob.user_id == current_user.id,
        SavedJob.job_id == job_id
    ).first()

    if not saved:
        raise HTTPException(status_code=404, detail="Job is not in saved list.")

    db.delete(saved)
    db.commit()
    return {"message": "Job removed from saved list."}

@router.get("", response_model=List[SavedJobResponse])
def get_saved_jobs(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    saved_list = db.query(SavedJob).join(Job).filter(
        SavedJob.user_id == current_user.id
    ).order_by(SavedJob.saved_at.desc()).all()

    output = []
    for s in saved_list:
        output.append(
            SavedJobResponse(
                id=s.id,
                job=JobResponse(**serialize_job(s.job, user_id=current_user.id, db=db)),
                notes=s.notes,
                saved_at=s.saved_at
            )
        )
    return output
