# CareerConnect Database Architecture

The database architecture is designed for production PostgreSQL deployment, while maintaining compatibility with SQLite for fast, friction-free local development and automated testing.

---

## 1. Relational Entity Diagram

```
+----------------+       1:1       +-------------------+
|     users      |-----------------|   user_profiles   |
+----------------+                 +-------------------+
   |  1:N   |  1:N                          | 1:N
   |        |                               v
   |        +----------------------->  user_skills
   |  1:N                          (normalized_name)
   +---------------------> resumes (PDF/DOCX)
   |
   |  1:N
   +---------------------> saved_jobs --------+
   |                                          |
   |  1:N                                     |
   +---------------------> applications ------+
   |                                          |
   |  1:N                                     v
   +---------------------> job_matches ----> jobs <---- job_sources
   |                                          ^   (Remotive, Arbeitnow)
   |  1:N                                     |
   +---------------------> recommendations ---+
   |                                          | 1:N
   |  1:N                                     v
   +---------------------> notifications    job_required_skills
   |                                        (normalized_name)
   |  1:1
   +---------------------> notification_preferences
```

---

## 2. Table Catalog
1. **`users`**: Core candidate authentication, bcrypt hashed passwords, active/verified status, reset tokens.
2. **`user_profiles`**: Candidate education, college/university, degree, specialization, graduation year, experience years, preferences, and `is_confirmed_by_user` verification flag.
3. **`resumes`**: Uploaded resume file records, file size, storage paths, raw text, and parsed keyword data.
4. **`user_skills`**: Normalized candidate technical and soft skills, proficiency level, verification status.
5. **`job_sources`**: Registered job providers (Remotive, Arbeitnow, Adzuna, verified employers), API endpoints, sync timestamps.
6. **`jobs`**: Normalized job listings with title, company, location, employment type, experience requirements, description, salary, publication date, original application URL, active/expired flag.
7. **`job_required_skills`**: Skills required by job postings, normalized for matching comparison.
8. **`job_matches`**: Calculated mathematical compatibility scores, breakdown, matching skills, missing skills, and natural-language explanations.
9. **`recommendations`**: Personalized recommended jobs ranked by match score.
10. **`saved_jobs`**: Candidate bookmarked listings.
11. **`applications`**: Candidate application tracking (Interested, Applied, Interview, Rejected, Offer).
12. **`notifications`**: In-app notifications with read/unread status and type indicators.
13. **`notification_preferences`**: User alert preferences.

---

## 3. PostgreSQL Migration Setup Instructions
1. Install PostgreSQL and create the database:
   ```bash
   createdb -U postgres careerconnect_db
   ```
2. Execute the schema DDL:
   ```bash
   psql -U postgres -d careerconnect_db -f database/schema.sql
   ```
3. Load seed job sources:
   ```bash
   psql -U postgres -d careerconnect_db -f database/seed.sql
   ```
4. Set `DATABASE_URL` in `.env`:
   ```env
   DATABASE_URL=postgresql://postgres:password@localhost:5432/careerconnect_db
   ```
