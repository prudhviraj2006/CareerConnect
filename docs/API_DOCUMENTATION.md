# CareerConnect REST API Documentation

Base URL: `http://localhost:8000/` (or `http://10.0.2.2:8000/` from Android Emulator)
Swagger Documentation: `http://localhost:8000/docs`

---

## 1. Authentication Endpoints

### `POST /api/auth/register`
Creates a new candidate account.
- **Request Body**:
  ```json
  {
    "email": "candidate@example.com",
    "password": "SecurePassword123!",
    "full_name": "Alex Morgan",
    "phone_number": "+1234567890"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "access_token": "eyJhbGciOi...",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOi...",
    "user": {
      "id": 1,
      "email": "candidate@example.com",
      "full_name": "Alex Morgan",
      "phone_number": "+1234567890",
      "is_active": true,
      "is_verified": false
    }
  }
  ```

### `POST /api/auth/login`
Authenticates a candidate and returns JWT tokens.
- **Request Body**:
  ```json
  {
    "email": "candidate@example.com",
    "password": "SecurePassword123!"
  }
  ```
- **Response** (`200 OK`): `TokenResponse`

### `POST /api/auth/refresh`
Issues a new access token using a valid refresh token.
- **Request Body**: `{ "refresh_token": "..." }`
- **Response** (`200 OK`): `TokenResponse`

### `POST /api/auth/forgot-password`
Initiates secure token-based password recovery.
- **Request Body**: `{ "email": "candidate@example.com" }`
- **Response** (`200 OK`): `{ "message": "Password reset instructions have been generated." }`

### `POST /api/auth/reset-password`
Resets user password with token.
- **Request Body**:
  ```json
  {
    "token": "reset_token_string",
    "new_password": "NewSecurePassword123!"
  }
  ```
- **Response** (`200 OK`): `{ "message": "Password has been successfully reset." }`

---

## 2. User Profile & Resume Endpoints

### `GET /api/profile`
Retrieves verified profile for the authenticated user.
- **Headers**: `Authorization: Bearer <token>`
- **Response** (`200 OK`): `UserProfileResponse`

### `PUT /api/profile`
Updates profile information. Triggering this recalculates matches and recommendations.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
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
    "employment_type_preference": "Full-time",
    "is_confirmed_by_user": true
  }
  ```

### `POST /api/resume/upload`
Uploads a resume file (.pdf, .docx, <= 10MB) and extracts candidate details.
- **Headers**: `Authorization: Bearer <token>`
- **Form Data**: `file: <binary document>`
- **Response** (`200 OK`): Extracted structured data for user review (with transparency metadata).

### `POST /api/resume/confirm`
Saves reviewed and user-corrected details into the official profile.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**: User-verified profile update payload.

### `DELETE /api/resume`
Deletes uploaded resume file and parsed history.

---

## 3. Jobs Endpoints

### `GET /api/jobs`
List verified job postings with pagination, sorting, and filters.
- **Query Parameters**:
  - `page`: int (default 1)
  - `limit`: int (default 20, max 100)
  - `location`: string (e.g. "Remote", "Berlin")
  - `experience_level`: string ("Entry-level", "Mid-level", "Senior")
  - `employment_type`: string ("Full-time", "Part-time", "Contract")
  - `is_remote`: boolean
  - `sort_by`: "date" or "relevance"

### `GET /api/jobs/search`
Searches jobs by title, company name, description, or required skill tags.
- **Query Parameter**: `q`: query string.

### `GET /api/jobs/{id}`
Returns complete details of a specific job, including full description and original application URL.

### `POST /api/jobs/sync`
Polls genuine external job providers (Remotive, Arbeitnow, Adzuna), normalizes data, dedupes, and updates database.

---

## 4. Matching & Skill Gap Endpoints

### `GET /api/matches/{job_id}`
Returns transparent compatibility analysis with exact weight breakdown:
- Overall Score (0-100)
- Skills Similarity (60% weight)
- Experience Compatibility (20% weight)
- Education Compatibility (10% weight)
- Location & Preference Compatibility (10% weight)
- Matching skills and missing skills lists
- Natural-language explanation summary

### `GET /api/skill-gaps/{job_id}`
Returns skill gap analysis:
- User skills vs required job skills
- Matching skills list
- Missing skills list
- Actionable advice and potential compatibility percentage boost upon skill acquisition.

---

## 5. Recommendations Endpoints

### `GET /api/recommendations`
Returns top job recommendations personalized to the user's verified profile, ranked by calculated match score.

---

## 6. Saved Jobs & Applications Endpoints

### `POST /api/saved-jobs/{job_id}`
Saves a job listing for future review.

### `DELETE /api/saved-jobs/{job_id}`
Removes a job listing from the saved collection.

### `GET /api/saved-jobs`
Retrieves all saved jobs for the authenticated user.

### `POST /api/applications`
Adds a job to manual application tracking with initial status (`Interested`, `Applied`, `Interview`, `Rejected`, `Offer`).

### `GET /api/applications`
Retrieves all candidate tracked applications.

### `PUT /api/applications/{id}`
Updates application status or notes.

---

## 7. Notifications Endpoints

### `GET /api/notifications`
Retrieves in-app notifications.

### `PUT /api/notifications/{id}/read`
Marks a single notification as read.

### `PUT /api/notifications/read-all`
Marks all user notifications as read.

### `GET /api/notifications/preferences`
Retrieves user notification preferences.

### `PUT /api/notifications/preferences`
Updates notification preferences.
