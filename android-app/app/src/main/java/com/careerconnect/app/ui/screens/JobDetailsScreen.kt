package com.careerconnect.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.utils.IndiaLocations
import com.careerconnect.app.ui.components.*
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.JobViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobDetailsScreen(
    jobId: Int,
    jobViewModel: JobViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMatchScore: (Int) -> Unit,
    onNavigateToSkillGap: (Int) -> Unit
) {
    val context = LocalContext.current
    val job by jobViewModel.selectedJob.collectAsState()
    val savedIds by jobViewModel.savedJobIds.collectAsState()
    val isSaved = savedIds.contains(jobId)

    var showApplyTrackingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jobId) {
        jobViewModel.loadJobDetails(jobId)
    }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Job Details",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { jobViewModel.toggleSaveJob(jobId) }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) AccentBlue else NavyPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToSkillGap(jobId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skill Gap", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            job?.originalUrl?.let { url ->
                                if (url.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    showApplyTrackingDialog = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        if (job == null) {
            LoadingView(message = "Loading job details...")
        } else {
            val j = job!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Company & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = j.companyName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = j.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondaryLight
                        )
                        Text(
                            text = "Verified Provider: ${j.sourceName ?: "Legitimate Source"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = j.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    j.location?.let { MetadataChip(icon = Icons.Outlined.LocationOn, text = it) }
                    j.employmentType?.let { MetadataChip(icon = Icons.Outlined.WorkOutline, text = it) }
                    j.experienceLevel?.let { MetadataChip(icon = Icons.Outlined.Person, text = it) }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Compensation: ${IndiaLocations.formatIndianSalary(j.salary)}",
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Match Compatibility Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMatchScore(jobId) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intelligent Match Score",
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Based on 60% skills, 20% experience, 10% education, 10% preferences",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryLight
                            )
                        }
                        MatchScoreBadge(score = j.matchScore)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Required Skills
                if (j.requiredSkills.isNotEmpty()) {
                    Text(
                        text = "Required Skills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        j.requiredSkills.forEach { skill ->
                            SkillChip(name = skill)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Description
                Text(
                    text = "Job Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = j.description ?: "No description provided by source.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryLight,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Source transparency notice
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Genuine Listing Notice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "This listing was retrieved directly from authorized provider ${j.sourceName}. Tapping 'Apply Now' redirects you to the genuine employer application page.",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Post-Apply Tracking Confirmation Dialog
    if (showApplyTrackingDialog) {
        AlertDialog(
            onDismissRequest = { showApplyTrackingDialog = false },
            title = { Text("Track Application") },
            text = { Text("Did you apply to this position? Adding it to your tracker allows you to update interview and offer statuses.") },
            confirmButton = {
                Button(onClick = {
                    jobViewModel.trackApplication(jobId, "Applied", "Applied via official employer portal.")
                    showApplyTrackingDialog = false
                }) {
                    Text("Add to Tracker")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyTrackingDialog = false }) {
                    Text("Not Yet")
                }
            }
        )
    }
}
