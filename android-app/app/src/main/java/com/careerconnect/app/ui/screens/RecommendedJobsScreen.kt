package com.careerconnect.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.components.EmptyStateView
import com.careerconnect.app.ui.components.LoadingView
import com.careerconnect.app.ui.components.MatchScoreBadge
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.MatchViewModel

@Composable
fun RecommendedJobsScreen(
    matchViewModel: MatchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToJobDetails: (Int) -> Unit
) {
    val recommendations by matchViewModel.recommendations.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        matchViewModel.loadRecommendations()
    }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Recommended For You",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading && recommendations.isEmpty()) {
            LoadingView(message = "Ranking personalized recommendations...")
        } else if (recommendations.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ThumbUp,
                title = "No Recommendations Yet",
                message = "Make sure your profile has verified skills, or complete your profile to enable personalized ranking."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(recommendations) { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToJobDetails(rec.job.id) },
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "#${rec.rankOrder} Best Match",
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan,
                                        fontSize = 12.sp
                                    )
                                }
                                MatchScoreBadge(score = rec.matchScore)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = rec.job.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )

                            Text(
                                text = "${rec.job.companyName} • ${rec.job.location ?: "Remote"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = rec.recommendationReason,
                                    fontSize = 12.sp,
                                    color = TextSecondaryLight,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
