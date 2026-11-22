# Genuine Job Provider Integration Guide

CareerConnect is built to fetch **genuine, verified job opportunities** from legitimate providers. Scraping or inserting fabricated jobs is strictly prohibited.

---

## 1. Active Integrated Providers

### Provider 1: Remotive API (Active by Default)
- **Status**: Live, authorized public API.
- **Endpoint**: `https://remotive.com/api/remote-jobs`
- **Authentication**: No API key required.
- **Data Retrieved**: Remote tech jobs, tags, employer names, company logos, full HTML/text descriptions, original employer application URLs.

### Provider 2: Arbeitnow API (Active by Default)
- **Status**: Live, authorized public API.
- **Endpoint**: `https://www.arbeitnow.com/api/job-board-api`
- **Authentication**: No API key required.
- **Data Retrieved**: Verified job listings, location, remote indicators, tags, employment types, direct URLs.

---

## 2. Optional Configurable Providers

### Provider 3: Adzuna API
- **Website**: `https://developer.adzuna.com/`
- **Configuration in `.env`**:
  ```env
  ADZUNA_APP_ID=your_adzuna_app_id_here
  ADZUNA_APP_KEY=your_adzuna_app_key_here
  ADZUNA_COUNTRY=us
  ```
- When configured, the backend automatically registers and synchronizes with Adzuna's job search API.

---

## 3. Adding a New Provider

The platform uses a pluggable `BaseJobProvider` interface defined in `backend/app/services/job_sync_service.py`:

```python
class CustomJobProvider(BaseJobProvider):
    name = "CustomProviderName"
    api_url = "https://api.customjobsource.com/v1/jobs"

    async def fetch_jobs(self, limit: int = 50) -> List[Dict[str, Any]]:
        # 1. Fetch from authorized endpoint using httpx
        # 2. Extract fields: external_id, title, company_name, description, url, publication_date
        # 3. Return normalized dictionary
        ...
```
Add the provider class to `JobSyncService.get_registered_providers()`, and it will immediately participate in all periodic and on-demand synchronization cycles.
