from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from backend.app.database import get_db
from backend.app.models.all_models import Notification, NotificationPreference, User
from backend.app.schemas.schemas import (
    NotificationResponse,
    NotificationPreferenceResponse,
    NotificationPreferenceUpdate
)
from backend.app.services.auth_service import get_current_user
from backend.app.services.notification_service import NotificationService

router = APIRouter(prefix="/api/notifications", tags=["Notifications"])

@router.get("", response_model=List[NotificationResponse])
def get_notifications(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    notifs = db.query(Notification).filter(
        Notification.user_id == current_user.id
    ).order_by(Notification.created_at.desc()).limit(50).all()

    return [NotificationResponse.model_validate(n) for n in notifs]

@router.put("/{notif_id}/read", response_model=NotificationResponse)
def mark_notification_read(
    notif_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    notif = db.query(Notification).filter(
        Notification.id == notif_id,
        Notification.user_id == current_user.id
    ).first()

    if not notif:
        raise HTTPException(status_code=404, detail="Notification not found.")

    notif.is_read = True
    db.commit()
    db.refresh(notif)
    return NotificationResponse.model_validate(notif)

@router.put("/read-all")
def mark_all_notifications_read(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    db.query(Notification).filter(
        Notification.user_id == current_user.id,
        Notification.is_read == False
    ).update({"is_read": True})
    db.commit()
    return {"message": "All notifications marked as read."}

@router.get("/preferences", response_model=NotificationPreferenceResponse)
def get_preferences(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    prefs = NotificationService.get_or_create_preferences(db, current_user.id)
    return NotificationPreferenceResponse.model_validate(prefs)

@router.put("/preferences", response_model=NotificationPreferenceResponse)
def update_preferences(
    req: NotificationPreferenceUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    prefs = NotificationService.get_or_create_preferences(db, current_user.id)

    if req.email_notifications is not None:
        prefs.email_notifications = req.email_notifications
    if req.push_notifications is not None:
        prefs.push_notifications = req.push_notifications
    if req.job_matches_enabled is not None:
        prefs.job_matches_enabled = req.job_matches_enabled
    if req.application_updates_enabled is not None:
        prefs.application_updates_enabled = req.application_updates_enabled
    if req.recommendations_enabled is not None:
        prefs.recommendations_enabled = req.recommendations_enabled

    db.commit()
    db.refresh(prefs)
    return NotificationPreferenceResponse.model_validate(prefs)
