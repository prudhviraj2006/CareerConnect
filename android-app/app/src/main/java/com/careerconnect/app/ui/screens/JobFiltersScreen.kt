package com.careerconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.JobFilterState
import com.careerconnect.app.ui.viewmodels.JobViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobFiltersScreen(
    jobViewModel: JobViewModel,
    onNavigateBack: () -> Unit
) {
    val currentFilter by jobViewModel.filterState.collectAsState()

    var location by remember(currentFilter) { mutableStateOf(currentFilter.location) }
    var experienceLevel by remember(currentFilter) { mutableStateOf(currentFilter.experienceLevel) }
    var employmentType by remember(currentFilter) { mutableStateOf(currentFilter.employmentType) }
    var isRemoteOnly by remember(currentFilter) { mutableStateOf(currentFilter.isRemoteOnly) }
    var sortBy by remember(currentFilter) { mutableStateOf(currentFilter.sortBy) }

    val expLevels = listOf("Entry-level", "Mid-level", "Senior")
    val empTypes = listOf("Full-time", "Part-time", "Contract", "Internship")

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Job Filters",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(onClick = {
                        jobViewModel.resetFilters()
                        onNavigateBack()
                    }) {
                        Text("Reset", color = TextSecondaryLight)
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Location
                Text("Location", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("e.g. Remote, Berlin, New York") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Remote Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Remote Opportunities", fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        Text("Show only 100% remote positions", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = isRemoteOnly,
                        onCheckedChange = { isRemoteOnly = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Experience Level
                Text("Experience Level", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    expLevels.forEach { lvl ->
                        FilterChip(
                            selected = experienceLevel.equals(lvl, ignoreCase = true),
                            onClick = {
                                experienceLevel = if (experienceLevel.equals(lvl, ignoreCase = true)) "" else lvl
                            },
                            label = { Text(lvl) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Employment Type
                Text("Employment Type", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    empTypes.forEach { type ->
                        FilterChip(
                            selected = employmentType.equals(type, ignoreCase = true),
                            onClick = {
                                employmentType = if (employmentType.equals(type, ignoreCase = true)) "" else type
                            },
                            label = { Text(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sort By
                Text("Sort Listings By", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = sortBy == "date",
                        onClick = { sortBy = "date" },
                        label = { Text("Posting Date") }
                    )
                    FilterChip(
                        selected = sortBy == "relevance",
                        onClick = { sortBy = "relevance" },
                        label = { Text("Relevance") }
                    )
                }
            }

            // Apply Button
            Button(
                onClick = {
                    jobViewModel.updateFilters(
                        JobFilterState(
                            location = location.trim(),
                            experienceLevel = experienceLevel,
                            employmentType = employmentType,
                            isRemoteOnly = isRemoteOnly,
                            sortBy = sortBy
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Apply Filters", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
