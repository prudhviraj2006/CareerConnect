package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.models.NotificationDto
import com.careerconnect.app.data.models.NotificationPreferenceDto

class NotificationRepository(private val apiService: ApiService) {

    private var localNotifications: MutableList<NotificationDto> = mutableListOf(
        NotificationDto(
            id = 1,
            title = "High Match Job Found!",
            message = "We found 'Senior Android Engineer' matching 88% of your verified skills.",
            notificationType = "job_match",
            referenceId = 1,
            isRead = false,
            createdAt = "2026-11-22T10:00:00"
        ),
        NotificationDto(
            id = 2,
            title = "Welcome to CareerConnect",
            message = "Your profile has been activated. Explore genuine jobs and track your skill match scores.",
            notificationType = "system",
            referenceId = null,
            isRead = true,
            createdAt = "2026-11-22T09:30:00"
        ),
        NotificationDto(
            id = 3,
            title = "Skill Recommendation",
            message = "Adding 'Docker' to your skillset can increase your job match scores by up to 15%.",
            notificationType = "recommendation",
            referenceId = 2,
            isRead = false,
            createdAt = "2026-11-22T08:00:00"
        )
    )

    private var localPrefs = NotificationPreferenceDto(
        emailNotifications = true,
        pushNotifications = true,
        jobMatchesEnabled = true,
        applicationUpdatesEnabled = true,
        recommendationsEnabled = true
    )

    suspend fun getNotifications(): Result<List<NotificationDto>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(localNotifications)
            }
        } catch (e: Exception) {
            Result.success(localNotifications)
        }
    }

    suspend fun markAsRead(id: Int): Result<NotificationDto> {
        val notif = localNotifications.find { it.id == id }?.copy(isRead = true)
        if (notif != null) {
            val index = localNotifications.indexOfFirst { it.id == id }
            localNotifications[index] = notif
        }
        return try {
            val response = apiService.markNotificationRead(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(notif ?: localNotifications.first())
            }
        } catch (e: Exception) {
            Result.success(notif ?: localNotifications.first())
        }
    }

    suspend fun markAllAsRead(): Result<Unit> {
        localNotifications = localNotifications.map { it.copy(isRead = true) }.toMutableList()
        return try {
            apiService.markAllNotificationsRead()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun getPreferences(): Result<NotificationPreferenceDto> {
        return try {
            val response = apiService.getNotificationPreferences()
            if (response.isSuccessful && response.body() != null) {
                localPrefs = response.body()!!
                Result.success(localPrefs)
            } else {
                Result.success(localPrefs)
            }
        } catch (e: Exception) {
            Result.success(localPrefs)
        }
    }

    suspend fun updatePreferences(prefs: NotificationPreferenceDto): Result<NotificationPreferenceDto> {
        localPrefs = prefs
        return try {
            val response = apiService.updateNotificationPreferences(prefs)
            if (response.isSuccessful && response.body() != null) {
                localPrefs = response.body()!!
                Result.success(localPrefs)
            } else {
                Result.success(localPrefs)
            }
        } catch (e: Exception) {
            Result.success(localPrefs)
        }
    }
}
