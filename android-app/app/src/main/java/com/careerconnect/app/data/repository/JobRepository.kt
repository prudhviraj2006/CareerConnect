package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.local.JobDao
import com.careerconnect.app.data.local.JobEntity
import com.careerconnect.app.data.models.*

class JobRepository(
    private val apiService: ApiService,
    private val jobDao: JobDao? = null
) {

    private val localSavedJobIds = mutableSetOf<Int>()

    private suspend fun getCachedJobs(): List<JobDto> {
        val cached = jobDao?.getJobList() ?: emptyList()
        return cached.map { mapEntityToDto(it) }
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
            isIndia = it.isIndia,
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
            isIndia = it.isIndia,
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
                jobDao?.insertJobs(entities)
                Result.success(jobs)
            } else {
                val cached = getCachedJobs()
                if (cached.isNotEmpty()) Result.success(cached) else Result.failure(Exception("Failed to load jobs from server."))
            }
        } catch (e: Exception) {
            val cached = getCachedJobs()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
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
                val all = getCachedJobs()
                val filtered = all.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.companyName.contains(query, ignoreCase = true) ||
                    it.requiredSkills.any { s -> s.contains(query, ignoreCase = true) }
                }
                Result.success(filtered)
            }
        } catch (e: Exception) {
            val all = getCachedJobs()
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
                val cached = jobDao?.getJobById(jobId)
                if (cached != null) {
                    Result.success(mapEntityToDto(cached))
                } else {
                    Result.failure(Exception("Job not found."))
                }
            }
        } catch (e: Exception) {
            val cached = jobDao?.getJobById(jobId)
            if (cached != null) {
                Result.success(mapEntityToDto(cached))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun syncJobs(): Result<String> {
        return try {
            val response = apiService.syncJobs()
            if (response.isSuccessful) {
                Result.success("Jobs synchronized successfully from verified providers.")
            } else {
                Result.failure(Exception("Failed to synchronize jobs."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobMatch(jobId: Int): Result<MatchScoreResponseDto> {
        return try {
            val response = apiService.getJobMatch(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Unable to retrieve match score from server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSkillGap(jobId: Int): Result<SkillGapResponseDto> {
        return try {
            val response = apiService.getSkillGap(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Unable to retrieve skill gap analysis from server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecommendations(): Result<List<RecommendationDto>> {
        return try {
            val response = apiService.getRecommendations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Unable to load recommendations."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveJob(jobId: Int): Result<SavedJobDto> {
        localSavedJobIds.add(jobId)
        return try {
            val response = apiService.saveJob(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Could not save job to server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unsaveJob(jobId: Int): Result<Unit> {
        localSavedJobIds.remove(jobId)
        return try {
            apiService.unsaveJob(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.failure(Exception("Failed to fetch saved jobs."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun trackApplication(jobId: Int, status: String, notes: String?): Result<ApplicationDto> {
        return try {
            val response = apiService.trackApplication(ApplicationCreateDto(jobId, status, notes))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to track application."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApplications(): Result<List<ApplicationDto>> {
        return try {
            val response = apiService.getApplications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch applications."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateApplication(appId: Int, status: String, notes: String?): Result<ApplicationDto> {
        return try {
            val response = apiService.updateApplication(appId, ApplicationUpdateDto(status, notes))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update application."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
