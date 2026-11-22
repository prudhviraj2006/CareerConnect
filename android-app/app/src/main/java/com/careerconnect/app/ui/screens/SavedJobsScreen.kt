package com.careerconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.components.JobCard
import com.careerconnect.app.ui.theme.BackgroundLight
import com.careerconnect.app.ui.theme.NavyPrimary
import com.careerconnect.app.ui.viewmodels.JobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedJobsScreen(
    jobViewModel: JobViewModel,
    onNavigateToJobDetails: (Int) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val savedJobs by jobViewModel.savedJobs.collectAsState()

    LaunchedEffect(Unit) {
        jobViewModel.loadSavedJobs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Jobs (${savedJobs.size})", color = NavyPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (savedJobs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.BookmarkBorder,
                title = "No Saved Jobs",
                message = "Bookmark genuine job listings to review later and track your applications.",
                actionButtonText = "Explore Jobs",
                onActionClick = onNavigateToSearch
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(savedJobs) { item ->
                    JobCard(
                        job = item.job,
                        onClick = { onNavigateToJobDetails(item.job.id) },
                        onSaveToggle = { jobViewModel.toggleSaveJob(item.job.id) },
                        isSaved = true
                    )
                }
            }
        }
    }
}
