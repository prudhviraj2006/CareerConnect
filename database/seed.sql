-- Seed data for CareerConnect
INSERT INTO job_sources (name, api_url, is_active, created_at)
VALUES 
    ('Remotive', 'https://remotive.com/api/remote-jobs', TRUE, CURRENT_TIMESTAMP),
    ('Arbeitnow', 'https://www.arbeitnow.com/api/job-board-api', TRUE, CURRENT_TIMESTAMP),
    ('Adzuna', 'https://api.adzuna.com/v1/api/jobs', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
