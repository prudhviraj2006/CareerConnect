package com.careerconnect.app.data.models

import com.google.gson.annotations.SerializedName

data class ResumeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("file_size") val fileSize: Int,
    @SerializedName("parse_status") val parseStatus: String,
    @SerializedName("parsing_errors") val parsingErrors: String? = null,
    @SerializedName("parsed_data") val parsedData: ResumeExtractedDataDto? = null,
    @SerializedName("created_at") val createdAt: String
)

data class ResumeExtractedDataDto(
    @SerializedName("candidate_name") val candidateName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("education_level") val educationLevel: String? = null,
    @SerializedName("college_university") val collegeUniversity: String? = null,
    @SerializedName("degree") val degree: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("extracted_skills") val extractedSkills: List<String> = emptyList(),
    @SerializedName("extracted_soft_skills") val extractedSoftSkills: List<String> = emptyList(),
    @SerializedName("experience_summary") val experienceSummary: String? = null,
    @SerializedName("experience_years") val experienceYears: Float = 0f,
    @SerializedName("internship_experience") val internshipExperience: String? = null,
    @SerializedName("projects") val projects: String? = null,
    @SerializedName("certifications") val certifications: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("relevant_keywords") val relevantKeywords: List<String> = emptyList(),
    @SerializedName("parser_engine") val parserEngine: String? = null,
    @SerializedName("is_ai_generated") val isAiGenerated: Boolean = false,
    @SerializedName("notice") val notice: String? = null
)
