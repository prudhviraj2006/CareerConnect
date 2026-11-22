package com.careerconnect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.careerconnect.app.ui.navigation.CareerConnectNavGraph
import com.careerconnect.app.ui.theme.CareerConnectTheme
import com.careerconnect.app.ui.viewmodels.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as CareerConnectApp

        val authViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(app.authRepository) as T
            }
        })[AuthViewModel::class.java]

        val profileViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(app.profileRepository) as T
            }
        })[ProfileViewModel::class.java]

        val resumeViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ResumeViewModel(app.resumeRepository) as T
            }
        })[ResumeViewModel::class.java]

        val jobViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return JobViewModel(app.jobRepository) as T
            }
        })[JobViewModel::class.java]

        val matchViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MatchViewModel(app.jobRepository) as T
            }
        })[MatchViewModel::class.java]

        val notificationViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return NotificationViewModel(app.notificationRepository) as T
            }
        })[NotificationViewModel::class.java]

        setContent {
            CareerConnectTheme {
                CareerConnectNavGraph(
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    resumeViewModel = resumeViewModel,
                    jobViewModel = jobViewModel,
                    matchViewModel = matchViewModel,
                    notificationViewModel = notificationViewModel
                )
            }
        }
    }
}
