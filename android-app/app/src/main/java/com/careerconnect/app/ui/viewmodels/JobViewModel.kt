package com.careerconnect.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.careerconnect.app.data.models.ApplicationDto
import com.careerconnect.app.data.models.JobDto
import com.careerconnect.app.data.models.SavedJobDto
import com.careerconnect.app.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobFilterState(
    val location: String = "",
    val state: String = "",
    val city: String = "",
    val experienceLevel: String = "",
    val employmentType: String = "",
    val isRemoteOnly: Boolean = false,
    val isFresherOnly: Boolean = false,
    val isInternshipOnly: Boolean = false,
    val includeInternational: Boolean = false,
    val sortBy: String = "date" // date, relevance
)

class JobViewModel(private val jobRepository: JobRepository) : ViewModel() {

    private val _jobs = MutableStateFlow<List<JobDto>>(emptyList())
    val jobs: StateFlow<List<JobDto>> = _jobs.asStateFlow()

    private val _searchResults = MutableStateFlow<List<JobDto>>(emptyList())
    val searchResults: StateFlow<List<JobDto>> = _searchResults.asStateFlow()

    private val _savedJobs = MutableStateFlow<List<SavedJobDto>>(emptyList())
    val savedJobs: StateFlow<List<SavedJobDto>> = _savedJobs.asStateFlow()

    private val _applications = MutableStateFlow<List<ApplicationDto>>(emptyList())
    val applications: StateFlow<List<ApplicationDto>> = _applications.asStateFlow()

    private val _selectedJob = MutableStateFlow<JobDto?>(null)
    val selectedJob: StateFlow<JobDto?> = _selectedJob.asStateFlow()

    private val _filterState = MutableStateFlow(JobFilterState())
    val filterState: StateFlow<JobFilterState> = _filterState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _savedJobIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedJobIds: StateFlow<Set<Int>> = _savedJobIds.asStateFlow()

    init {
        loadJobs()
        loadSavedJobs()
        loadApplications()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val f = _filterState.value
            jobRepository.getJobs(
                location = f.location.takeIf { it.isNotBlank() },
                experienceLevel = f.experienceLevel.takeIf { it.isNotBlank() },
                employmentType = f.employmentType.takeIf { it.isNotBlank() },
                isRemote = if (f.isRemoteOnly) true else null,
                sortBy = f.sortBy
            ).onSuccess {
                _jobs.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to load jobs."
            }
            _isLoading.value = false
        }
    }

    fun searchJobs(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val f = _filterState.value
            jobRepository.searchJobs(
                query = query,
                location = f.location.takeIf { it.isNotBlank() },
                employmentType = f.employmentType.takeIf { it.isNotBlank() },
                isRemote = if (f.isRemoteOnly) true else null
            ).onSuccess {
                _searchResults.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Search failed."
            }
            _isLoading.value = false
        }
    }

    fun loadJobDetails(jobId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            jobRepository.getJobDetails(jobId).onSuccess {
                _selectedJob.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to load job details."
            }
            _isLoading.value = false
        }
    }

    fun syncJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            jobRepository.syncJobs().onSuccess {
                loadJobs()
            }.onFailure {
                _errorMessage.value = "Sync failed."
            }
            _isLoading.value = false
        }
    }

    fun updateFilters(newFilters: JobFilterState) {
        _filterState.value = newFilters
        loadJobs()
    }

    fun resetFilters() {
        _filterState.value = JobFilterState()
        loadJobs()
    }

    fun loadSavedJobs() {
        viewModelScope.launch {
            jobRepository.getSavedJobs().onSuccess { list ->
                _savedJobs.value = list
                _savedJobIds.value = list.map { it.job.id }.toSet()
            }
        }
    }

    fun toggleSaveJob(jobId: Int) {
        viewModelScope.launch {
            if (_savedJobIds.value.contains(jobId)) {
                jobRepository.unsaveJob(jobId).onSuccess {
                    loadSavedJobs()
                }
            } else {
                jobRepository.saveJob(jobId).onSuccess {
                    loadSavedJobs()
                }
            }
        }
    }

    fun loadApplications() {
        viewModelScope.launch {
            jobRepository.getApplications().onSuccess {
                _applications.value = it
            }
        }
    }

    fun trackApplication(jobId: Int, status: String, notes: String?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            jobRepository.trackApplication(jobId, status, notes).onSuccess {
                loadApplications()
                onComplete()
            }
        }
    }

    fun updateApplicationStatus(appId: Int, newStatus: String, notes: String?) {
        viewModelScope.launch {
            jobRepository.updateApplication(appId, newStatus, notes).onSuccess {
                loadApplications()
            }
        }
    }
}
