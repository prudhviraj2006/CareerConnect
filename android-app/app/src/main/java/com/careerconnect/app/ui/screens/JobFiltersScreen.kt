package com.careerconnect.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.utils.IndiaLocations
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

    var selectedCity by remember(currentFilter) { mutableStateOf(currentFilter.city) }
    var selectedState by remember(currentFilter) { mutableStateOf(currentFilter.state) }
    var isRemoteOnly by remember(currentFilter) { mutableStateOf(currentFilter.isRemoteOnly) }
    var isFresherOnly by remember(currentFilter) { mutableStateOf(currentFilter.isFresherOnly) }
    var isInternshipOnly by remember(currentFilter) { mutableStateOf(currentFilter.isInternshipOnly) }
    var includeInternational by remember(currentFilter) { mutableStateOf(currentFilter.includeInternational) }
    var employmentType by remember(currentFilter) { mutableStateOf(currentFilter.employmentType) }
    var sortBy by remember(currentFilter) { mutableStateOf(currentFilter.sortBy) }

    var stateMenuExpanded by remember { mutableStateOf(false) }

    val popularCities = listOf(
        "All India",
        "Bengaluru",
        "Hyderabad",
        "Pune",
        "Mumbai",
        "Delhi NCR",
        "Noida",
        "Gurugram",
        "Chennai",
        "Kochi",
        "Coimbatore",
        "Visakhapatnam",
        "Tirupati"
    )

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Job Filters (India-First)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(onClick = {
                        jobViewModel.resetFilters()
                        onNavigateBack()
                    }) {
                        Text("Reset", color = TextSecondaryLight, fontWeight = FontWeight.Bold)
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
                // 1. Indian Cities (Popular Hubs)
                Text("Select Indian City", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularCities.forEach { city ->
                        val isSelected = if (city == "All India") selectedCity.isBlank() else selectedCity == city
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCity = if (city == "All India") "" else city
                            },
                            label = { Text(city) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. State / Union Territory Dropdown
                Text("Indian State or Union Territory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedState.isBlank()) "All States & UTs" else selectedState,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { stateMenuExpanded = !stateMenuExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select State")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { stateMenuExpanded = true },
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(
                        expanded = stateMenuExpanded,
                        onDismissRequest = { stateMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All States & UTs") },
                            onClick = {
                                selectedState = ""
                                stateMenuExpanded = false
                            }
                        )
                        IndiaLocations.statesAndUTs.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    selectedState = s
                                    stateMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Fresher & Internship Specific Filters
                Text("Career Stage & Eligibility", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isFresherOnly,
                        onClick = { isFresherOnly = !isFresherOnly },
                        label = { Text("Fresher Friendly") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isInternshipOnly,
                        onClick = { isInternshipOnly = !isInternshipOnly },
                        label = { Text("Internships") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Remote in India / Work Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Remote Opportunities", fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        Text("Show remote opportunities accessible across India", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = isRemoteOnly,
                        onCheckedChange = { isRemoteOnly = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Optional International Jobs Toggle (Default: OFF)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Include International Jobs", fontWeight = FontWeight.SemiBold, color = NavyPrimary, fontSize = 14.sp)
                            Text(
                                text = "Default is India only. Turn on to view opportunities outside India with native currencies.",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                        Switch(
                            checked = includeInternational,
                            onCheckedChange = { includeInternational = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Sort By
                Text("Sort Listings By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = sortBy == "date",
                        onClick = { sortBy = "date" },
                        label = { Text("Latest Postings") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = sortBy == "relevance",
                        onClick = { sortBy = "relevance" },
                        label = { Text("Skill Compatibility") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Apply Filters Button (Visible White Text)
            Button(
                onClick = {
                    val locationQuery = when {
                        selectedCity.isNotBlank() -> selectedCity
                        selectedState.isNotBlank() -> selectedState
                        else -> ""
                    }
                    val empType = if (isInternshipOnly) "Internship" else employmentType

                    jobViewModel.updateFilters(
                        JobFilterState(
                            location = locationQuery,
                            state = selectedState,
                            city = selectedCity,
                            experienceLevel = if (isFresherOnly) "Fresher" else "",
                            employmentType = empType,
                            isRemoteOnly = isRemoteOnly,
                            isFresherOnly = isFresherOnly,
                            isInternshipOnly = isInternshipOnly,
                            includeInternational = includeInternational,
                            sortBy = sortBy
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Apply Filters",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
