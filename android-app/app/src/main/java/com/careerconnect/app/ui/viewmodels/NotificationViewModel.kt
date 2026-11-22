package com.careerconnect.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerconnect.app.data.models.NotificationDto
import com.careerconnect.app.data.models.NotificationPreferenceDto
import com.careerconnect.app.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications.asStateFlow()

    private val _preferences = MutableStateFlow(NotificationPreferenceDto())
    val preferences: StateFlow<NotificationPreferenceDto> = _preferences.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
        loadPreferences()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            notificationRepository.getNotifications().onSuccess {
                _notifications.value = it
            }
            _isLoading.value = false
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id).onSuccess {
                loadNotifications()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead().onSuccess {
                loadNotifications()
            }
        }
    }

    fun loadPreferences() {
        viewModelScope.launch {
            notificationRepository.getPreferences().onSuccess {
                _preferences.value = it
            }
        }
    }

    fun updatePreferences(newPrefs: NotificationPreferenceDto) {
        viewModelScope.launch {
            notificationRepository.updatePreferences(newPrefs).onSuccess {
                _preferences.value = it
            }
        }
    }
}
