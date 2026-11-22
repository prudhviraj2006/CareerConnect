package com.careerconnect.app

import android.app.Application
import android.util.Log
import com.careerconnect.app.data.api.RetrofitClient
import com.careerconnect.app.data.local.AppDatabase
import com.careerconnect.app.data.preferences.UserPreferencesDataStore
import com.careerconnect.app.data.repository.*

class CareerConnectApp : Application() {

    lateinit var preferencesDataStore: UserPreferencesDataStore
        private set

    var database: AppDatabase? = null
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var profileRepository: ProfileRepository
        private set

    lateinit var resumeRepository: ResumeRepository
        private set

    lateinit var jobRepository: JobRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    override fun onCreate() {
        super.onCreate()

        try {
            preferencesDataStore = UserPreferencesDataStore(this)
        } catch (e: Throwable) {
            Log.e("CareerConnectApp", "PreferencesDataStore init warning: ${e.message}")
        }

        try {
            database = AppDatabase.getInstance(this)
        } catch (e: Throwable) {
            Log.e("CareerConnectApp", "Room database init warning: ${e.message}")
            database = null
        }

        val apiService = RetrofitClient.getApiService(this)

        authRepository = AuthRepository(apiService, preferencesDataStore)
        profileRepository = ProfileRepository(apiService)
        resumeRepository = ResumeRepository(apiService)
        jobRepository = JobRepository(apiService, database?.jobDao())
        notificationRepository = NotificationRepository(apiService)
    }
}
