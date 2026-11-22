package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SkillGapScreen(
    jobId: Int,
    matchViewModel: MatchViewModel,
    onNavigateBack: () -> Unit
) {
    val skillGap by matchViewModel.skillGap.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()

    LaunchedEffect(jobId) {
        matchViewModel.loadSkillGap(jobId)
    }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Skill Gap Analysis",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading || skillGap == null) {
            LoadingView(message = "Analyzing skill gaps against requirements...")
        } else {
            val sg = skillGap!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = sg.jobTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sg.companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Boost Potential: +${sg.compatibilityBoostPotential}% match",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Improvement Advice Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Targeted Skill Recommendation",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sg.improvementAdvice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Skills You Possess
                Text(
                    text = "Skills You Have Matched (${sg.matchingSkills.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (sg.matchingSkills.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sg.matchingSkills.forEach { skill ->
                            SkillChip(name = skill, isMatched = true)
                        }
                    }
                } else {
                    Text("No matching skills recorded yet.", color = TextSecondaryLight, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Missing Skills
                Text(
                    text = "Skills Missing From Your Profile (${sg.missingSkills.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (sg.missingSkills.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sg.missingSkills.forEach { skill ->
                            SkillChip(name = skill, isMatched = false)
                        }
                    }
                } else {
                    Text("No missing skills! You possess all required technical skills.", color = MatchHigh, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Academic Integrity Note
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NavyLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Academic Integrity Note: A missing skill indication does not imply you lack the skill in real life; it signifies that the skill is not yet confirmed in your CareerConnect profile. You can add it anytime via Edit Profile.",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
