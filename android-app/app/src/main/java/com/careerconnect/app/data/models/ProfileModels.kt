package com.careerconnect.app.data.models

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("education_level") val educationLevel: String? = null,
    @SerializedName("college_university") val collegeUniversity: String? = null,
    @SerializedName("degree") val degree: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("technical_skills") val technicalSkills: List<String> = emptyList(),
    @SerializedName("soft_skills") val softSkills: List<String> = emptyList(),
    @SerializedName("work_experience") val workExperience: String? = null,
    @SerializedName("experience_years") val experienceYears: Float = 0f,
    @SerializedName("internship_experience") val internshipExperience: String? = null,
    @SerializedName("preferred_job_role") val preferredJobRole: String? = null,
    @SerializedName("preferred_location") val preferredLocation: String? = null,
    @SerializedName("expected_salary") val expectedSalary: String? = null,
    @SerializedName("employment_type_preference") val employmentTypePreference: String? = null,
    @SerializedName("is_confirmed_by_user") val isConfirmedByUser: Boolean = false
)

data class UserProfileUpdateDto(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("education_level") val educationLevel: String? = null,
    @SerializedName("college_university") val collegeUniversity: String? = null,
    @SerializedName("degree") val degree: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("technical_skills") val technicalSkills: List<String> = emptyList(),
    @SerializedName("soft_skills") val softSkills: List<String> = emptyList(),
    @SerializedName("work_experience") val workExperience: String? = null,
    @SerializedName("experience_years") val experienceYears: Float = 0f,
    @SerializedName("internship_experience") val internshipExperience: String? = null,
    @SerializedName("preferred_job_role") val preferredJobRole: String? = null,
    @SerializedName("preferred_location") val preferredLocation: String? = null,
    @SerializedName("expected_salary") val expectedSalary: String? = null,
    @SerializedName("employment_type_preference") val employmentTypePreference: String? = null,
    @SerializedName("is_confirmed_by_user") val isConfirmedByUser: Boolean = true
)
