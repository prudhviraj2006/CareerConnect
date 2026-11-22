package com.careerconnect.app.data.models

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("notification_type") val notificationType: String,
    @SerializedName("reference_id") val referenceId: Int? = null,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String
)

data class NotificationPreferenceDto(
    @SerializedName("email_notifications") val emailNotifications: Boolean = true,
    @SerializedName("push_notifications") val pushNotifications: Boolean = true,
    @SerializedName("job_matches_enabled") val jobMatchesEnabled: Boolean = true,
    @SerializedName("application_updates_enabled") val applicationUpdatesEnabled: Boolean = true,
    @SerializedName("recommendations_enabled") val recommendationsEnabled: Boolean = true
)
