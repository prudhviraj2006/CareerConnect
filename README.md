# CareerConnect – Intelligent Skill-Based Job Matching Application

[![Python 3.10+](https://img.shields.io/badge/Python-3.10%2B-blue.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/Backend-FastAPI-009688.svg)](https://fastapi.tiangolo.com/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![UN SDG 8](https://img.shields.io/badge/UN%20SDG-8%3A%20Decent%20Work%20%26%20Economic%20Growth-E5243B.svg)](https://sdgs.un.org/goals/goal8)

> **CareerConnect** is an academic-grade, production-style, end-to-end mobile and backend platform designed to advance **United Nations Sustainable Development Goal 8 (Decent Work and Economic Growth)**. It provides fair, transparent, and explainable job matching by directly comparing verified candidate skills against live job market requirements retrieved from genuine, authorized job providers.

---

## Architecture Overview

```
                               +---------------------------------------------------+
                               |              CareerConnect Android App            |
                               |    (Kotlin, Jetpack Compose, Material 3, MVVM)    |
                               +---------------------------------------------------+
                                        |                             |
                       Preferences DataStore (JWT)              Room Local DB
                                        |                             |
                                        +--------------+--------------+
                                                       |
                                            Retrofit 2 + AuthInterceptor
                                                       |
                                                       v
                               +---------------------------------------------------+
                               |              FastAPI Backend Service              |
                               |     (Python 3.10+, SQLAlchemy 2.0 ORM, PyJWT)     |
                               +---------------------------------------------------+
                                        |                             |
                                PostgreSQL Database           Resume Extraction
                           (Users, Profiles, Matches, Jobs)   (pypdf, python-docx)
                                        |
                         +--------------+--------------+
                         |                             |
                         v                             v
           +---------------------------+ +---------------------------+
           |       Remotive API        | |       Arbeitnow API       |
           | (Authorized Remote Jobs)  | |  (Verified Direct Jobs)   |
           +---------------------------+ +---------------------------+
```

---

## 1. Features & Academic Module Implementations

### Module 1 – User Profile & Resume Analysis
- **Authentication**: Registration with email, password hashing (Bcrypt), JWT access & refresh tokens, password reset token flow.
- **Candidate Profile**: Education level, college/university, degree, specialization, graduation year, technical skills, soft skills, work experience, internship experience, preferred job role, location, expected salary, and employment type.
- **Resume Upload & Parser**: Supports PDF and DOCX documents (<= 10MB). Text extraction using `pypdf` and `python-docx` paired with a canonical skills taxonomy.
- **Academic Transparency**: Clearly labels extracted data as rule-based keyword extraction (never claiming an opaque AI blackbox). All extracted data must be reviewed and confirmed by the candidate before saving.

### Module 2 – Genuine Job Integration & Skill-Based Matching
- **Authorized Provider Integration**:
  - **Remotive API** (live public API, remote technical jobs).
  - **Arbeitnow API** (live public API, verified listings).
  - **Adzuna API** (pluggable integration for configured credentials).
- **Zero Fabrication**: Only genuine listings from authorized sources.
- **Matching Engine Formula**:
  $$\text{Score} = \text{Skills (60\%)} + \text{Experience (20\%)} + \text{Education (10\%)} + \text{Preferences (10\%)}$$
- **Explainability**: Every match score details matching skills, missing skills, and a transparent natural-language rationale.

### Module 3 – Recommendations, Skill Gap & Notifications
- **Personalized Recommendations**: Dynamically ranked top job listings based on verified profile compatibility.
- **Skill Gap Analysis**: Shows exact skills the candidate has vs. missing required skills, along with actionable improvement advice and potential percentage compatibility boost.
- **Candidate Application Tracking**: Candidate-managed application status board (`Interested`, `Applied`, `Interview`, `Rejected`, `Offer`).
- **In-App Notifications**: Real-time alerts for new job matches, recommendation updates, and application status changes.

---

## 2. Project Directory Structure

```
CareerConnect/
├── android-app/                       # Complete Android Studio Project
│   ├── app/
│   │   ├── build.gradle.kts          # Dependencies (Compose, Retrofit, Room, DataStore)
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml   # Permissions, network security config, launch activity
│   │   │   ├── java/com/careerconnect/app/
│   │   │   │   ├── data/             # API, Room Local DB, Models, Preferences, Repositories
│   │   │   │   ├── ui/               # 22 Compose Screens, Navigation, Theme, Components, ViewModels
│   │   │   │   ├── CareerConnectApp.kt
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/                  # Drawables, mipmaps, strings, colors, themes, xml
│   │   └── src/test/                 # Unit tests (MatchingAlgorithmUnitTest)
│   ├── build.gradle.kts              # Root build script
│   ├── settings.gradle.kts           # Settings & repositories
│   ├── gradle.properties             # JVM arguments & AndroidX flags
│   ├── gradlew & gradlew.bat         # Gradle wrapper executables
│   └── gradle/wrapper/               # Gradle wrapper jar and properties
│
├── backend/                           # Complete FastAPI Backend Service
│   ├── app/
│   │   ├── config.py                 # Pydantic Settings & environment variables
│   │   ├── database.py               # SQLAlchemy engine & sessionmaker
│   │   ├── main.py                   # FastAPI application instance & lifespan
│   │   ├── models/                   # PostgreSQL SQLAlchemy ORM models (13 tables)
│   │   ├── schemas/                  # Pydantic request and response schemas
│   │   ├── routers/                  # Modular API routers (Auth, Profile, Resume, Jobs, etc.)
│   │   ├── services/                 # AuthService, ResumeParser, JobSync, Matching, Notifications
│   │   └── tests/                    # Pytest test suite (100% passing)
│   ├── uploads/resumes/              # Secure document uploads storage
│   ├── requirements.txt              # Python package dependencies
│   └── .env                          # Local environment settings
│
├── database/                          # Database Schemas & Migrations
│   ├── schema.sql                    # Production PostgreSQL DDL schema with indexes and cascades
│   └── seed.sql                      # Initial provider seed records
│
├── docs/                              # Technical & Academic Documentation
│   ├── ARCHITECTURE.md               # System architecture & component interaction
│   ├── API_DOCUMENTATION.md          # REST API endpoints & payload specifications
│   ├── MATCHING_ALGORITHM.md         # Formula, weight distribution, and explainability engine
│   ├── JOB_PROVIDERS_GUIDE.md        # Provider integration & setup guide
│   ├── DATABASE_SCHEMA.md            # Schema design & PostgreSQL migration guide
│   └── TESTING_AND_VERIFICATION_REPORT.md # Verification matrix & test results
│
├── .env.example                       # Reference environment variables
└── README.md                          # Master documentation
```

---

## 3. 22 Android Screens Implemented

1. **SplashScreen**: App branding, SDG 8 banner, session detection.
2. **OnboardingScreen**: 3-step carousel explaining skill-based matching, resume parsing, and SDG 8.
3. **RegisterScreen**: Candidate registration with name, email, phone, and password validation.
4. **LoginScreen**: Secure login with JWT authentication.
5. **ForgotPasswordScreen**: Token-based password recovery.
6. **ProfileSetupScreen**: Initial profile onboarding (education, skills, preferences).
7. **ResumeUploadScreen**: Document picker for PDF/DOCX resumes (<= 10MB) with parsing progress.
8. **ResumeReviewScreen**: Review and correct extracted skills, education, and experience.
9. **HomeScreen**: Dashboard with welcome hero, stats, top recommendations carousel, latest genuine jobs, and sync button.
10. **JobSearchScreen**: Search by title, company, or skill tags with filter indicators.
11. **JobFiltersScreen**: Filter by location, remote, experience level, employment type, and sort order.
12. **JobListingScreen**: Dedicated paginated job listings with pull-to-refresh.
13. **JobDetailsScreen**: Detailed view with source badge, required skills, and genuine external application URL intent.
14. **MatchScoreDetailsScreen**: Overall compatibility gauge and 60/20/10/10 component score breakdown.
15. **RecommendedJobsScreen**: Ranked recommendation cards with match explanations.
16. **SkillGapScreen**: Possessed skills vs. missing skills with boost potential percentages.
17. **SavedJobsScreen**: Candidate bookmarked job listings.
18. **ApplicationTrackingScreen**: Application status board (`Interested`, `Applied`, `Interview`, `Rejected`, `Offer`).
19. **NotificationsScreen**: In-app alerts with unread indicators and mark-all-read action.
20. **UserProfileScreen**: Candidate verified profile overview with verified skills chips.
21. **EditProfileScreen**: Comprehensive profile editing form.
22. **SettingsScreen**: Notification toggles, backend server URL configuration, and logout.

---

## 4. Running CareerConnect Without a Laptop (Phone-Only & Cloud Operation)

CareerConnect is built to run directly on an Android smartphone with **zero dependency on a laptop or local computer**:

### Option 1: Direct Phone Installation & Standalone Mode (Zero Server / Zero Laptop Required)
1. Transfer `apk/CareerConnect-debug.apk` directly to your Android phone (via WhatsApp, Google Drive, Telegram, or USB).
2. Tap the APK file on your phone and install it.
3. Open the app on your phone.
4. On the Login screen, tap **`[ Run on Phone (No Laptop Required) ]`**.
5. The app immediately launches:
   - Sets up verified candidate profile **Alex Chen** (B.Tech Computer Science).
   - Pre-seeds genuine tech opportunities into the on-device Room database.
   - Evaluates real 60/20/10/10 skill matching scores and explains reasons.
   - Computes skill-gap analysis and score boost potentials on the device.
   - Allows uploading and parsing PDF/DOCX resumes directly from phone storage.
   - Saves jobs, tracks applications through all 5 stages, and displays notifications.
   - **Requires 0% laptop interaction.**

### Option 2: 24/7 Cloud Deployment (Render, Railway, or Koyeb)
To run the full FastAPI backend and PostgreSQL database on the cloud 24/7 for multiple users:
1. Push this repository to GitHub.
2. Log in to [Render.com](https://render.com) (free tier, no credit card required) and select **New + Blueprint**.
3. Render automatically reads `backend/render.yaml` and spins up the PostgreSQL database and FastAPI web service.
4. Copy your live Render URL (e.g., `https://careerconnect-api.onrender.com/`).
5. Open CareerConnect on your phone $\rightarrow$ **Settings** $\rightarrow$ enter your URL $\rightarrow$ tap **Save Server URL**.
6. Full guide in [docs/CLOUD_DEPLOYMENT_GUIDE.md](docs/CLOUD_DEPLOYMENT_GUIDE.md).

---

## 5. Local Setup and Execution Instructions (Optional for Developers)

### A. Backend Setup
1. **Navigate to the backend directory**:
   ```bash
   cd backend
   ```
2. **Install Python dependencies**:
   ```bash
   pip install -r requirements.txt
   ```
3. **Start the FastAPI backend**:
   ```bash
   uvicorn backend.app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
4. **Access API Documentation**:
   - Interactive Swagger UI: `http://localhost:8000/docs`
   - ReDoc UI: `http://localhost:8000/redoc`

### B. PostgreSQL Database Setup (Production)
1. Create database:
   ```bash
   createdb -U postgres careerconnect_db
   ```
2. Run schema and seed scripts:
   ```bash
   psql -U postgres -d careerconnect_db -f database/schema.sql
   psql -U postgres -d careerconnect_db -f database/seed.sql
   ```
3. Update `DATABASE_URL` in `.env`:
   ```env
   DATABASE_URL=postgresql://postgres:password@localhost:5432/careerconnect_db
   ```

### C. Android App Setup
1. Open Android Studio and choose **Open**, selecting the `CareerConnect/android-app` folder.
2. Allow Gradle to sync.
3. **Connecting Android App to Backend**:
   - If running on **Android Emulator**, the app automatically connects to `http://10.0.2.2:8000/`.
   - If running on a **Physical Android Device**, enter your development machine's local Wi-Fi IP (e.g. `http://192.168.1.50:8000/`) in the **Settings** screen inside the app!
4. **Run or Build APK**:
   ```bash
   cd android-app
   .\gradlew.bat assembleDebug
   ```

---

## 5. Verification & Test Execution Results

Run all backend automated tests:
```bash
python -m pytest backend/app/tests/test_backend.py -v
```

**Results:**
- `test_skill_normalization`: **PASSED**
- `test_resume_text_parser`: **PASSED**
- `test_auth_workflow`: **PASSED**
- `test_skill_matching_algorithm`: **PASSED**
- `test_saved_jobs_and_applications`: **PASSED**

---

## 6. Academic Compliance Checklist
- [x] Complete working application (frontend, backend, database, matching, resume processing).
- [x] Genuine job data integration (Remotive and Arbeitnow authorized providers).
- [x] Objective, non-random match score algorithm based on 60/20/10/10 configurable weights.
- [x] Full skill gap analysis with compatibility boost potential.
- [x] Full 22 Material 3 Compose screens with 5-tab bottom navigation.
- [x] Transparent resume processing with user review and confirmation before profile changes.
- [x] PostgreSQL production schema DDL, indexes, cascades, and Alembic migrations provided.
- [x] Complete documentation, testing matrix, and clean project structure.
