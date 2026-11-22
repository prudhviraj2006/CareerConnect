package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.models.JobDto
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.components.JobCard
import com.careerconnect.app.ui.components.MatchScoreBadge
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.JobViewModel
import com.careerconnect.app.ui.viewmodels.MatchViewModel
import com.careerconnect.app.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    jobViewModel: JobViewModel,
    matchViewModel: MatchViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToJobDetails: (Int) -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToApplications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToResumeUpload: () -> Unit
) {
    val jobs by jobViewModel.jobs.collectAsState()
    val recommendations by matchViewModel.recommendations.collectAsState()
    val profile by profileViewModel.currentProfile.collectAsState()
    val savedIds by jobViewModel.savedJobIds.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        jobViewModel.loadJobs()
        matchViewModel.loadRecommendations()
        profileViewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CareerConnect",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "UN SDG 8 • Decent Work",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryLight
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { jobViewModel.syncJobs() }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Jobs",
                            tint = NavyPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Welcome Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Hello, ${profile?.fullName ?: "Candidate"}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (profile?.isConfirmedByUser == true)
                                "Your verified skills: ${profile?.technicalSkills?.size ?: 0} technical skills active"
                            else
                                "Upload your resume or set up profile to activate real skill-matching",
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateToResumeUpload,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Resume", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToProfile,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Edit Profile", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Saved",
                        value = "${savedIds.size}",
                        icon = Icons.Default.Bookmark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSaved
                    )
                    StatCard(
                        title = "Recommendations",
                        value = "${recommendations.size}",
                        icon = Icons.Default.ThumbUp,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRecommendations
                    )
                    StatCard(
                        title = "Applications",
                        value = "Track",
                        icon = Icons.Default.Assignment,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToApplications
                    )
                }
            }

            // Top Recommendations Section
            if (recommendations.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended For You",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        TextButton(onClick = onNavigateToRecommendations) {
                            Text("See All", color = AccentBlue)
                        }
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recommendations.take(5)) { rec ->
                            RecommendedJobItem(
                                job = rec.job,
                                matchScore = rec.matchScore,
                                reason = rec.recommendationReason,
                                onClick = { onNavigateToJobDetails(rec.job.id) }
                            )
                        }
                    }
                }
            }

            // Recent Genuine Job Listings Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Latest Verified Job Listings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Genuine opportunities from authorized providers",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            if (jobs.isEmpty() && !isLoading) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.WorkOutline,
                        title = "No jobs loaded yet",
                        message = "Tap Sync to fetch real job listings from Remotive and Arbeitnow public APIs.",
                        actionButtonText = "Sync Genuine Jobs",
                        onActionClick = { jobViewModel.syncJobs() }
                    )
                }
            } else {
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

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyPrimary)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
        }
    }
}

@Composable
fun RecommendedJobItem(
    job: JobDto,
    matchScore: Float,
    reason: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.companyName,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                MatchScoreBadge(score = matchScore)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = job.title,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
