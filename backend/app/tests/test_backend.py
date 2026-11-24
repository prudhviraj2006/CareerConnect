import pytest
from backend.app.services.resume_parser import ResumeParserService, normalize_skill
from backend.app.services.skill_matching_service import SkillMatchingService
from backend.app.models.all_models import Job, JobSource, JobRequiredSkill, UserProfile, User

def test_skill_normalization():
    assert normalize_skill("Android Development") == "android"
    assert normalize_skill("Jetpack Compose") == "jetpack compose"
    assert normalize_skill("Postgres") == "postgresql"
    assert normalize_skill("K8s") == "kubernetes"

def test_resume_text_parser():
    sample_resume = """
    Alex Morgan
    Email: alex.morgan@example.com
    Phone: (555) 123-4567

    EDUCATION
    Bachelor of Technology in Computer Science, 2023
    University of Engineering

    EXPERIENCE
    Software Engineer with 2.5 years of experience in Android and Backend systems.
    - Built mobile apps using Kotlin, Jetpack Compose, Retrofit, and Room Database.
    - Developed backend microservices using Python, FastAPI, and PostgreSQL.
    - Practiced Agile and CI/CD with Git.

    SKILLS
    Kotlin, Jetpack Compose, Android, Python, FastAPI, PostgreSQL, Git, Communication, Teamwork
    """
    parsed = ResumeParserService.parse_resume_content(sample_resume)
    assert parsed["email"] == "alex.morgan@example.com"
    assert parsed["phone"] == "(555) 123-4567"
    assert parsed["candidate_name"] == "Alex Morgan"
    assert parsed["education_level"] == "Bachelor's Degree"
    assert parsed["graduation_year"] == 2023
    assert parsed["experience_years"] == 2.5
    assert "Kotlin" in parsed["extracted_skills"]
    assert "Fastapi" in parsed["extracted_skills"]
    assert "Postgresql" in parsed["extracted_skills"]
    assert "Communication" in parsed["extracted_soft_skills"]
    assert parsed["is_ai_generated"] is False

def test_auth_workflow(client):
    # 1. Register
    reg_resp = client.post("/api/auth/register", json={
        "email": "testuser@careerconnect.org",
        "password": "StrongPassword123!",
        "full_name": "Test Candidate",
        "phone_number": "+1234567890"
    })
    assert reg_resp.status_code == 201
    tokens = reg_resp.json()
    assert "access_token" in tokens
    assert tokens["user"]["email"] == "testuser@careerconnect.org"

    # 2. Duplicate registration should fail
    dup_resp = client.post("/api/auth/register", json={
        "email": "testuser@careerconnect.org",
        "password": "StrongPassword123!",
        "full_name": "Test Candidate"
    })
    assert dup_resp.status_code == 400

    # 3. Login
    login_resp = client.post("/api/auth/login", json={
        "email": "testuser@careerconnect.org",
        "password": "StrongPassword123!"
    })
    assert login_resp.status_code == 200
    access_token = login_resp.json()["access_token"]
    headers = {"Authorization": f"Bearer {access_token}"}

    # 4. Profile
    prof_resp = client.get("/api/profile", headers=headers)
    assert prof_resp.status_code == 200
    assert prof_resp.json()["full_name"] == "Test Candidate"

    # 5. Update profile
    update_resp = client.put("/api/profile", headers=headers, json={
        "education_level": "Bachelor's Degree",
        "college_university": "National Tech University",
        "degree": "B.Tech Computer Science",
        "specialization": "Software Engineering",
        "graduation_year": 2024,
        "technical_skills": ["Kotlin", "Android", "Jetpack Compose", "FastAPI"],
        "soft_skills": ["Teamwork", "Problem Solving"],
        "experience_years": 2.0,
        "preferred_job_role": "Android Developer",
        "preferred_location": "Remote",
        "employment_type_preference": "Full-time"
    })
    assert update_resp.status_code == 200
    assert "Kotlin" in update_resp.json()["technical_skills"]

