package com.careerconnect.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.careerconnect.app.ui.screens.*
import com.careerconnect.app.ui.viewmodels.*

@Composable
fun CareerConnectNavGraph(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    resumeViewModel: ResumeViewModel,
    jobViewModel: JobViewModel,
    matchViewModel: MatchViewModel,
    notificationViewModel: NotificationViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.JobSearch.route,
        Screen.SavedJobs.route,
        Screen.Notifications.route,
        Screen.UserProfile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    val token by authViewModel.accessTokenFlow.collectAsState(initial = null)
    val isOnboardingCompleted by authViewModel.isOnboardingCompletedFlow.collectAsState(initial = false)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CareerConnectBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Splash
            composable(Screen.Splash.route) {
                SplashScreen(
                    isLoggedIn = !token.isNullOrBlank(),
                    isOnboardingCompleted = isOnboardingCompleted,
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        authViewModel.completeOnboarding()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // 3. Login
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // 4. Register
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // 5. Forgot Password
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 6. Profile Setup
            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(
                    profileViewModel = profileViewModel,
                    onNavigateToResumeUpload = { navController.navigate(Screen.ResumeUpload.route) },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                        }
                    }
                )
            }

            // 7. Resume Upload
            composable(Screen.ResumeUpload.route) {
                ResumeUploadScreen(
                    resumeViewModel = resumeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReview = { navController.navigate(Screen.ResumeReview.route) }
                )
            }

            // 8. Resume Review
            composable(Screen.ResumeReview.route) {
                ResumeReviewScreen(
                    resumeViewModel = resumeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onConfirmComplete = {
                        profileViewModel.loadProfile()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ResumeUpload.route) { inclusive = true }
                        }
                    }
                )
            }

            // 9. Home Dashboard
            composable(Screen.Home.route) {
                HomeScreen(
                    jobViewModel = jobViewModel,
                    matchViewModel = matchViewModel,
                    profileViewModel = profileViewModel,
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    },
                    onNavigateToRecommendations = { navController.navigate(Screen.RecommendedJobs.route) },
                    onNavigateToSaved = { navController.navigate(Screen.SavedJobs.route) },
                    onNavigateToApplications = { navController.navigate(Screen.ApplicationTracking.route) },
                    onNavigateToProfile = { navController.navigate(Screen.UserProfile.route) },
                    onNavigateToResumeUpload = { navController.navigate(Screen.ResumeUpload.route) }
                )
            }

            // 10. Job Search
            composable(Screen.JobSearch.route) {
                JobSearchScreen(
                    jobViewModel = jobViewModel,
                    onNavigateToFilters = { navController.navigate(Screen.JobFilters.route) },
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    }
                )
            }

            // 11. Job Filters
            composable(Screen.JobFilters.route) {
                JobFiltersScreen(
                    jobViewModel = jobViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 12. Job Listing
            composable(Screen.JobListing.route) {
                JobListingScreen(
                    jobViewModel = jobViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    }
                )
            }

            // 13. Job Details
            composable(
                route = Screen.JobDetails.route,
                arguments = listOf(navArgument("jobId") { type = NavType.IntType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getInt("jobId") ?: return@composable
                JobDetailsScreen(
                    jobId = jobId,
                    jobViewModel = jobViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMatchScore = { id ->
                        navController.navigate(Screen.MatchScoreDetails.createRoute(id))
                    },
                    onNavigateToSkillGap = { id ->
                        navController.navigate(Screen.SkillGap.createRoute(id))
                    }
                )
            }

            // 14. Match Score Details
            composable(
                route = Screen.MatchScoreDetails.route,
                arguments = listOf(navArgument("jobId") { type = NavType.IntType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getInt("jobId") ?: return@composable
                MatchScoreDetailsScreen(
                    jobId = jobId,
                    matchViewModel = matchViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 15. Recommended Jobs
            composable(Screen.RecommendedJobs.route) {
                RecommendedJobsScreen(
                    matchViewModel = matchViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    }
                )
            }

            // 16. Skill Gap Analysis
            composable(
                route = Screen.SkillGap.route,
                arguments = listOf(navArgument("jobId") { type = NavType.IntType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getInt("jobId") ?: return@composable
                SkillGapScreen(
                    jobId = jobId,
                    matchViewModel = matchViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 17. Saved Jobs
            composable(Screen.SavedJobs.route) {
                SavedJobsScreen(
                    jobViewModel = jobViewModel,
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    },
                    onNavigateToSearch = { navController.navigate(Screen.JobSearch.route) }
                )
            }

            // 18. Application Tracking
            composable(Screen.ApplicationTracking.route) {
                ApplicationTrackingScreen(
                    jobViewModel = jobViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    }
                )
            }

            // 19. Notifications
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    notificationViewModel = notificationViewModel,
                    onNavigateToJobDetails = { jobId ->
                        navController.navigate(Screen.JobDetails.createRoute(jobId))
                    }
                )
            }

            // 20. User Profile
            composable(Screen.UserProfile.route) {
                UserProfileScreen(
                    profileViewModel = profileViewModel,
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToResumeUpload = { navController.navigate(Screen.ResumeUpload.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // 21. Edit Profile
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    profileViewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 22. Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    authViewModel = authViewModel,
                    notificationViewModel = notificationViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
