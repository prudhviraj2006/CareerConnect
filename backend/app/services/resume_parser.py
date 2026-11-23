import os
import re
from typing import Dict, Any, List, Tuple
from pypdf import PdfReader
from docx import Document

# Standard Technical and Soft Skill Taxonomy
TECHNICAL_SKILLS_CATALOG = {
    "kotlin", "java", "jetpack compose", "android", "flutter", "react native", "swift", "ios",
    "python", "fastapi", "django", "flask", "postgresql", "mysql", "sqlite", "mongodb",
    "redis", "docker", "kubernetes", "git", "github", "gitlab", "ci/cd", "rest api", "graphql",
    "javascript", "typescript", "react", "next.js", "vue", "angular", "html", "css", "tailwind",
    "node.js", "express", "aws", "azure", "google cloud", "gcp", "firebase", "linux", "bash",
    "c++", "c#", ".net", "go", "golang", "rust", "machine learning", "deep learning", "nlp",
    "tensorflow", "pytorch", "scikit-learn", "pandas", "numpy", "data structures", "algorithms",
    "room database", "retrofit", "coroutines", "flow", "mvvm", "clean architecture", "dagger hilt",
    "unit testing", "junit", "espresso", "material 3", "figma", "ui/ux", "agile", "scrum"
}

SOFT_SKILLS_CATALOG = {
    "communication", "teamwork", "problem solving", "leadership", "critical thinking",
    "time management", "adaptability", "collaboration", "work ethic", "attention to detail",
    "conflict resolution", "creativity", "decision making", "presentation", "mentoring"
}

INDIAN_LOCATIONS = [
    "bengaluru", "bangalore", "hyderabad", "pune", "mumbai", "delhi", "new delhi", "noida",
    "gurugram", "gurgaon", "chennai", "kolkata", "ahmedabad", "jaipur", "kochi", "coimbatore",
    "chandigarh", "indore", "surat", "bhopal", "nagpur", "visakhapatnam", "lucknow", "thiruvananthapuram"
]

# Synonyms and Normalization Map
SKILL_SYNONYMS = {
    "android development": "android",
    "android dev": "android",
    "compose": "jetpack compose",
    "jetpack compose": "jetpack compose",
    "postgres": "postgresql",
    "k8s": "kubernetes",
    "js": "javascript",
    "ts": "typescript",
    "py": "python",
    "ml": "machine learning",
    "dl": "deep learning",
    "gcp": "google cloud",
    "amazon web services": "aws",
    "microsoft azure": "azure",
    "node": "node.js",
    "reactjs": "react",
    "vuejs": "vue",
    "golang": "go",
    "tdd": "unit testing",
}

def normalize_skill(skill_name: str) -> str:
    cleaned = skill_name.strip().lower()
    cleaned = re.sub(r'[\(\)\[\]\{\}]', '', cleaned)
    return SKILL_SYNONYMS.get(cleaned, cleaned)

