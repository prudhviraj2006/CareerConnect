package com.careerconnect.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerconnect.app.data.models.ResumeDto
import com.careerconnect.app.data.models.UserProfileDto
import com.careerconnect.app.data.models.UserProfileUpdateDto
import com.careerconnect.app.data.repository.ResumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class ResumeUiState {
    object Idle : ResumeUiState()
    object Uploading : ResumeUiState()
    data class Parsed(val resume: ResumeDto) : ResumeUiState()
    data class Confirmed(val profile: UserProfileDto) : ResumeUiState()
    data class Error(val message: String) : ResumeUiState()
}

class ResumeViewModel(private val resumeRepository: ResumeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ResumeUiState>(ResumeUiState.Idle)
    val uiState: StateFlow<ResumeUiState> = _uiState.asStateFlow()

    private val _latestResume = MutableStateFlow<ResumeDto?>(null)
    val latestResume: StateFlow<ResumeDto?> = _latestResume.asStateFlow()

    fun uploadResumeFile(file: File) {
        viewModelScope.launch {
            _uiState.value = ResumeUiState.Uploading
            resumeRepository.uploadResume(file).onSuccess {
                _latestResume.value = it
                _uiState.value = ResumeUiState.Parsed(it)
            }.onFailure {
                _uiState.value = ResumeUiState.Error(it.message ?: "Failed to upload and parse resume.")
            }
        }
    }

    fun loadResume() {
        viewModelScope.launch {
            resumeRepository.getResume().onSuccess {
                _latestResume.value = it
                _uiState.value = ResumeUiState.Parsed(it)
            }.onFailure {
                // No resume uploaded yet
            }
        }
    }

    fun confirmExtractedDetails(profileUpdate: UserProfileUpdateDto, onConfirmed: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ResumeUiState.Uploading
            resumeRepository.confirmResumeDetails(profileUpdate).onSuccess {
                _uiState.value = ResumeUiState.Confirmed(it)
                onConfirmed()
            }.onFailure {
                _uiState.value = ResumeUiState.Error(it.message ?: "Failed to confirm profile information.")
            }
        }
    }

    fun deleteResume() {
        viewModelScope.launch {
            resumeRepository.deleteResume().onSuccess {
                _latestResume.value = null
                _uiState.value = ResumeUiState.Idle
            }
        }
    }
}
