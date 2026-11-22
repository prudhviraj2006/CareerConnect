# CareerConnect Skill-Based Matching Engine Specification

## 1. Principles & Objectives
The matching engine is built on strict academic principles:
- **No Random Percentages**: Every match percentage is derived mathematically from verifiable data points.
- **Explainability**: Candidates receive an itemized breakdown of why they matched or where a gap exists.
- **Missing Information Transparency**: Absence of requirements in a job description is never treated as a match; if essential requirements are missing, an `Insufficient Info` state is surfaced.
- **Synonym Normalization**: Technical competencies with different representations (e.g., "Postgres" vs "PostgreSQL", "Compose" vs "Jetpack Compose", "K8s" vs "Kubernetes") are mapped to canonical identifiers before comparison.

---

## 2. Configurable Mathematical Formula

The overall match score $S \in [0, 100]$ is computed as:

$$S = S_{\text{skills}} + S_{\text{experience}} + S_{\text{education}} + S_{\text{preferences}}$$

### Component 1: Skills Similarity ($W_s = 60\%$, max 60 points)
Let $K_{\text{job}}$ be the set of normalized required skills extracted from the job listing, and $K_{\text{user}}$ be the verified skills set in the user profile.

- If $|K_{\text{job}}| > 0$:
  $$S_{\text{skills}} = \left( \frac{|K_{\text{job}} \cap K_{\text{user}}|}{|K_{\text{job}}|} \right) \times 60.0$$
- If $|K_{\text{job}}| = 0$: $S_{\text{skills}} = 0.0$ (Cannot assume compatibility).

### Component 2: Experience Compatibility ($W_{exp} = 20\%$, max 20 points)
Let $Y_{\text{user}}$ be the candidate's years of experience, and $Y_{\text{req}}$ be the minimum required years:
- If $Y_{\text{req}} = \text{null}$ (unspecified): $S_{\text{experience}} = 10.0$ (Neutral half credit).
- If $Y_{\text{user}} \ge Y_{\text{req}}$: $S_{\text{experience}} = 20.0$ (Full compatibility).
- If $Y_{\text{user}} \ge (Y_{\text{req}} - 1.0)$: $S_{\text{experience}} = 15.0$ (Near compatibility).
- If $Y_{\text{user}} > 0$: $S_{\text{experience}} = \left( \frac{Y_{\text{user}}}{Y_{\text{req}}} \right) \times 20.0$.
- Else: $S_{\text{experience}} = 0.0$.

### Component 3: Education Compatibility ($W_{edu} = 10\%$, max 10 points)
- If job minimum degree not specified: $S_{\text{education}} = 5.0$ (Neutral).
- If candidate holds a degree equal to or higher than required: $S_{\text{education}} = 10.0$.
- If candidate holds a partial or adjacent qualification: $S_{\text{education}} = 4.0$.

### Component 4: Location & Employment Preference ($W_{pref} = 10\%$, max 10 points)
Split into 5 points for location and 5 points for employment type:
- **Location (5 pts)**:
  - If job is Remote: 5.0 points.
  - Else if candidate's preferred location matches job location: 5.0 points.
  - Else: 2.0 points.
- **Employment Type (5 pts)**:
  - If preferred type (e.g. Full-time, Internship) matches job type: 5.0 points.
  - Else if preference unconstrained: 5.0 points.
  - Else: 1.0 point.

---

## 3. Skill Gap Analysis & Compatibility Boost
For any job, the skill gap identifies:
- $\text{Matching} = K_{\text{job}} \cap K_{\text{user}}$
- $\text{Missing} = K_{\text{job}} \setminus K_{\text{user}}$

The **Compatibility Boost Potential** indicates the exact score increase achievable if the candidate acquires the missing skills:
$$\text{Boost Potential} = \left( \frac{|\text{Missing}|}{|K_{\text{job}}|} \right) \times 60.0\%$$
