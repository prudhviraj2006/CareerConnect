-- CareerConnect PostgreSQL Database Schema
-- Promoting Decent Work and Economic Growth (UN SDG 8)

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    reset_token VARCHAR(255),
    reset_token_expires TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 2. User Profiles Table
CREATE TABLE IF NOT EXISTS user_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(50),
    education_level VARCHAR(100),
    college_university VARCHAR(255),
    degree VARCHAR(255),
    specialization VARCHAR(255),
    graduation_year INTEGER,
    technical_skills JSONB DEFAULT '[]'::jsonb,
    soft_skills JSONB DEFAULT '[]'::jsonb,
    work_experience TEXT,
    experience_years FLOAT DEFAULT 0.0,
    internship_experience TEXT,
    preferred_job_role VARCHAR(255),
    preferred_location VARCHAR(255),
    expected_salary VARCHAR(100),
    employment_type_preference VARCHAR(100),
    is_confirmed_by_user BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);

-- 3. Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size INTEGER NOT NULL,
    parsed_text TEXT,
    parsed_data JSONB DEFAULT '{}'::jsonb,
    parse_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    parsing_errors TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON resumes(user_id);

-- 4. User Skills Table
CREATE TABLE IF NOT EXISTS user_skills (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) DEFAULT 'technical' NOT NULL,
    proficiency_level VARCHAR(50) DEFAULT 'intermediate' NOT NULL,
    is_verified_by_user BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_skill UNIQUE (user_id, normalized_name)
);
CREATE INDEX IF NOT EXISTS idx_user_skills_user_norm ON user_skills(user_id, normalized_name);

-- 5. Job Sources Table
CREATE TABLE IF NOT EXISTS job_sources (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    api_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. Jobs Table
CREATE TABLE IF NOT EXISTS jobs (
    id SERIAL PRIMARY KEY,
    source_id INTEGER REFERENCES job_sources(id) ON DELETE SET NULL,
    external_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_logo VARCHAR(500),
    location VARCHAR(255),
    employment_type VARCHAR(100),
    experience_level VARCHAR(100),
    experience_years_required FLOAT,
    min_education VARCHAR(100),
    description TEXT NOT NULL,
    salary VARCHAR(150),
    publication_date TIMESTAMP WITH TIME ZONE,
    original_url VARCHAR(1000) NOT NULL,
    is_remote BOOLEAN DEFAULT FALSE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_source_external_job UNIQUE (source_id, external_id)
);
CREATE INDEX IF NOT EXISTS idx_jobs_title ON jobs(title);
CREATE INDEX IF NOT EXISTS idx_jobs_company ON jobs(company_name);
CREATE INDEX IF NOT EXISTS idx_jobs_location ON jobs(location);
CREATE INDEX IF NOT EXISTS idx_jobs_is_active ON jobs(is_active);
CREATE INDEX IF NOT EXISTS idx_jobs_publication_date ON jobs(publication_date DESC);

-- 7. Job Required Skills Table
CREATE TABLE IF NOT EXISTS job_required_skills (
    id SERIAL PRIMARY KEY,
    job_id INTEGER NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    is_mandatory BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT uq_job_skill UNIQUE (job_id, normalized_name)
);
CREATE INDEX IF NOT EXISTS idx_job_required_skills_job_id ON job_required_skills(job_id);
CREATE INDEX IF NOT EXISTS idx_job_required_skills_normalized ON job_required_skills(normalized_name);

-- 8. Job Matches Table
CREATE TABLE IF NOT EXISTS job_matches (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id INTEGER NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    overall_match_score FLOAT NOT NULL,
    skills_score FLOAT NOT NULL,
    experience_score FLOAT NOT NULL,
    education_score FLOAT NOT NULL,
    location_preference_score FLOAT NOT NULL,
    matching_skills JSONB DEFAULT '[]'::jsonb,
    missing_skills JSONB DEFAULT '[]'::jsonb,
    explanation_summary TEXT NOT NULL,
    detailed_breakdown JSONB DEFAULT '{}'::jsonb,
    has_sufficient_info BOOLEAN DEFAULT TRUE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_job_match UNIQUE (user_id, job_id)
);
CREATE INDEX IF NOT EXISTS idx_job_matches_score ON job_matches(user_id, overall_match_score DESC);

-- 9. Recommendations Table
CREATE TABLE IF NOT EXISTS recommendations (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id INTEGER NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    match_score FLOAT NOT NULL,
    recommendation_reason TEXT NOT NULL,
    rank_order INTEGER DEFAULT 1 NOT NULL,
    is_dismissed BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_job_recommendation UNIQUE (user_id, job_id)
);
CREATE INDEX IF NOT EXISTS idx_recommendations_rank ON recommendations(user_id, rank_order ASC);

-- 10. Saved Jobs Table
CREATE TABLE IF NOT EXISTS saved_jobs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id INTEGER NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    notes TEXT,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_saved_job UNIQUE (user_id, job_id)
);
CREATE INDEX IF NOT EXISTS idx_saved_jobs_user_id ON saved_jobs(user_id);

-- 11. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id INTEGER NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'Applied' NOT NULL,
    notes TEXT,
    applied_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_job_application UNIQUE (user_id, job_id)
);
CREATE INDEX IF NOT EXISTS idx_applications_user_status ON applications(user_id, status);

-- 12. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) DEFAULT 'SYSTEM' NOT NULL,
    reference_id INTEGER,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);

-- 13. Notification Preferences Table
CREATE TABLE IF NOT EXISTS notification_preferences (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    email_notifications BOOLEAN DEFAULT TRUE NOT NULL,
    push_notifications BOOLEAN DEFAULT TRUE NOT NULL,
    job_matches_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    application_updates_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    recommendations_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
