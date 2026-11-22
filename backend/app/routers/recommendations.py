from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from backend.app.database import get_db
from backend.app.models.all_models import Recommendation, Job, User
from backend.app.schemas.schemas import RecommendationResponse, JobResponse
from backend.app.services.auth_service import get_current_user
from backend.app.services.skill_matching_service import SkillMatchingService
from backend.app.routers.jobs import serialize_job

router = APIRouter(prefix="/api/recommendations", tags=["Recommendations"])

@router.get("", response_model=List[RecommendationResponse])
def get_recommendations(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    # Check if user has recommendations cached
    recs = db.query(Recommendation).join(Job).filter(
        Recommendation.user_id == current_user.id,
        Job.is_active == True,
        Recommendation.is_dismissed == False
    ).order_by(Recommendation.rank_order.asc()).all()

    if not recs:
        # Compute dynamically
        SkillMatchingService.update_matches_for_user(db, current_user.id)
        recs = db.query(Recommendation).join(Job).filter(
            Recommendation.user_id == current_user.id,
            Job.is_active == True,
            Recommendation.is_dismissed == False
        ).order_by(Recommendation.rank_order.asc()).all()

    output = []
    for r in recs:
        serialized_job = serialize_job(r.job, user_id=current_user.id, db=db)
        serialized_job["match_score"] = r.match_score
        output.append(
            RecommendationResponse(
                id=r.id,
                job=JobResponse(**serialized_job),
                match_score=r.match_score,
                recommendation_reason=r.recommendation_reason,
                rank_order=r.rank_order,
                created_at=r.created_at
            )
        )

    return output
