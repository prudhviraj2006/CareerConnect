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
import com.careerconnect.app.data.utils.IndiaLocations
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

    // Calculate genuine Profile Completion Percentage
    val completionPercentage = remember(profile) {
        var score = 0
        val p = profile
        if (p != null) {
            if (!p.fullName.isNullOrBlank()) score += 15
            if (!p.email.isNullOrBlank()) score += 15
            if (!p.phoneNumber.isNullOrBlank()) score += 10
            if (!p.collegeUniversity.isNullOrBlank()) score += 15
            if (!p.degree.isNullOrBlank()) score += 10
            if (p.graduationYear != null && p.graduationYear > 1970) score += 10
            if (p.technicalSkills.isNotEmpty()) score += 15
            if (!p.preferredJobRole.isNullOrBlank() || !p.preferredLocation.isNullOrBlank()) score += 10
        }
        score.coerceIn(0, 100)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Profile", color = NavyPrimary, fontWeight = FontWeight.Bold) },
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
            // Hero Profile Card
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
                            text = profile?.fullName?.take(1)?.uppercase() ?: "C",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile?.fullName?.takeIf { it.isNotBlank() } ?: "Not added yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = profile?.email?.takeIf { it.isNotBlank() } ?: "Email not added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )

                    if (!profile?.phoneNumber.isNullOrBlank()) {
                        Text(
                            text = profile!!.phoneNumber!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verified Badge: Only displayed when genuinely confirmed by user
                    val isGenuinelyVerified = profile?.isConfirmedByUser == true && !profile?.fullName.isNullOrBlank()
                    Surface(
                        color = if (isGenuinelyVerified) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isGenuinelyVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isGenuinelyVerified) MatchHigh else MatchModerate,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGenuinelyVerified) "Verified Profile" else "Profile Incomplete",
                                color = if (isGenuinelyVerified) MatchHigh else MatchModerate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (Explicit High Contrast)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToEditProfile,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToResumeUpload,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Completion Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile Completion",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "$completionPercentage%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AccentBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { completionPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    if (completionPercentage < 80) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tip: Complete education, graduation year, and skills to maximize match scores.",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        text = profile?.collegeUniversity?.takeIf { it.isNotBlank() } ?: "Not added yet",
                        fontWeight = FontWeight.SemiBold,
                        color = if (profile?.collegeUniversity.isNullOrBlank()) TextSecondaryLight else TextPrimaryLight,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${profile?.degree?.takeIf { it.isNotBlank() } ?: "Degree not set"} ${if (profile?.graduationYear != null) "• Class of ${profile?.graduationYear}" else ""}",
                        color = TextSecondaryLight,
                        fontSize = 13.sp
                    )
                    if (!profile?.specialization.isNullOrBlank()) {
                        Text(text = "Specialization: ${profile!!.specialization}", color = TextSecondaryLight, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verified Technical Skills Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Confirmed Technical Skills (${profile?.technicalSkills?.size ?: 0})",
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
                        Text(
                            text = "No technical skills confirmed yet. Tap Edit Profile or Upload Resume to add your skills.",
                            color = TextSecondaryLight,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Career Preferences & Experience (India)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Preferences & Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileDetailRow("Preferred Role", profile?.preferredJobRole?.takeIf { it.isNotBlank() } ?: "Not added yet")
                    ProfileDetailRow("Preferred Location", profile?.preferredLocation?.takeIf { it.isNotBlank() } ?: "India (Any)")
                    ProfileDetailRow("Employment Type", profile?.employmentTypePreference?.takeIf { it.isNotBlank() } ?: "Full-time")
                    ProfileDetailRow(
                        "Experience",
                        if ((profile?.experienceYears ?: 0f) == 0f) "Fresher" else "${profile?.experienceYears} years"
                    )
                    ProfileDetailRow(
                        "Expected Salary",
                        IndiaLocations.formatIndianSalary(profile?.expectedSalary)
                    )
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
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondaryLight, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, color = NavyPrimary, fontSize = 13.sp)
    }
}
