# CareerConnect Architecture Specification

## Overview
**CareerConnect** is an intelligent, skill-based job matching application developed to advance **UN Sustainable Development Goal 8 (Decent Work and Economic Growth)**. The platform directly bridges the gap between job seekers and employers by evaluating objective, verified skills against real-world job posting requirements, eliminating keyword bias and promoting transparent, fair employment matching.

---

## Architecture Diagram

```
+----------------------------------------------------------------------------------+
|                            ANDROID CLIENT APPLICATION                            |
|                                                                                  |
|   +--------------------------------------------------------------------------+   |
|   |                  Jetpack Compose & Material 3 UI Layer                   |   |
|   |  (22 Screens, Material 3 Navy & Blue Theme, Bottom Navigation (5 tabs))   |   |
|   +--------------------------------------------------------------------------+   |
|                                     |                                            |
|   +--------------------------------------------------------------------------+   |
|   |                         MVVM ViewModels (Flow)                           |   |
|   |    (AuthVM, ProfileVM, ResumeVM, JobVM, MatchVM, NotificationVM)         |   |
|   +--------------------------------------------------------------------------+   |
|                         |                                |                       |
|   +------------------------------------+   +---------------------------------+   |
|   |         Offline Local Store        |   |       Remote API Layer          |   |
|   |  - Room DB (Cached Jobs)           |   |  - Retrofit Client              |   |
|   |  - Preferences DataStore (Tokens)  |   |  - AuthInterceptor (JWT Bearer) |   |
|   +------------------------------------+   +---------------------------------+   |
+----------------------------------------------------------------------------------+
                                        | (HTTPS / REST API)
                                        v
+----------------------------------------------------------------------------------+
|                              FASTAPI BACKEND SERVICE                             |
|                                                                                  |
|   +--------------------------------------------------------------------------+   |
|   |                             REST API Routers                             |   |
|   |   /api/auth       /api/profile        /api/resume        /api/jobs       |   |
|   |   /api/matches    /api/skill-gaps     /api/recommendations               |   |
|   |   /api/saved-jobs /api/applications   /api/notifications                 |   |
|   +--------------------------------------------------------------------------+   |
|                                     |                                            |
|   +--------------------------------------------------------------------------+   |
|   |                            Core Services Layer                           |   |
|   |   - ResumeParserService (pypdf, python-docx, keyword extractor)          |   |
|   |   - SkillMatchingService (60% Skill + 20% Exp + 10% Edu + 10% Pref)      |   |
|   |   - JobSyncService (Pluggable: Remotive, Arbeitnow, Adzuna)              |   |
|   |   - NotificationService & AuthService (Bcrypt, PyJWT)                    |   |
|   +--------------------------------------------------------------------------+   |
|                                     |                                            |
|   +--------------------------------------------------------------------------+   |
|   |                  Database Layer (SQLAlchemy ORM + Alembic)               |   |
|   |            PostgreSQL (Production) / SQLite (Dev & Testing)              |   |
|   +--------------------------------------------------------------------------+   |
+----------------------------------------------------------------------------------+
                                        |
                   +--------------------+--------------------+
                   |                                         |
                   v                                         v
   +-------------------------------+         +-------------------------------+
   |      Remotive Remote API      |         |       Arbeitnow API           |
   | (Genuine public remote jobs)  |         |  (Verified job opportunities) |
   +-------------------------------+         +-------------------------------+
```

---

## 1. Android Frontend Architecture
- **Language**: Kotlin 1.9+
- **UI Toolkit**: Jetpack Compose with Material 3 design system.
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with unidirectional data flow (UDF).
- **Navigation**: Navigation Compose managing 22 distinct screen routes with state restoration.
- **Local Persistence**:
  - `DataStore Preferences`: Handles secure storage of JWT access/refresh tokens, session state, and user preferences.
  - `Room Database`: Caches job listings locally for offline resilience.
- **Networking**: Retrofit 2 + OkHttp 4 with `AuthInterceptor` providing automatic Bearer token injection.
- **Asynchronous Execution**: Kotlin Coroutines (`viewModelScope`) and reactive `StateFlow`.

## 2. Backend Architecture
- **Language & Framework**: Python 3.10+ / FastAPI.
- **ORM & Database**: SQLAlchemy 2.0 with connection pooling.
  - Production target: PostgreSQL 14+ with full schemas, indexes, foreign keys, and cascading deletes.
  - Development & automated testing: SQLite support.
- **Security**:
  - Password Hashing: Industry-standard Bcrypt.
  - Authentication: Signed JWT tokens (HS256) with configurable access and refresh durations.
  - User Isolation: All profile, resume, saved job, and application queries are strictly filtered by authenticated `user_id`.
- **Background Tasks**: Asynchronous job provider polling and notification dispatches without blocking HTTP client threads.
