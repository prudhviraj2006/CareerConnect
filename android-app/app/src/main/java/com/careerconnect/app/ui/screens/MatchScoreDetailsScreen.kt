package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.components.LoadingView
import com.careerconnect.app.ui.components.SkillChip
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.MatchViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchScoreDetailsScreen(
    jobId: Int,
    matchViewModel: MatchViewModel,
    onNavigateBack: () -> Unit
) {
    val matchDetails by matchViewModel.matchDetails.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()

    LaunchedEffect(jobId) {
        matchViewModel.loadMatchScore(jobId)
    }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Match Score Analysis",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading || matchDetails == null) {
            LoadingView(message = "Calculating transparent match score...")
        } else {
            val m = matchDetails!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Circular Score Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val scoreColor = when {
                            m.overallMatchScore >= 75f -> MatchHigh
                            m.overallMatchScore >= 50f -> MatchModerate
                            else -> MatchLow
                        }

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(scoreColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${m.overallMatchScore.toInt()}%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                                Text(
                                    text = m.recommendationTier,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scoreColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Transparent Compatibility Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = m.explanationSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Breakdown Section
                Text(
                    text = "Score Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                BreakdownProgressRow(
                    label = "Skills Similarity (60% max)",
                    score = m.breakdown.skillsScore,
                    maxScore = m.breakdown.skillsWeight,
                    color = AccentBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                BreakdownProgressRow(
                    label = "Experience Compatibility (20% max)",
                    score = m.breakdown.experienceScore,
                    maxScore = m.breakdown.experienceWeight,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                BreakdownProgressRow(
                    label = "Education Compatibility (10% max)",
                    score = m.breakdown.educationScore,
                    maxScore = m.breakdown.educationWeight,
                    color = NavyLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                BreakdownProgressRow(
                    label = "Location & Preferences (10% max)",
                    score = m.breakdown.locationPreferenceScore,
                    maxScore = m.breakdown.locationPreferenceWeight,
                    color = MatchHigh
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Matching Skills
                Text(
                    text = "Matching Verified Skills (${m.matchingSkills.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (m.matchingSkills.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        m.matchingSkills.forEach { skill ->
                            SkillChip(name = skill, isMatched = true)
                        }
                    }
                } else {
                    Text("No matching skills found in user profile.", color = TextSecondaryLight, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Missing Skills
                Text(
                    text = "Missing Required Skills (${m.missingSkills.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (m.missingSkills.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        m.missingSkills.forEach { skill ->
                            SkillChip(name = skill, isMatched = false)
                        }
                    }
                } else {
                    Text("You have all required skills listed for this job!", color = MatchHigh, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun BreakdownProgressRow(
    label: String,
    score: Float,
    maxScore: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
                Text(text = "$score / ${maxScore.toInt()} pts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (score / maxScore).coerceIn(0f, 1f) },
                color = color,
                trackColor = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}
