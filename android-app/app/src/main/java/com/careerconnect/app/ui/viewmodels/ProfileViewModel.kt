package com.careerconnect.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerconnect.app.data.models.UserProfileDto
import com.careerconnect.app.data.models.UserProfileUpdateDto
import com.careerconnect.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfileDto) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfileDto?>(null)
    val currentProfile: StateFlow<UserProfileDto?> = _currentProfile.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.getProfile().onSuccess {
                _currentProfile.value = it
                _uiState.value = ProfileUiState.Success(it)
            }.onFailure {
                _uiState.value = ProfileUiState.Error(it.message ?: "Failed to load profile.")
            }
        }
    }

    fun updateProfile(dto: UserProfileUpdateDto, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.updateProfile(dto).onSuccess {
                _currentProfile.value = it
                _uiState.value = ProfileUiState.Success(it)
                onSaved()
            }.onFailure {
                _uiState.value = ProfileUiState.Error(it.message ?: "Failed to save profile.")
            }
        }
    }
}
