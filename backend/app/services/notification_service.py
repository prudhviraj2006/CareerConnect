from sqlalchemy.orm import Session
from datetime import datetime
import logging
from typing import Optional

from backend.app.models.all_models import Notification, NotificationPreference, User

logger = logging.getLogger(__name__)

class NotificationService:

    @staticmethod
    def get_or_create_preferences(db: Session, user_id: int) -> NotificationPreference:
        prefs = db.query(NotificationPreference).filter(NotificationPreference.user_id == user_id).first()
        if not prefs:
            prefs = NotificationPreference(
                user_id=user_id,
                email_notifications=True,
                push_notifications=True,
                job_matches_enabled=True,
                application_updates_enabled=True,
                recommendations_enabled=True
            )
            db.add(prefs)
            db.commit()
            db.refresh(prefs)
        return prefs

    @classmethod
    def send_notification(
        cls,
        db: Session,
        user_id: int,
        title: str,
        message: str,
        notification_type: str = "SYSTEM",
        reference_id: Optional[int] = None
    ) -> Notification:
        """
        Creates an in-app notification honoring user notification preferences.
        """
        prefs = cls.get_or_create_preferences(db, user_id)

        # Check preferences
        if notification_type == "JOB_MATCH" and not prefs.job_matches_enabled:
            return None
        if notification_type == "RECOMMENDATION" and not prefs.recommendations_enabled:
            return None
        if notification_type == "APPLICATION_UPDATE" and not prefs.application_updates_enabled:
            return None

        notif = Notification(
            user_id=user_id,
            title=title,
            message=message,
            notification_type=notification_type,
            reference_id=reference_id,
            is_read=False,
            created_at=datetime.utcnow()
        )
        db.add(notif)
        db.commit()
        db.refresh(notif)
        return notif
