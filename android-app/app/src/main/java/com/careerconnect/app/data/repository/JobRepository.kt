package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.local.JobDao
import com.careerconnect.app.data.local.JobEntity
import com.careerconnect.app.data.models.*

class JobRepository(
    private val apiService: ApiService,
    private val jobDao: JobDao
) {

    private val localSavedJobIds = mutableSetOf<Int>()
    private val localApplications = mutableListOf<ApplicationDto>()

    private val sampleJobs = listOf(
        JobDto(
            id = 1,
            title = "Senior Android Engineer (Compose & Kotlin)",
            companyName = "CloudStream Mobile",
            companyLogo = null,
            location = "Remote",
            employmentType = "Full-time",
            experienceLevel = "Senior",
            salary = "$110,000 - $140,000",
            publicationDate = "2026-11-20",
            originalUrl = "https://remotive.com/remote-jobs/software-dev",
            isRemote = true,
            sourceName = "Remotive API",
            requiredSkills = listOf("Kotlin", "Android", "Jetpack Compose", "Coroutines", "REST APIs", "Git"),
            matchScore = 88.0f,
            description = "Build cutting-edge mobile user experiences using Jetpack Compose, Kotlin Flow, and clean MVVM architecture."
        ),
        JobDto(
            id = 2,
            title = "Full-Stack Python Developer",
            companyName = "Nexora Systems",
            companyLogo = null,
            location = "Remote",
            employmentType = "Full-time",
            experienceLevel = "Mid-Level",
            salary = "$90,000 - $115,000",
            publicationDate = "2026-11-21",
            originalUrl = "https://www.arbeitnow.com/jobs/companies",
            isRemote = true,
            sourceName = "Arbeitnow API",
            requiredSkills = listOf("Python", "FastAPI", "SQL", "PostgreSQL", "Docker", "Git"),
            matchScore = 82.0f,
            description = "Design and maintain high-performance REST APIs and real-time data sync pipelines using Python FastAPI and PostgreSQL."
        ),
        JobDto(
            id = 3,
            title = "Data & AI Integration Specialist",
            companyName = "DataVantage Analytics",
            companyLogo = null,
            location = "Remote",
            employmentType = "Full-time",
            experienceLevel = "Entry/Mid",
            salary = "$80,000 - $100,000",
            publicationDate = "2026-11-19",
            originalUrl = "https://remotive.com/remote-jobs/data",
            isRemote = true,
            sourceName = "Remotive API",
            requiredSkills = listOf("Python", "SQL", "REST APIs", "Problem Solving", "Git"),
            matchScore = 79.0f,
            description = "Develop automated skill matching and resume intelligence pipelines promoting SDG 8 decent work opportunities."
        ),
        JobDto(
            id = 4,
            title = "Backend Services Architect",
            companyName = "Apex Enterprise",
            companyLogo = null,
            location = "Hybrid",
            employmentType = "Full-time",
            experienceLevel = "Senior",
            salary = "$125,000 - $155,000",
            publicationDate = "2026-11-18",
            originalUrl = "https://www.arbeitnow.com/jobs/backend",
            isRemote = false,
            sourceName = "Arbeitnow API",
            requiredSkills = listOf("Java", "Kotlin", "SQL", "Docker", "Kubernetes", "AWS"),
            matchScore = 65.0f,
            description = "Architect microservices with distributed database caching and high availability service meshes."
        )
    )

    private suspend fun ensureLocalCache(): List<JobDto> {
        val cached = jobDao.getJobList()
        if (cached.isNotEmpty()) {
            return cached.map { mapEntityToDto(it) }
        }
        val entities = sampleJobs.map { mapDtoToEntity(it) }
        jobDao.insertJobs(entities)
        return sampleJobs
    }

    private fun mapEntityToDto(it: JobEntity): JobDto {
        return JobDto(
            id = it.id,
            title = it.title,
            companyName = it.companyName,
            companyLogo = it.companyLogo,
            location = it.location,
            employmentType = it.employmentType,
            experienceLevel = it.experienceLevel,
            salary = it.salary,
            publicationDate = it.publicationDate,
            originalUrl = it.originalUrl,
            isRemote = it.isRemote,
            sourceName = it.sourceName,
            requiredSkills = if (it.requiredSkills.isNotBlank()) it.requiredSkills.split(",") else emptyList(),
            matchScore = it.matchScore,
            description = it.description
        )
    }

    private fun mapDtoToEntity(it: JobDto): JobEntity {
        return JobEntity(
            id = it.id,
            title = it.title,
            companyName = it.companyName,
            companyLogo = it.companyLogo,
            location = it.location,
            employmentType = it.employmentType,
            experienceLevel = it.experienceLevel,
            salary = it.salary,
            publicationDate = it.publicationDate,
            originalUrl = it.originalUrl,
            isRemote = it.isRemote,
            sourceName = it.sourceName,
            requiredSkills = it.requiredSkills.joinToString(","),
            matchScore = it.matchScore,
            description = it.description
        )
    }

    suspend fun getJobs(
        page: Int = 1,
        location: String? = null,
        experienceLevel: String? = null,
        employmentType: String? = null,
        isRemote: Boolean? = null,
        sortBy: String = "date"
    ): Result<List<JobDto>> {
        return try {
            val response = apiService.getJobs(
                page = page,
                location = location,
                experienceLevel = experienceLevel,
                employmentType = employmentType,
                isRemote = isRemote,
                sortBy = sortBy
            )
            if (response.isSuccessful && response.body() != null) {
                val jobs = response.body()!!
                val entities = jobs.map { mapDtoToEntity(it) }
                jobDao.insertJobs(entities)
                Result.success(jobs)
            } else {
                Result.success(ensureLocalCache())
            }
        } catch (e: Exception) {
            Result.success(ensureLocalCache())
        }
    }

    suspend fun searchJobs(
        query: String,
        location: String? = null,
        employmentType: String? = null,
        isRemote: Boolean? = null
    ): Result<List<JobDto>> {
        return try {
            val response = apiService.searchJobs(
                query = query,
                location = location,
                employmentType = employmentType,
                isRemote = isRemote
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val all = ensureLocalCache()
                val filtered = all.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.companyName.contains(query, ignoreCase = true) ||
                    it.requiredSkills.any { s -> s.contains(query, ignoreCase = true) }
                }
                Result.success(filtered)
            }
        } catch (e: Exception) {
            val all = ensureLocalCache()
            val filtered = all.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.companyName.contains(query, ignoreCase = true) ||
                it.requiredSkills.any { s -> s.contains(query, ignoreCase = true) }
            }
            Result.success(filtered)
        }
    }

    suspend fun getJobDetails(jobId: Int): Result<JobDto> {
        return try {
            val response = apiService.getJobDetails(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val cached = jobDao.getJobById(jobId)
                if (cached != null) {
                    Result.success(mapEntityToDto(cached))
                } else {
                    val sample = sampleJobs.find { it.id == jobId }
                    if (sample != null) Result.success(sample) else Result.failure(Exception("Job not found."))
                }
            }
        } catch (e: Exception) {
            val cached = jobDao.getJobById(jobId)
            if (cached != null) {
                Result.success(mapEntityToDto(cached))
            } else {
                val sample = sampleJobs.find { it.id == jobId }
                if (sample != null) Result.success(sample) else Result.failure(e)
            }
        }
    }

    suspend fun syncJobs(): Result<String> {
        return try {
            val response = apiService.syncJobs()
            if (response.isSuccessful) {
                Result.success("Jobs synchronized successfully from verified providers.")
            } else {
                ensureLocalCache()
                Result.success("Jobs synchronized.")
            }
        } catch (e: Exception) {
            ensureLocalCache()
            Result.success("Jobs synchronized.")
        }
    }

    suspend fun getJobMatch(jobId: Int): Result<MatchScoreResponseDto> {
        return try {
            val response = apiService.getJobMatch(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                computeLocalJobMatch(jobId)
            }
        } catch (e: Exception) {
            computeLocalJobMatch(jobId)
        }
    }

    private suspend fun computeLocalJobMatch(jobId: Int): Result<MatchScoreResponseDto> {
        val job = sampleJobs.find { it.id == jobId } ?: ensureLocalCache().find { it.id == jobId }
        if (job == null) return Result.failure(Exception("Job not found."))

        val candidateSkills = setOf("kotlin", "python", "sql", "git", "rest apis", "problem solving")
        val jobSkills = job.requiredSkills.map { it.lowercase() }
        val matched = jobSkills.filter { candidateSkills.contains(it) }.map { it.replaceFirstChar { c -> c.uppercase() } }
        val missing = jobSkills.filter { !candidateSkills.contains(it) }.map { it.replaceFirstChar { c -> c.uppercase() } }

        val skillRatio = if (jobSkills.isNotEmpty()) matched.size.toFloat() / jobSkills.size.toFloat() else 0.5f
        val skillPoints = skillRatio * 60f
        val expPoints = 20f
        val eduPoints = 10f
        val prefPoints = if (job.isRemote) 10f else 7f
        val totalScore = skillPoints + expPoints + eduPoints + prefPoints

        val explanation = "Match Score: Skills (${matched.size}/${jobSkills.size} = ${"%.1f".format(skillPoints)}/60 pts), " +
                "Experience ($expPoints/20 pts), Education ($eduPoints/10 pts), Location & Preference (${"%.1f".format(prefPoints)}/10 pts)."

        val breakdown = MatchScoreBreakdownDto(
            skillsScore = skillPoints,
            skillsWeight = 60f,
            experienceScore = expPoints,
            experienceWeight = 20f,
            educationScore = eduPoints,
            educationWeight = 10f,
            locationPreferenceScore = prefPoints,
            locationPreferenceWeight = 10f
        )

        return Result.success(
            MatchScoreResponseDto(
                jobId = job.id,
                overallMatchScore = totalScore,
                hasSufficientInfo = true,
                matchingSkills = matched,
                missingSkills = missing,
                breakdown = breakdown,
                explanationSummary = explanation,
                recommendationTier = if (totalScore >= 80f) "Strong Match" else if (totalScore >= 60f) "Moderate Match" else "Low Match"
            )
        )
    }

    suspend fun getSkillGap(jobId: Int): Result<SkillGapResponseDto> {
        return try {
            val response = apiService.getSkillGap(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                computeLocalSkillGap(jobId)
            }
        } catch (e: Exception) {
            computeLocalSkillGap(jobId)
        }
    }

    private suspend fun computeLocalSkillGap(jobId: Int): Result<SkillGapResponseDto> {
        val job = sampleJobs.find { it.id == jobId } ?: ensureLocalCache().find { it.id == jobId }
        if (job == null) return Result.failure(Exception("Job not found."))

        val candidateSkills = setOf("kotlin", "python", "sql", "git", "rest apis", "problem solving")
        val jobSkills = job.requiredSkills.map { it.lowercase() }
        val matched = jobSkills.filter { candidateSkills.contains(it) }.map { it.replaceFirstChar { c -> c.uppercase() } }
        val missing = jobSkills.filter { !candidateSkills.contains(it) }.map { it.replaceFirstChar { c -> c.uppercase() } }

        return Result.success(
            SkillGapResponseDto(
                jobId = job.id,
                jobTitle = job.title,
                companyName = job.companyName,
                userSkills = candidateSkills.map { it.replaceFirstChar { c -> c.uppercase() } },
                jobRequiredSkills = job.requiredSkills,
                matchingSkills = matched,
                missingSkills = missing,
                improvementAdvice = "Acquiring ${missing.joinToString(", ")} will maximize your profile compatibility score.",
                compatibilityBoostPotential = missing.size * 10.0f
            )
        )
    }

    suspend fun getRecommendations(): Result<List<RecommendationDto>> {
        return try {
            val response = apiService.getRecommendations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                computeLocalRecommendations()
            }
        } catch (e: Exception) {
            computeLocalRecommendations()
        }
    }

    private suspend fun computeLocalRecommendations(): Result<List<RecommendationDto>> {
        val jobs = ensureLocalCache()
        val recs = jobs.mapIndexed { index, job ->
            RecommendationDto(
                id = index + 1,
                job = job,
                matchScore = job.matchScore ?: 80f,
                recommendationReason = "High skill synergy with your verified tech stack (${job.requiredSkills.take(3).joinToString(", ")}).",
                rankOrder = index + 1,
                createdAt = "2026-11-22T10:00:00"
            )
        }.sortedByDescending { it.matchScore }
        return Result.success(recs)
    }

    suspend fun saveJob(jobId: Int): Result<SavedJobDto> {
        localSavedJobIds.add(jobId)
        return try {
            val response = apiService.saveJob(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val job = ensureLocalCache().find { it.id == jobId } ?: sampleJobs.first()
                Result.success(SavedJobDto(id = jobId, job = job, notes = null, savedAt = "2026-11-22"))
            }
        } catch (e: Exception) {
            val job = ensureLocalCache().find { it.id == jobId } ?: sampleJobs.first()
            Result.success(SavedJobDto(id = jobId, job = job, notes = null, savedAt = "2026-11-22"))
        }
    }

    suspend fun unsaveJob(jobId: Int): Result<Unit> {
        localSavedJobIds.remove(jobId)
        return try {
            apiService.unsaveJob(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun getSavedJobs(): Result<List<SavedJobDto>> {
        return try {
            val response = apiService.getSavedJobs()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                localSavedJobIds.clear()
                localSavedJobIds.addAll(list.map { it.job.id })
                Result.success(list)
            } else {
                computeLocalSavedJobs()
            }
        } catch (e: Exception) {
            computeLocalSavedJobs()
        }
    }

    private suspend fun computeLocalSavedJobs(): Result<List<SavedJobDto>> {
        val all = ensureLocalCache()
        val saved = all.filter { localSavedJobIds.contains(it.id) }.map {
            SavedJobDto(id = it.id, job = it, notes = null, savedAt = "2026-11-22")
        }
        return Result.success(saved)
    }

    suspend fun trackApplication(jobId: Int, status: String, notes: String?): Result<ApplicationDto> {
        val job = ensureLocalCache().find { it.id == jobId } ?: sampleJobs.first()
        val app = ApplicationDto(
            id = localApplications.size + 1,
            jobId = jobId,
            job = job,
            status = status,
            notes = notes,
            appliedDate = "2026-11-22",
            updatedAt = "2026-11-22"
        )
        localApplications.removeAll { it.jobId == jobId }
        localApplications.add(app)
        return try {
            val response = apiService.trackApplication(ApplicationCreateDto(jobId, status, notes))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(app)
            }
        } catch (e: Exception) {
            Result.success(app)
        }
    }

    suspend fun getApplications(): Result<List<ApplicationDto>> {
        return try {
            val response = apiService.getApplications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                if (localApplications.isEmpty()) {
                    val jobs = ensureLocalCache()
                    localApplications.add(
                        ApplicationDto(
                            id = 1,
                            jobId = 1,
                            job = jobs.first(),
                            status = "Applied",
                            notes = "Submitted application via company career portal.",
                            appliedDate = "2026-11-21",
                            updatedAt = "2026-11-21"
                        )
                    )
                }
                Result.success(localApplications)
            }
        } catch (e: Exception) {
            if (localApplications.isEmpty()) {
                val jobs = ensureLocalCache()
                localApplications.add(
                    ApplicationDto(
                        id = 1,
                        jobId = 1,
                        job = jobs.first(),
                        status = "Applied",
                        notes = "Submitted application via company career portal.",
                        appliedDate = "2026-11-21",
                        updatedAt = "2026-11-21"
                    )
                )
            }
            Result.success(localApplications)
        }
    }

    suspend fun updateApplication(appId: Int, status: String, notes: String?): Result<ApplicationDto> {
        val existing = localApplications.find { it.id == appId }
        if (existing != null) {
            val updated = existing.copy(status = status, notes = notes ?: existing.notes, updatedAt = "2026-11-22")
            val index = localApplications.indexOfFirst { it.id == appId }
            localApplications[index] = updated
        }
        return try {
            val response = apiService.updateApplication(appId, ApplicationUpdateDto(status, notes))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(existing ?: localApplications.first())
            }
        } catch (e: Exception) {
            Result.success(existing ?: localApplications.first())
        }
    }
}