def test_skill_matching_algorithm(db_session):
    # Setup candidate profile
    profile = UserProfile(
        user_id=1,
        full_name="Jane Doe",
        education_level="Bachelor's Degree",
        experience_years=3.0,
        preferred_location="Remote",
        employment_type_preference="Full-time",
        technical_skills=["Kotlin", "Android", "Jetpack Compose", "Room Database"],
        soft_skills=["Communication"]
    )

    # Setup Job with 4 required skills (3 matched, 1 missing)
    job = Job(
        id=999,
        title="Senior Android Engineer",
        company_name="Tech Corp",
        location="Remote",
        employment_type="Full-time",
        experience_level="Mid-level",
        experience_years_required=2.0,
        min_education="Bachelor's Degree",
        description="Looking for an experienced Android engineer with Kotlin, Jetpack Compose, Room, and Coroutines.",
        original_url="https://example.com/jobs/999",
        is_remote=True,
        is_active=True
    )

    # Attach required skills
    job.required_skills = [
        JobRequiredSkill(job_id=999, skill_name="Kotlin", normalized_name="kotlin"),
        JobRequiredSkill(job_id=999, skill_name="Jetpack Compose", normalized_name="jetpack compose"),
        JobRequiredSkill(job_id=999, skill_name="Room Database", normalized_name="room database"),
        JobRequiredSkill(job_id=999, skill_name="Coroutines", normalized_name="coroutines"),
    ]

    match = SkillMatchingService.calculate_match(profile, job)
    assert match["has_sufficient_info"] is True
    # 3 out of 4 skills matched = 75% of 60 points = 45 points
    assert match["skills_score"] == 45.0
    # Candidate has 3 yrs vs 2 yrs required = full 20 points
    assert match["experience_score"] == 20.0
    # Education matched Bachelor's = full 10 points
    assert match["education_score"] == 10.0
    # Remote + Full-time matched = full 10 points
    assert match["location_preference_score"] == 10.0
    # Total overall score = 45 + 20 + 10 + 10 = 85.0
    assert match["overall_match_score"] == 85.0
    assert "Coroutines" in match["missing_skills"]
    assert "Kotlin" in match["matching_skills"]
    assert match["recommendation_tier"] == "High"

def test_saved_jobs_and_applications(client):
    # Register candidate
    reg = client.post("/api/auth/register", json={
        "email": "cand2@careerconnect.org",
        "password": "Password123!",
        "full_name": "Candidate Two"
    })
    token = reg.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # First trigger job sync or insert a job in the test DB
    sync_resp = client.post("/api/jobs/sync?limit_per_provider=5")
    assert sync_resp.status_code == 200

    jobs = client.get("/api/jobs").json()
    if len(jobs) > 0:
        first_job_id = jobs[0]["id"]

        # Save job
        save_resp = client.post(f"/api/saved-jobs/{first_job_id}", headers=headers)
        assert save_resp.status_code in [200, 201]

        # List saved jobs
        saved_list = client.get("/api/saved-jobs", headers=headers).json()
        assert len(saved_list) >= 1
        assert saved_list[0]["job"]["id"] == first_job_id

        # Track application
        app_resp = client.post("/api/applications", headers=headers, json={
            "job_id": first_job_id,
            "status": "Applied",
            "notes": "Applied via official employer portal."
        })
        assert app_resp.status_code == 201
        assert app_resp.json()["status"] == "Applied"

        # Update application status
        app_id = app_resp.json()["id"]
        upd_app = client.put(f"/api/applications/{app_id}", headers=headers, json={
            "status": "Interview",
            "notes": "Invited for technical interview."
        })
        assert upd_app.status_code == 200
        assert upd_app.json()["status"] == "Interview"

        # Check notifications
        notifs = client.get("/api/notifications", headers=headers).json()
        assert len(notifs) >= 1

def test_firebase_auth_endpoint(client, monkeypatch):
    from backend.app.routers import auth
    # Mock verify_firebase_id_token
    monkeypatch.setattr(auth, "verify_firebase_id_token", lambda token: {
        "uid": "fb_uid_123456789",
        "email": "firebaseuser@careerconnect.org",
        "name": "Firebase User",
        "email_verified": True
    })

    # Call /api/auth/firebase
    response = client.post("/api/auth/firebase", json={
        "firebase_token": "valid_mocked_token",
        "email": "firebaseuser@careerconnect.org",
        "full_name": "Firebase User"
    })
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert data["user"]["email"] == "firebaseuser@careerconnect.org"
    assert data["user"]["full_name"] == "Firebase User"