class ResumeParserService:
    @staticmethod
    def extract_text_from_pdf(file_path: str) -> str:
        try:
            reader = PdfReader(file_path)
            extracted_text = []
            for page in reader.pages:
                text = page.extract_text()
                if text:
                    extracted_text.append(text)
            return "\n".join(extracted_text)
        except Exception as e:
            raise ValueError(f"Failed to read PDF document: {str(e)}")

    @staticmethod
    def extract_text_from_docx(file_path: str) -> str:
        try:
            doc = Document(file_path)
            paragraphs = [p.text for p in doc.paragraphs if p.text]
            for table in doc.tables:
                for row in table.rows:
                    for cell in row.cells:
                        if cell.text:
                            paragraphs.append(cell.text)
            return "\n".join(paragraphs)
        except Exception as e:
            raise ValueError(f"Failed to read DOCX document: {str(e)}")

    @classmethod
    def extract_resume_text(cls, file_path: str, file_type: str) -> str:
        ext = file_type.lower().replace(".", "")
        if ext == "pdf":
            return cls.extract_text_from_pdf(file_path)
        elif ext in ("docx", "doc"):
            return cls.extract_text_from_docx(file_path)
        else:
            raise ValueError(f"Unsupported file format: {file_type}. Supported formats are PDF and DOCX.")

    @classmethod
    def parse_resume_content(cls, raw_text: str) -> Dict[str, Any]:
        """
        Extracts structured candidate info, skills, education, and experience from resume text.
        Transparent keyword/regex-based extraction (clearly distinguished from verified data).
        """
        text_lower = raw_text.lower()
        lines = [line.strip() for line in raw_text.splitlines() if line.strip()]

        # 1. Contact Info
        email_match = re.search(r"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+", raw_text)
        email = email_match.group(0) if email_match else None

        phone_match = re.search(r"(\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}", raw_text)
        phone = phone_match.group(0) if phone_match else None

        # Candidate name heuristic: First non-empty header line without symbols/emails
        candidate_name = None
        for line in lines[:5]:
            if "@" not in line and not re.search(r"\d{3,}", line) and len(line.split()) in (2, 3, 4):
                if not any(header in line.lower() for header in ["resume", "curriculum vitae", "cv", "profile", "contact"]):
                    candidate_name = line.strip()
                    break

        # 2. Education Extraction
        degree_patterns = [
            (r"\b(b\.?tech|b\.?e\.?|bachelor of technology|bachelor of engineering|b\.?s\.?|bachelor of science|bca)\b", "Bachelor's Degree"),
            (r"\b(m\.?tech|m\.?e\.?|master of technology|m\.?s\.?|master of science|mca)\b", "Master's Degree"),
            (r"\b(ph\.?d|doctorate)\b", "Doctorate"),
            (r"\b(diploma|associate)\b", "Diploma / Associate Degree"),
            (r"\b(high school|secondary)\b", "High School"),
        ]
        detected_education_level = None
        for pattern, level in degree_patterns:
            if re.search(pattern, text_lower):
                detected_education_level = level
                break

        # Graduation year detection (e.g. 2018 - 2022, Graduating 2025, etc.)
        year_matches = re.findall(r"\b(20[0-2][0-9]|19[8-9][0-9])\b", raw_text)
        graduation_year = None
        if year_matches:
            # Pick highest reasonable graduation year
            years = [int(y) for y in year_matches if 2000 <= int(y) <= 2030]
            if years:
                graduation_year = max(years)

        # College / University detection
        college_university = None
        for line in lines:
            line_l = line.lower()
            if any(term in line_l for term in ["university", "institute of technology", "college of engineering", "iit", "nit", "bits"]):
                college_university = line[:100].strip()
                break

        # 3. Skills Identification
        extracted_tech_skills = set()
        for skill in TECHNICAL_SKILLS_CATALOG:
            # Word boundary regex to avoid partial substring false positives (e.g., 'go' in 'good')
            escaped = re.escape(skill)
            pattern = rf"(?<![a-zA-Z0-9]){escaped}(?![a-zA-Z0-9])"
            if re.search(pattern, text_lower):
                extracted_tech_skills.add(skill.title())

        extracted_soft_skills = set()
        for s_skill in SOFT_SKILLS_CATALOG:
            escaped = re.escape(s_skill)
            pattern = rf"(?<![a-zA-Z0-9]){escaped}(?![a-zA-Z0-9])"
            if re.search(pattern, text_lower):
                extracted_soft_skills.add(s_skill.title())

        # 4. Experience Years Extraction
        exp_match = re.search(r"(\d+(\.\d+)?)\+?\s*(?:years?|yrs?)(?:\s+of)?\s+experience", text_lower)
        experience_years = float(exp_match.group(1)) if exp_match else 0.0

        # Section Extractor Helper
        def extract_section_lines(header_keywords: List[str], stop_keywords: List[str], max_count: int = 8) -> List[str]:
            found_lines = []
            is_in_section = False
            for line in lines:
                l_lower = line.lower().strip()
                if any(k in l_lower for k in header_keywords) and len(l_lower.split()) <= 5:
                    is_in_section = True
                    continue
                if is_in_section:
                    if any(s in l_lower for s in stop_keywords) and len(l_lower.split()) <= 5:
                        is_in_section = False
                    else:
                        found_lines.append(line)
                        if len(found_lines) >= max_count:
                            break
            return found_lines

        # Summarize work experience lines
        exp_lines = extract_section_lines(
            header_keywords=["work experience", "employment history", "professional experience"],
            stop_keywords=["education", "skills", "projects", "certifications", "internship", "interests"]
        )
        if not exp_lines:
            # Fallback general experience check
            exp_lines = extract_section_lines(
                header_keywords=["experience"],
                stop_keywords=["education", "skills", "projects", "certifications", "interests"]
            )
        experience_summary = "\n".join(exp_lines) if exp_lines else None

        # 5. Internship Experience Extraction
        intern_lines = extract_section_lines(
            header_keywords=["internship", "internships", "summer internship", "training"],
            stop_keywords=["education", "skills", "projects", "certifications", "work experience", "interests"]
        )
        internship_experience = "\n".join(intern_lines) if intern_lines else None

        # 6. Projects Extraction
        proj_lines = extract_section_lines(
            header_keywords=["projects", "academic projects", "key projects", "personal projects"],
            stop_keywords=["education", "skills", "certifications", "experience", "internship", "interests"]
        )
        projects = "\n".join(proj_lines) if proj_lines else None

        # 7. Certifications Extraction
        cert_lines = extract_section_lines(
            header_keywords=["certifications", "certificates", "licenses", "online courses", "credentials"],
            stop_keywords=["education", "skills", "projects", "experience", "internship", "interests"]
        )
        certifications = "\n".join(cert_lines) if cert_lines else None

        # 8. Location Detection (India-first check)
        detected_location = None
        common_cities = [
            "bengaluru", "bangalore", "hyderabad", "pune", "mumbai", "delhi", "noida", "gurugram", "gurgaon",
            "chennai", "kolkata", "ahmedabad", "jaipur", "kochi", "coimbatore", "chandigarh", "indore"
        ]
        for line in lines[:10]:
            line_l = line.lower()
            for city in common_cities:
                if city in line_l:
                    detected_location = city.title() + ", India"
                    break
            if detected_location:
                break

        # Relevant keywords
        relevant_keywords = list(extracted_tech_skills.union(extracted_soft_skills))[:20]

        return {
            "candidate_name": candidate_name,
            "email": email,
            "phone": phone,
            "education_level": detected_education_level or "Bachelor's Degree",
            "college_university": college_university,
            "degree": detected_education_level,
            "specialization": "Computer Science / Information Technology" if "computer" in text_lower or "software" in text_lower else None,
            "graduation_year": graduation_year,
            "extracted_skills": sorted(list(extracted_tech_skills)),
            "extracted_soft_skills": sorted(list(extracted_soft_skills)),
            "experience_summary": experience_summary,
            "experience_years": experience_years,
            "internship_experience": internship_experience,
            "projects": projects,
            "certifications": certifications,
            "location": detected_location,
            "relevant_keywords": relevant_keywords,
            "parser_engine": "CareerConnect Rule-Based Keyword & Taxonomy Parser v1.0",
            "is_ai_generated": False, # Explicit transparency requirement
            "notice": "Extracted automatically from uploaded document text. Please review and verify all details."
        }
