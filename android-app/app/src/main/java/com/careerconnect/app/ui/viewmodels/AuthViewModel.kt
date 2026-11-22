package com.careerconnect.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerconnect.app.data.models.LoginRequest
import com.careerconnect.app.data.models.RegisterRequest
import com.careerconnect.app.data.models.TokenResponse
import com.careerconnect.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val tokenResponse: TokenResponse) : AuthUiState()
    data class Message(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val accessTokenFlow = authRepository.accessTokenFlow
    val isOnboardingCompletedFlow = authRepository.isOnboardingCompletedFlow

    fun register(email: String, pass: String, name: String, phone: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(
                RegisterRequest(email = email, password = pass, fullName = name, phoneNumber = phone)
            )
            result.onSuccess {
                _uiState.value = AuthUiState.Success(it)
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Registration failed.")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(LoginRequest(email = email, password = pass))
            result.onSuccess {
                _uiState.value = AuthUiState.Success(it)
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Invalid email or password.")
            }
        }
    }

    fun loginStandalone() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.loginStandalone()
            result.onSuccess {
                _uiState.value = AuthUiState.Success(it)
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Failed to enter standalone mode.")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.forgotPassword(email)
            result.onSuccess {
                _uiState.value = AuthUiState.Message("Password reset request submitted. Check instructions.")
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Failed request.")
            }
        }
    }

    fun resetPassword(token: String, newPass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.resetPassword(token, newPass)
            result.onSuccess {
                _uiState.value = AuthUiState.Message("Password reset successfully. Please log in.")
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Failed password reset.")
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            authRepository.setOnboardingCompleted()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
