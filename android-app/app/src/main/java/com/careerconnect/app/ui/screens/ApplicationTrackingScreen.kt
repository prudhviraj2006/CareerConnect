package com.careerconnect.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.models.ApplicationDto
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.JobViewModel

@Composable
fun ApplicationTrackingScreen(
    jobViewModel: JobViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToJobDetails: (Int) -> Unit
) {
    val applications by jobViewModel.applications.collectAsState()
    var selectedAppToEdit by remember { mutableStateOf<ApplicationDto?>(null) }

    LaunchedEffect(Unit) {
        jobViewModel.loadApplications()
    }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Application Tracker",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Notice Card
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = NavyLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Candidate-managed tracking: Update your interview and application milestones manually.",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            if (applications.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Assignment,
                    title = "No Applications Tracked",
                    message = "When you apply to jobs, track your progress from 'Interested' through 'Interview' and 'Offer' stages."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(applications) { app ->
                        ApplicationCard(
                            app = app,
                            onClick = { onNavigateToJobDetails(app.jobId) },
                            onEditStatus = { selectedAppToEdit = app }
                        )
                    }
                }
            }
        }
    }

    // Status Edit Dialog
    selectedAppToEdit?.let { app ->
        var newStatus by remember { mutableStateOf(app.status) }
        var notes by remember { mutableStateOf(app.notes ?: "") }
        val statuses = listOf("Interested", "Applied", "Interview", "Rejected", "Offer")

        AlertDialog(
            onDismissRequest = { selectedAppToEdit = null },
            title = { Text("Update Application Status") },
            text = {
                Column {
                    Text(
                        text = "${app.job.title} at ${app.job.companyName}",
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Status:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    statuses.forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newStatus = st }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (newStatus == st),
                                onClick = { newStatus = st }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (e.g. interview date)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    jobViewModel.updateApplicationStatus(app.id, newStatus, notes)
                    selectedAppToEdit = null
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ApplicationCard(
    app: ApplicationDto,
    onClick: () -> Unit,
    onEditStatus: () -> Unit
) {
    val (statusBg, statusColor) = when (app.status) {
        "Offer" -> Color(0xFFD1FAE5) to MatchHigh
        "Interview" -> Color(0xFFEFF6FF) to AccentBlue
        "Applied" -> Color(0xFFFEF3C7) to MatchModerate
        "Rejected" -> Color(0xFFFEE2E2) to MatchLow
        else -> Color(0xFFF1F5F9) to MatchInfo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.job.companyName,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryLight
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = app.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.job.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            if (!app.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${app.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditStatus) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Status", color = AccentBlue, fontSize = 12.sp)
                }
            }
        }
    }
}
