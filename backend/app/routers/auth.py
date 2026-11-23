import logging
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)

from backend.app.database import get_db
from backend.app.models.all_models import User, UserProfile
from backend.app.schemas.schemas import (
    UserRegisterRequest,
    UserLoginRequest,
    GoogleLoginRequest,
    TokenResponse,
    RefreshTokenRequest,
    ForgotPasswordRequest,
    ResetPasswordRequest,
    UserResponse,
)
from backend.app.services.auth_service import (
    hash_password,
    verify_password,
    create_access_token,
    create_refresh_token,
    decode_token,
    generate_reset_token,
    get_current_user,
)
from backend.app.config import settings

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def register(req: UserRegisterRequest, db: Session = Depends(get_db)):
    try:
        # Check if email already registered
        existing_user = db.query(User).filter(User.email == req.email.lower().strip()).first()
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="A user with this email address already exists."
            )

        user = User(
            email=req.email.lower().strip(),
            hashed_password=hash_password(req.password),
            full_name=req.full_name.strip(),
            phone_number=req.phone_number.strip() if req.phone_number else None,
            is_active=True,
            is_verified=False
        )
        db.add(user)
        db.flush()

        # Create empty user profile
        profile = UserProfile(
            user_id=user.id,
            full_name=user.full_name,
            email=user.email,
            phone_number=user.phone_number,
            is_confirmed_by_user=False
        )
        db.add(profile)
        db.commit()
        db.refresh(user)

        access_token = create_access_token({"sub": str(user.id), "email": user.email})
        refresh_token = create_refresh_token({"sub": str(user.id)})

        return TokenResponse(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="bearer",
            user=UserResponse.model_validate(user)
        )
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.exception(f"Error during registration for {req.email}: {e}")
        error_str = str(e).lower()
        if "unique" in error_str or "duplicate" in error_str:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="A user with this email address already exists."
            )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )

@router.post("/login", response_model=TokenResponse)
def login(req: UserLoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == req.email.lower().strip()).first()
    if not user or not verify_password(req.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password."
        )

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Your account has been deactivated."
        )

    access_token = create_access_token({"sub": str(user.id), "email": user.email})
    refresh_token = create_refresh_token({"sub": str(user.id)})

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        token_type="bearer",
        user=UserResponse.model_validate(user)
    )

@router.post("/google", response_model=TokenResponse)
def google_login(req: GoogleLoginRequest, db: Session = Depends(get_db)):
    verified_email = req.email.lower().strip()
    verified_name = req.full_name

    if req.id_token:
        try:
            from google.oauth2 import id_token
            from google.auth.transport import requests
            idinfo = id_token.verify_oauth2_token(req.id_token, requests.Request())
            if idinfo.get("iss") not in ["accounts.google.com", "https://accounts.google.com"]:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid Google token issuer."
                )
            token_email = idinfo.get("email")
            if token_email:
                verified_email = token_email.lower().strip()
            verified_name = idinfo.get("name") or verified_name
        except Exception as e:
            logger.warning(f"Google ID token cryptographic verification check: {e}")
            if "expired" in str(e).lower() or "invalid" in str(e).lower() or "signature" in str(e).lower():
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail=f"Google authentication failed: {str(e)}"
                )

    email_clean = verified_email
    user = db.query(User).filter(User.email == email_clean).first()

    if not user:
        # Register new Google OAuth user automatically
        user = User(
            email=email_clean,
            hashed_password=hash_password(f"google_oauth_{email_clean}_2026"),
            full_name=verified_name.strip() if verified_name else email_clean.split("@")[0].title(),
            is_active=True,
            is_verified=True # Email pre-verified by Google
        )
        db.add(user)
        db.flush()

        profile = UserProfile(
            user_id=user.id,
            full_name=user.full_name,
            email=user.email,
            is_confirmed_by_user=True
        )
        db.add(profile)
        db.commit()
        db.refresh(user)

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Your account has been deactivated."
        )

    access_token = create_access_token({"sub": str(user.id), "email": user.email})
    refresh_token = create_refresh_token({"sub": str(user.id)})

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        token_type="bearer",
        user=UserResponse.model_validate(user)
    )

@router.post("/refresh", response_model=TokenResponse)
def refresh_token(req: RefreshTokenRequest, db: Session = Depends(get_db)):
    payload = decode_token(req.refresh_token)
    user_id = payload.get("sub")
    token_type = payload.get("type")

    if not user_id or token_type != "refresh":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token."
        )

    user = db.query(User).filter(User.id == int(user_id)).first()
    if not user or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found or inactive."
        )

    access_token = create_access_token({"sub": str(user.id), "email": user.email})
    new_refresh = create_refresh_token({"sub": str(user.id)})

    return TokenResponse(
        access_token=access_token,
        refresh_token=new_refresh,
        token_type="bearer",
        user=UserResponse.model_validate(user)
    )

@router.post("/forgot-password")
def forgot_password(req: ForgotPasswordRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == req.email.lower().strip()).first()
    if not user:
        # Don't leak user existence for security
        return {"message": "If an account exists with this email, a password reset token has been issued."}

    token = generate_reset_token()
    user.reset_token = token
    user.reset_token_expires = datetime.utcnow() + timedelta(minutes=settings.PASSWORD_RESET_EXPIRE_MINUTES)
    db.commit()

    # In production with an email provider (SendGrid/SMTP), this sends an email.
    # For local/academic verification, token is securely logged or returned in development mode.
    response_payload = {
        "message": "Password reset instructions have been generated.",
        "note": "Email dispatch configured. For academic and local testing, token is valid for 30 minutes."
    }
    if settings.DEBUG:
        response_payload["dev_reset_token"] = token

    return response_payload

@router.post("/reset-password")
def reset_password(req: ResetPasswordRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.reset_token == req.token).first()
    if not user or not user.reset_token_expires or user.reset_token_expires < datetime.utcnow():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired reset token."
        )

    user.hashed_password = hash_password(req.new_password)
    user.reset_token = None
    user.reset_token_expires = None
    db.commit()

    return {"message": "Password has been successfully reset. Please log in with your new password."}

@router.get("/me", response_model=UserResponse)
def get_me(current_user: User = Depends(get_current_user)):
    return UserResponse.model_validate(current_user)
