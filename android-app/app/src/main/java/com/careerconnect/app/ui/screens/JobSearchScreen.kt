package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.components.JobCard
import com.careerconnect.app.ui.components.LoadingView
import com.careerconnect.app.ui.theme.AccentBlue
import com.careerconnect.app.ui.theme.BackgroundLight
import com.careerconnect.app.ui.theme.NavyPrimary
import com.careerconnect.app.ui.theme.TextSecondaryLight
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

    var activeCityChip by remember { mutableStateOf("All India") }

    val quickChips = listOf(
        "All India",
        "Bengaluru",
        "Hyderabad",
        "Pune",
        "Delhi NCR",
        "Remote",
        "Fresher"
    )

    val baseJobs = if (searchQuery.isNotBlank()) searchResults else allJobs
    val displayJobs = remember(baseJobs, activeCityChip, filterState) {
        baseJobs.filter { job ->
            val matchesCity = when (activeCityChip) {
                "All India" -> true
                "Remote" -> job.isRemote || job.location?.contains("remote", ignoreCase = true) == true
                "Fresher" -> job.experienceLevel?.contains("fresher", ignoreCase = true) == true ||
                        (job.experienceYearsRequired ?: 0f) <= 1.0f ||
                        job.title.contains("intern", ignoreCase = true) ||
                        job.title.contains("graduate", ignoreCase = true)
                else -> job.location?.contains(activeCityChip, ignoreCase = true) == true
            }
            matchesCity
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Jobs (India)", color = NavyPrimary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToFilters) {
                        BadgedBox(
                            badge = {
                                if (filterState.location.isNotBlank() || filterState.isRemoteOnly || filterState.isFresherOnly || filterState.isInternshipOnly) {
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
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isNotBlank()) {
                            jobViewModel.searchJobs(it)
                        }
                    },
                    placeholder = { Text("Search title, company, or skills (e.g. Kotlin, Python)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NavyPrimary,
                        unfocusedTextColor = NavyPrimary,
                        focusedBorderColor = AccentBlue
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Quick Location & Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickChips.forEach { chip ->
                    val isSelected = activeCityChip == chip
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeCityChip = chip },
                        label = { Text(chip, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isLoading) {
                LoadingView(message = "Searching verified job postings in India...")
            } else if (displayJobs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = "No jobs found",
                    message = if (searchQuery.isNotBlank())
                        "No genuine Indian jobs matched '$searchQuery'. Try adjusting your keywords or city filter."
                    else
                        "No jobs found in $activeCityChip. Tap 'All India' or use Filters to broaden your search."
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
