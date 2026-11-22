package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.ui.components.SkillChip
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    profileViewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToResumeUpload: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val profile by profileViewModel.currentProfile.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Profile", color = NavyPrimary) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NavyPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.fullName?.take(1)?.uppercase() ?: "U",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profile?.fullName ?: "Candidate Name",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Text(
                        text = profile?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )

                    profile?.phoneNumber?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = if (profile?.isConfirmedByUser == true) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (profile?.isConfirmedByUser == true) "Verified Candidate Profile" else "Profile Unverified",
                            color = if (profile?.isConfirmedByUser == true) MatchHigh else MatchModerate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onNavigateToEditProfile,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile")
                        }

                        OutlinedButton(
                            onClick = onNavigateToResumeUpload,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Education Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Education", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile?.collegeUniversity ?: "University not specified",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${profile?.degree ?: "Degree not set"} ${if (profile?.graduationYear != null) "• Class of ${profile?.graduationYear}" else ""}",
                        color = TextSecondaryLight,
                        fontSize = 13.sp
                    )
                    profile?.specialization?.let {
                        Text(text = "Specialization: $it", color = TextSecondaryLight, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verified Technical Skills
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Verified Technical Skills (${profile?.technicalSkills?.size ?: 0})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val skills = profile?.technicalSkills ?: emptyList()
                    if (skills.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            skills.forEach { skill ->
                                SkillChip(name = skill)
                            }
                        }
                    } else {
                        Text("No technical skills verified yet. Tap Edit Profile or Upload Resume to add.", color = TextSecondaryLight, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Career Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Preferences & Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileDetailRow("Preferred Role", profile?.preferredJobRole ?: "Not specified")
                    ProfileDetailRow("Location", profile?.preferredLocation ?: "Not specified")
                    ProfileDetailRow("Employment Type", profile?.employmentTypePreference ?: "Full-time")
                    ProfileDetailRow("Experience", "${profile?.experienceYears ?: 0f} years")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondaryLight, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, color = NavyPrimary, fontSize = 13.sp)
    }
}
