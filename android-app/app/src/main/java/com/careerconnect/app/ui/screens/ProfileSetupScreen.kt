package com.careerconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.models.UserProfileUpdateDto
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    profileViewModel: ProfileViewModel,
    onNavigateToResumeUpload: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var educationLevel by remember { mutableStateOf("Bachelor's Degree") }
    var university by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var gradYearText by remember { mutableStateOf("2024") }
    var expYearsText by remember { mutableStateOf("1.0") }
    var workExp by remember { mutableStateOf("") }
    var internshipExp by remember { mutableStateOf("") }
    var preferredRole by remember { mutableStateOf("Software Engineer") }
    var preferredLocation by remember { mutableStateOf("Remote") }
    var employmentType by remember { mutableStateOf("Full-time") }

    var skillInput by remember { mutableStateOf("") }
    val technicalSkills = remember { mutableStateListOf("Kotlin", "Android", "Jetpack Compose", "Git") }
    val softSkills = remember { mutableStateListOf("Teamwork", "Communication", "Problem Solving") }

    Scaffold(
        topBar = {
            CareerConnectTopBar(title = "Profile Setup")
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Complete Your Career Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Accurate profile data enables genuine skill-based job matching and gap analysis.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Education Section
            Text(text = "Education", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = university,
                onValueChange = { university = it },
                label = { Text("College / University") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree (e.g. B.Tech)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = gradYearText,
                    onValueChange = { gradYearText = it },
                    label = { Text("Graduation Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Specialization (e.g. Computer Science)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Technical Skills Section
            Text(text = "Technical Skills", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = skillInput,
                    onValueChange = { skillInput = it },
                    label = { Text("Add Technical Skill") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (skillInput.isNotBlank() && !technicalSkills.contains(skillInput.trim())) {
                            technicalSkills.add(skillInput.trim())
                            skillInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Skill", tint = AccentBlue)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                technicalSkills.forEach { skill ->
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(skill) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondaryLight
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Work Experience Section
            Text(text = "Experience & Preferences", style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = expYearsText,
                onValueChange = { expYearsText = it },
                label = { Text("Years of Experience") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredRole,
                onValueChange = { preferredRole = it },
                label = { Text("Preferred Job Role") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredLocation,
                onValueChange = { preferredLocation = it },
                label = { Text("Preferred Location (e.g. Remote, City)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val dto = UserProfileUpdateDto(
                        educationLevel = educationLevel,
                        collegeUniversity = university,
                        degree = degree,
                        specialization = specialization,
                        graduationYear = gradYearText.toIntOrNull(),
                        technicalSkills = technicalSkills.toList(),
                        softSkills = softSkills.toList(),
                        workExperience = workExp,
                        experienceYears = expYearsText.toFloatOrNull() ?: 0f,
                        internshipExperience = internshipExp,
                        preferredJobRole = preferredRole,
                        preferredLocation = preferredLocation,
                        employmentTypePreference = employmentType,
                        isConfirmedByUser = true
                    )
                    profileViewModel.updateProfile(dto) {
                        onNavigateToHome()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Profile & Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToResumeUpload,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Upload Resume to Auto-Fill", color = AccentBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
