package com.careerconnect.app.data.models

import com.google.gson.annotations.SerializedName

data class JobDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("company_logo") val companyLogo: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("employment_type") val employmentType: String? = null,
    @SerializedName("experience_level") val experienceLevel: String? = null,
    @SerializedName("experience_years_required") val experienceYearsRequired: Float? = null,
    @SerializedName("min_education") val minEducation: String? = null,
    @SerializedName("salary") val salary: String? = null,
    @SerializedName("publication_date") val publicationDate: String? = null,
    @SerializedName("original_url") val originalUrl: String,
    @SerializedName("is_remote") val isRemote: Boolean = false,
    @SerializedName("is_india") val isIndia: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("source_name") val sourceName: String? = "Direct Provider",
    @SerializedName("required_skills") val requiredSkills: List<String> = emptyList(),
    @SerializedName("match_score") val matchScore: Float? = null,
    @SerializedName("description") val description: String? = null
)

data class SavedJobDto(
    @SerializedName("id") val id: Int,
    @SerializedName("job") val job: JobDto,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("saved_at") val savedAt: String
)

data class ApplicationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("job_id") val jobId: Int,
    @SerializedName("job") val job: JobDto,
    @SerializedName("status") val status: String, // Interested, Applied, Interview, Rejected, Offer
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("applied_date") val appliedDate: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class ApplicationCreateDto(
    @SerializedName("job_id") val jobId: Int,
    @SerializedName("status") val status: String = "Applied",
    @SerializedName("notes") val notes: String? = null
)

data class ApplicationUpdateDto(
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String? = null
)
