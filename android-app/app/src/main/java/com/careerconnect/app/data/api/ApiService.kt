package com.careerconnect.app.data.api

import com.careerconnect.app.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Auth ---
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<TokenResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Map<String, Any>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Map<String, Any>>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    // --- Profile ---
    @GET("api/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("api/profile")
    suspend fun updateProfile(@Body profile: UserProfileUpdateDto): Response<UserProfileDto>

    // --- Resume ---
    @Multipart
    @POST("api/resume/upload")
    suspend fun uploadResume(@Part file: MultipartBody.Part): Response<ResumeDto>

    @GET("api/resume")
    suspend fun getResume(): Response<ResumeDto>

    @DELETE("api/resume")
    suspend fun deleteResume(): Response<Map<String, String>>

    @POST("api/resume/confirm")
    suspend fun confirmResumeData(@Body profile: UserProfileUpdateDto): Response<UserProfileDto>

    // --- Jobs ---
    @GET("api/jobs")
    suspend fun getJobs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort_by") sortBy: String = "date",
        @Query("location") location: String? = null,
        @Query("experience_level") experienceLevel: String? = null,
        @Query("employment_type") employmentType: String? = null,
        @Query("is_remote") isRemote: Boolean? = null
    ): Response<List<JobDto>>

    @GET("api/jobs/search")
    suspend fun searchJobs(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("location") location: String? = null,
        @Query("employment_type") employmentType: String? = null,
        @Query("is_remote") isRemote: Boolean? = null
    ): Response<List<JobDto>>

    @GET("api/jobs/{id}")
    suspend fun getJobDetails(@Path("id") jobId: Int): Response<JobDto>

    @POST("api/jobs/sync")
    suspend fun syncJobs(@Query("limit_per_provider") limit: Int = 20): Response<Map<String, Any>>

    // --- Matching & Skill Gap ---
    @GET("api/matches/{job_id}")
    suspend fun getJobMatch(@Path("job_id") jobId: Int): Response<MatchScoreResponseDto>

    @GET("api/skill-gaps/{job_id}")
    suspend fun getSkillGap(@Path("job_id") jobId: Int): Response<SkillGapResponseDto>

    // --- Recommendations ---
    @GET("api/recommendations")
    suspend fun getRecommendations(): Response<List<RecommendationDto>>

    // --- Saved Jobs ---
    @POST("api/saved-jobs/{job_id}")
    suspend fun saveJob(@Path("job_id") jobId: Int): Response<SavedJobDto>

    @DELETE("api/saved-jobs/{job_id}")
    suspend fun unsaveJob(@Path("job_id") jobId: Int): Response<Map<String, String>>

    @GET("api/saved-jobs")
    suspend fun getSavedJobs(): Response<List<SavedJobDto>>

    // --- Application Tracking ---
    @POST("api/applications")
    suspend fun trackApplication(@Body request: ApplicationCreateDto): Response<ApplicationDto>

    @GET("api/applications")
    suspend fun getApplications(): Response<List<ApplicationDto>>

    @PUT("api/applications/{id}")
    suspend fun updateApplication(
        @Path("id") applicationId: Int,
        @Body request: ApplicationUpdateDto
    ): Response<ApplicationDto>

    // --- Notifications ---
    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") notificationId: Int): Response<NotificationDto>

    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Map<String, String>>

    @GET("api/notifications/preferences")
    suspend fun getNotificationPreferences(): Response<NotificationPreferenceDto>

    @PUT("api/notifications/preferences")
    suspend fun updateNotificationPreferences(@Body preferences: NotificationPreferenceDto): Response<NotificationPreferenceDto>
}
