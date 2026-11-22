package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.models.UserProfileDto
import com.careerconnect.app.data.models.UserProfileUpdateDto

class ProfileRepository(private val apiService: ApiService) {

    private var standaloneProfile: UserProfileDto = UserProfileDto(
        id = 1,
        userId = 1,
        fullName = "Alex Chen",
        email = "candidate@careerconnect.cloud",
        phoneNumber = "+1-555-0199",
        educationLevel = "Bachelor's Degree",
        collegeUniversity = "University of Engineering & Technology",
        degree = "Bachelor of Technology",
        specialization = "Computer Science",
        graduationYear = 2024,
        technicalSkills = listOf("Kotlin", "Python", "SQL", "Git", "REST APIs", "Jetpack Compose"),
        softSkills = listOf("Problem Solving", "Team Collaboration", "Communication"),
        workExperience = "Software Development Engineer (2 years experience)",
        experienceYears = 2.0f,
        internshipExperience = "Software Development Intern at TechLabs",
        preferredJobRole = "Software Engineer",
        preferredLocation = "Remote",
        expectedSalary = "$85,000 - $105,000",
        employmentTypePreference = "Full-time",
        isConfirmedByUser = true
    )

    suspend fun getProfile(): Result<UserProfileDto> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                standaloneProfile = response.body()!!
                Result.success(standaloneProfile)
            } else {
                Result.success(standaloneProfile)
            }
        } catch (e: Exception) {
            Result.success(standaloneProfile)
        }
    }

    suspend fun updateProfile(dto: UserProfileUpdateDto): Result<UserProfileDto> {
        return try {
            val response = apiService.updateProfile(dto)
            if (response.isSuccessful && response.body() != null) {
                standaloneProfile = response.body()!!
                Result.success(standaloneProfile)
            } else {
                applyLocalUpdate(dto)
                Result.success(standaloneProfile)
            }
        } catch (e: Exception) {
            applyLocalUpdate(dto)
            Result.success(standaloneProfile)
        }
    }

    private fun applyLocalUpdate(dto: UserProfileUpdateDto) {
        standaloneProfile = standaloneProfile.copy(
            fullName = dto.fullName ?: standaloneProfile.fullName,
            email = dto.email ?: standaloneProfile.email,
            phoneNumber = dto.phoneNumber ?: standaloneProfile.phoneNumber,
            educationLevel = dto.educationLevel ?: standaloneProfile.educationLevel,
            collegeUniversity = dto.collegeUniversity ?: standaloneProfile.collegeUniversity,
            degree = dto.degree ?: standaloneProfile.degree,
            specialization = dto.specialization ?: standaloneProfile.specialization,
            graduationYear = dto.graduationYear ?: standaloneProfile.graduationYear,
            experienceYears = if (dto.experienceYears > 0f) dto.experienceYears else standaloneProfile.experienceYears,
            workExperience = dto.workExperience ?: standaloneProfile.workExperience,
            internshipExperience = dto.internshipExperience ?: standaloneProfile.internshipExperience,
            preferredJobRole = dto.preferredJobRole ?: standaloneProfile.preferredJobRole,
            preferredLocation = dto.preferredLocation ?: standaloneProfile.preferredLocation,
            expectedSalary = dto.expectedSalary ?: standaloneProfile.expectedSalary,
            employmentTypePreference = dto.employmentTypePreference ?: standaloneProfile.employmentTypePreference,
            technicalSkills = if (dto.technicalSkills.isNotEmpty()) dto.technicalSkills else standaloneProfile.technicalSkills,
            softSkills = if (dto.softSkills.isNotEmpty()) dto.softSkills else standaloneProfile.softSkills,
            isConfirmedByUser = true
        )
    }
}
