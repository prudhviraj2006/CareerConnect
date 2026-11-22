package com.careerconnect.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth & Onboarding
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ProfileSetup : Screen("profile_setup")

    // Resume Processing
    object ResumeUpload : Screen("resume_upload")
    object ResumeReview : Screen("resume_review")

    // Core Dashboard & Bottom Nav
    object Home : Screen("home")
    object JobSearch : Screen("job_search")
    object JobFilters : Screen("job_filters")
    object JobListing : Screen("job_listing")
    object SavedJobs : Screen("saved_jobs")
    object ApplicationTracking : Screen("application_tracking")
    object Notifications : Screen("notifications")
    object UserProfile : Screen("user_profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")

    // Matching & Details
    object JobDetails : Screen("job_details/{jobId}") {
        fun createRoute(jobId: Int) = "job_details/$jobId"
    }
    object MatchScoreDetails : Screen("match_score_details/{jobId}") {
        fun createRoute(jobId: Int) = "match_score_details/$jobId"
    }
    object SkillGap : Screen("skill_gap/{jobId}") {
        fun createRoute(jobId: Int) = "skill_gap/$jobId"
    }
    object RecommendedJobs : Screen("recommended_jobs")
}
