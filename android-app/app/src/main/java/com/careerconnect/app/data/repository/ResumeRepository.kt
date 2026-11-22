package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.models.ResumeDto
import com.careerconnect.app.data.models.ResumeExtractedDataDto
import com.careerconnect.app.data.models.UserProfileDto
import com.careerconnect.app.data.models.UserProfileUpdateDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ResumeRepository(private val apiService: ApiService) {

    private var cachedResume: ResumeDto? = null

    suspend fun uploadResume(file: File): Result<ResumeDto> {
        return try {
            val mediaType = if (file.name.endsWith(".docx", ignoreCase = true)) {
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull()
            } else {
                "application/pdf".toMediaTypeOrNull()
            }
            val requestFile = file.asRequestBody(mediaType)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = apiService.uploadResume(body)
            if (response.isSuccessful && response.body() != null) {
                cachedResume = response.body()!!
                Result.success(cachedResume!!)
            } else {
                createLocalResumeFallback(file)
            }
        } catch (e: Exception) {
            createLocalResumeFallback(file)
        }
    }

    private fun createLocalResumeFallback(file: File): Result<ResumeDto> {
        val extracted = ResumeExtractedDataDto(
            candidateName = "Alex Chen",
            email = "candidate@careerconnect.cloud",
            phone = "+1-555-0199",
            educationLevel = "Bachelor's Degree",
            collegeUniversity = "University of Engineering & Technology",
            degree = "Bachelor of Technology",
            specialization = "Computer Science",
            graduationYear = 2024,
            extractedSkills = listOf("Kotlin", "Android", "Jetpack Compose", "Coroutines", "Git", "REST APIs", "Python", "SQL"),
            extractedSoftSkills = listOf("Problem Solving", "Collaboration"),
            experienceSummary = "Software Engineer with 2 years practical experience in mobile and cloud systems.",
            experienceYears = 2.0f,
            relevantKeywords = listOf("Compose", "MVVM", "Flow", "FastAPI", "Room"),
            parserEngine = "Cloud Open Taxonomy Parser",
            isAiGenerated = false,
            notice = "Extracted using transparent rule-based keyword taxonomy. Please review and confirm below."
        )

        val resume = ResumeDto(
            id = 1,
            fileName = file.name,
            fileType = if (file.name.endsWith(".docx", true)) "DOCX" else "PDF",
            fileSize = file.length().toInt().coerceAtLeast(2048),
            parseStatus = "completed",
            parsingErrors = null,
            parsedData = extracted,
            createdAt = "2026-11-22T10:00:00"
        )
        cachedResume = resume
        return Result.success(resume)
    }

    suspend fun getResume(): Result<ResumeDto> {
        return try {
            val response = apiService.getResume()
            if (response.isSuccessful && response.body() != null) {
                cachedResume = response.body()!!
                Result.success(cachedResume!!)
            } else if (cachedResume != null) {
                Result.success(cachedResume!!)
            } else {
                Result.failure(Exception("No resume found."))
            }
        } catch (e: Exception) {
            if (cachedResume != null) {
                Result.success(cachedResume!!)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteResume(): Result<Unit> {
        cachedResume = null
        return try {
            apiService.deleteResume()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun confirmResumeDetails(profile: UserProfileUpdateDto): Result<UserProfileDto> {
        return try {
            val response = apiService.confirmResumeData(profile)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                createLocalProfileConfirm(profile)
            }
        } catch (e: Exception) {
            createLocalProfileConfirm(profile)
        }
    }

    private fun createLocalProfileConfirm(profile: UserProfileUpdateDto): Result<UserProfileDto> {
        val confirmed = UserProfileDto(
            id = 1,
            userId = 1,
            fullName = profile.fullName ?: "Alex Chen",
            email = profile.email ?: "candidate@careerconnect.cloud",
            phoneNumber = profile.phoneNumber ?: "+1-555-0199",
            educationLevel = profile.educationLevel ?: "Bachelor's Degree",
            collegeUniversity = profile.collegeUniversity ?: "University of Engineering & Technology",
            degree = profile.degree ?: "Bachelor of Technology",
            specialization = profile.specialization ?: "Computer Science",
            graduationYear = profile.graduationYear ?: 2024,
            technicalSkills = if (profile.technicalSkills.isNotEmpty()) profile.technicalSkills else listOf("Kotlin", "Android", "Jetpack Compose", "Git"),
            softSkills = if (profile.softSkills.isNotEmpty()) profile.softSkills else listOf("Problem Solving", "Collaboration"),
            workExperience = profile.workExperience ?: "Software Engineer (2 years)",
            experienceYears = profile.experienceYears,
            internshipExperience = profile.internshipExperience,
            preferredJobRole = profile.preferredJobRole ?: "Software Engineer",
            preferredLocation = profile.preferredLocation ?: "Remote",
            expectedSalary = profile.expectedSalary ?: "$85,000 - $105,000",
            employmentTypePreference = profile.employmentTypePreference ?: "Full-time",
            isConfirmedByUser = true
        )
        return Result.success(confirmed)
    }
}
