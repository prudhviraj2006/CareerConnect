package com.careerconnect.app.data.models

import com.google.gson.annotations.SerializedName

data class MatchScoreResponseDto(
    @SerializedName("job_id") val jobId: Int,
    @SerializedName("overall_match_score") val overallMatchScore: Float,
    @SerializedName("has_sufficient_info") val hasSufficientInfo: Boolean,
    @SerializedName("matching_skills") val matchingSkills: List<String> = emptyList(),
    @SerializedName("missing_skills") val missingSkills: List<String> = emptyList(),
    @SerializedName("breakdown") val breakdown: MatchScoreBreakdownDto,
    @SerializedName("explanation_summary") val explanationSummary: String,
    @SerializedName("recommendation_tier") val recommendationTier: String
)

data class MatchScoreBreakdownDto(
    @SerializedName("skills_score") val skillsScore: Float,
    @SerializedName("skills_weight") val skillsWeight: Float = 60f,
    @SerializedName("experience_score") val experienceScore: Float,
    @SerializedName("experience_weight") val experienceWeight: Float = 20f,
    @SerializedName("education_score") val educationScore: Float,
    @SerializedName("education_weight") val educationWeight: Float = 10f,
    @SerializedName("location_preference_score") val locationPreferenceScore: Float,
    @SerializedName("location_preference_weight") val locationPreferenceWeight: Float = 10f
)

data class SkillGapResponseDto(
    @SerializedName("job_id") val jobId: Int,
    @SerializedName("job_title") val jobTitle: String,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("user_skills") val userSkills: List<String> = emptyList(),
    @SerializedName("job_required_skills") val jobRequiredSkills: List<String> = emptyList(),
    @SerializedName("matching_skills") val matchingSkills: List<String> = emptyList(),
    @SerializedName("missing_skills") val missingSkills: List<String> = emptyList(),
    @SerializedName("improvement_advice") val improvementAdvice: String,
    @SerializedName("compatibility_boost_potential") val compatibilityBoostPotential: Float
)

data class RecommendationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("job") val job: JobDto,
    @SerializedName("match_score") val matchScore: Float,
    @SerializedName("recommendation_reason") val recommendationReason: String,
    @SerializedName("rank_order") val rankOrder: Int,
    @SerializedName("created_at") val createdAt: String
)
