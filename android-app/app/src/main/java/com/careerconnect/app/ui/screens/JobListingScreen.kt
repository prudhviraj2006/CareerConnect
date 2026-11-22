package com.careerconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.components.JobCard
import com.careerconnect.app.ui.components.LoadingView
import com.careerconnect.app.ui.theme.BackgroundLight
import com.careerconnect.app.ui.theme.NavyPrimary
import com.careerconnect.app.ui.viewmodels.JobViewModel

@Composable
fun JobListingScreen(
    jobViewModel: JobViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToJobDetails: (Int) -> Unit
) {
    val jobs by jobViewModel.jobs.collectAsState()
    val savedIds by jobViewModel.savedJobIds.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "All Verified Jobs",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { jobViewModel.syncJobs() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = NavyPrimary)
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && jobs.isEmpty()) {
                LoadingView(message = "Fetching verified job listings...")
            } else if (jobs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.WorkOutline,
                    title = "No jobs available",
                    message = "Tap Sync to poll live listings from Remotive & Arbeitnow.",
                    actionButtonText = "Sync Jobs",
                    onActionClick = { jobViewModel.syncJobs() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(jobs) { job ->
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
