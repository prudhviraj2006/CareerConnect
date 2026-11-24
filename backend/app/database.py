from sqlalchemy import create_engine, event, text
from sqlalchemy.orm import declarative_base, sessionmaker
from backend.app.config import settings

db_url = settings.DATABASE_URL
if db_url.startswith("postgres://"):
    db_url = db_url.replace("postgres://", "postgresql://", 1)

is_sqlite = db_url.startswith("sqlite")

connect_args = {"check_same_thread": False} if is_sqlite else {}

engine = create_engine(
    db_url,
    connect_args=connect_args,
    echo=False
)

if is_sqlite:
    @event.listens_for(engine, "connect")
    def set_sqlite_pragma(dbapi_connection, connection_record):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def run_schema_migrations(eng):
    """
    Ensure all columns required by the latest models exist in PostgreSQL / SQLite,
    safely adding any missing columns without dropping data.
    """
    dialect = eng.dialect.name
    with eng.begin() as conn:
        if dialect == "postgresql":
            migrations = [
                # users table
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50);",
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;",
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token VARCHAR(255);",
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token_expires TIMESTAMP;",
                # user_profiles table
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS technical_skills JSONB DEFAULT '[]'::jsonb;",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS soft_skills JSONB DEFAULT '[]'::jsonb;",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS experience_years FLOAT DEFAULT 0.0;",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS internship_experience TEXT;",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS projects TEXT;",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS certifications TEXT;",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS preferred_job_role VARCHAR(255);",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS preferred_location VARCHAR(255);",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS expected_salary VARCHAR(100);",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS employment_type_preference VARCHAR(100);",
                "ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS is_confirmed_by_user BOOLEAN DEFAULT FALSE;",
                # jobs table
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS is_india BOOLEAN DEFAULT FALSE;",
                "CREATE INDEX IF NOT EXISTS ix_jobs_is_india ON jobs (is_india);",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS is_india_relevant BOOLEAN DEFAULT TRUE;",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS is_internship BOOLEAN DEFAULT FALSE;",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS is_fresher_friendly BOOLEAN DEFAULT FALSE;",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS application_url VARCHAR(500);",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS salary_currency VARCHAR(10) DEFAULT 'INR';",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS min_salary_inr FLOAT;",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS max_salary_inr FLOAT;",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS source_attribution VARCHAR(255);",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS verified_source BOOLEAN DEFAULT FALSE;",
                "ALTER TABLE jobs ADD COLUMN IF NOT EXISTS direct_apply_url VARCHAR(500);"
            ]
            for statement in migrations:
                try:
                    conn.execute(text(statement))
                except Exception:
                    pass
        elif dialect == "sqlite":
            tables_cols = {
                "users": [
                    ("phone_number", "TEXT"),
                    ("is_verified", "BOOLEAN DEFAULT 0"),
                    ("reset_token", "TEXT"),
                    ("reset_token_expires", "DATETIME")
                ],
                "user_profiles": [
                    ("technical_skills", "TEXT DEFAULT '[]'"),
                    ("soft_skills", "TEXT DEFAULT '[]'"),
                    ("experience_years", "FLOAT DEFAULT 0.0"),
                    ("internship_experience", "TEXT"),
                    ("projects", "TEXT"),
                    ("certifications", "TEXT"),
                    ("preferred_job_role", "TEXT"),
                    ("preferred_location", "TEXT"),
                    ("expected_salary", "TEXT"),
                    ("employment_type_preference", "TEXT"),
                    ("is_confirmed_by_user", "BOOLEAN DEFAULT 0")
                ],
                "jobs": [
                    ("is_india", "BOOLEAN DEFAULT 0"),
                    ("is_india_relevant", "BOOLEAN DEFAULT 1"),
                    ("is_internship", "BOOLEAN DEFAULT 0"),
                    ("is_fresher_friendly", "BOOLEAN DEFAULT 0"),
                    ("application_url", "TEXT"),
                    ("salary_currency", "TEXT DEFAULT 'INR'"),
                    ("min_salary_inr", "FLOAT"),
                    ("max_salary_inr", "FLOAT"),
                    ("source_attribution", "TEXT"),
                    ("verified_source", "BOOLEAN DEFAULT 0"),
                    ("direct_apply_url", "TEXT")
                ]
            }
            for table, cols in tables_cols.items():
                try:
                    existing = {row[1] for row in conn.execute(text(f"PRAGMA table_info({table})")).fetchall()}
                    for col_name, col_type in cols:
                        if col_name not in existing:
                            conn.execute(text(f"ALTER TABLE {table} ADD COLUMN {col_name} {col_type}"))
                except Exception:
                    pass

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
