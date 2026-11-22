package com.careerconnect.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerconnect.app.data.models.MatchScoreResponseDto
import com.careerconnect.app.data.models.RecommendationDto
import com.careerconnect.app.data.models.SkillGapResponseDto
import com.careerconnect.app.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchViewModel(private val jobRepository: JobRepository) : ViewModel() {

    private val _matchDetails = MutableStateFlow<MatchScoreResponseDto?>(null)
    val matchDetails: StateFlow<MatchScoreResponseDto?> = _matchDetails.asStateFlow()

    private val _skillGap = MutableStateFlow<SkillGapResponseDto?>(null)
    val skillGap: StateFlow<SkillGapResponseDto?> = _skillGap.asStateFlow()

    private val _recommendations = MutableStateFlow<List<RecommendationDto>>(emptyList())
    val recommendations: StateFlow<List<RecommendationDto>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadMatchScore(jobId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            jobRepository.getJobMatch(jobId).onSuccess {
                _matchDetails.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to calculate match score."
            }
            _isLoading.value = false
        }
    }

    fun loadSkillGap(jobId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            jobRepository.getSkillGap(jobId).onSuccess {
                _skillGap.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to load skill gap analysis."
            }
            _isLoading.value = false
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            jobRepository.getRecommendations().onSuccess {
                _recommendations.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to fetch recommendations."
            }
            _isLoading.value = false
        }
    }
}
