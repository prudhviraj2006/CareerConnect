package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.components.JobCard
import com.careerconnect.app.ui.components.LoadingView
import com.careerconnect.app.ui.theme.AccentBlue
import com.careerconnect.app.ui.theme.BackgroundLight
import com.careerconnect.app.ui.theme.NavyPrimary
import com.careerconnect.app.ui.viewmodels.JobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSearchScreen(
    jobViewModel: JobViewModel,
    onNavigateToFilters: () -> Unit,
    onNavigateToJobDetails: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by jobViewModel.searchResults.collectAsState()
    val allJobs by jobViewModel.jobs.collectAsState()
    val savedIds by jobViewModel.savedJobIds.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val filterState by jobViewModel.filterState.collectAsState()

    val displayJobs = if (searchQuery.isNotBlank()) searchResults else allJobs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Jobs", color = NavyPrimary) },
                actions = {
                    IconButton(onClick = onNavigateToFilters) {
                        BadgedBox(
                            badge = {
                                if (filterState.location.isNotBlank() || filterState.isRemoteOnly || filterState.employmentType.isNotBlank()) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = NavyPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isNotBlank()) {
                            jobViewModel.searchJobs(it)
                        }
                    },
                    placeholder = { Text("Search title, company, or skill (e.g. Kotlin)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (isLoading) {
                LoadingView(message = "Searching verified job postings...")
            } else if (displayJobs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = "No jobs found",
                    message = if (searchQuery.isNotBlank())
                        "No genuine jobs matched '$searchQuery'. Try adjusting your search query or filters."
                    else
                        "No jobs available right now. Tap Sync to update listings."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayJobs) { job ->
                        JobCard(
                            job = job,
                            onClick = { onNavigateToJobDetails(job.id) },
                            onSaveToggle = { jobViewModel.toggleSaveJob(job.id) },
                            isSaved = savedIds.contains(job.id)
                        )
                    }
                }
            }
        }
    }
}
