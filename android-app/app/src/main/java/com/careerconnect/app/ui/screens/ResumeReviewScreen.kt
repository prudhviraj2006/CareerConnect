package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.models.UserProfileUpdateDto
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.ResumeUiState
import com.careerconnect.app.ui.viewmodels.ResumeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeReviewScreen(
    resumeViewModel: ResumeViewModel,
    onNavigateBack: () -> Unit,
    onConfirmComplete: () -> Unit
) {
    val resume by resumeViewModel.latestResume.collectAsState()
    val extracted = resume?.parsedData

    var name by remember(extracted) { mutableStateOf(extracted?.candidateName ?: "") }
    var email by remember(extracted) { mutableStateOf(extracted?.email ?: "") }
    var phone by remember(extracted) { mutableStateOf(extracted?.phone ?: "") }
    var university by remember(extracted) { mutableStateOf(extracted?.collegeUniversity ?: "") }
    var degree by remember(extracted) { mutableStateOf(extracted?.degree ?: "") }
    var gradYearText by remember(extracted) { mutableStateOf(extracted?.graduationYear?.toString() ?: "") }
    var expYearsText by remember(extracted) { mutableStateOf(extracted?.experienceYears?.toString() ?: "0.0") }
    var expSummary by remember(extracted) { mutableStateOf(extracted?.experienceSummary ?: "") }

    val technicalSkills = remember(extracted) {
        mutableStateListOf<String>().apply {
            addAll(extracted?.extractedSkills ?: emptyList())
        }
    }
    var newSkill by remember { mutableStateOf("") }

    val uiState by resumeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Review Extracted Data",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
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
            // Transparency Banner
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Extracted Automatically – Review & Correct",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This information was extracted via rule-based keyword matching. Please verify and edit every field before saving. Only your confirmed data will be saved.",
                            color = TextSecondaryLight,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Candidate Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Candidate Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // University
            OutlinedTextField(
                value = university,
                onValueChange = { university = it },
                label = { Text("College / University") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Degree & Grad Year
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = gradYearText,
                    onValueChange = { gradYearText = it },
                    label = { Text("Grad Year") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Technical Skills
            Text(
                text = "Identified Skills (${technicalSkills.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    label = { Text("Add / Correct Skill") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (newSkill.isNotBlank() && !technicalSkills.contains(newSkill.trim())) {
                        technicalSkills.add(newSkill.trim())
                        newSkill = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentBlue)
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
                        onClick = { technicalSkills.remove(skill) },
                        label = { Text(skill) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Experience Summary
            OutlinedTextField(
                value = expYearsText,
                onValueChange = { expYearsText = it },
                label = { Text("Estimated Years of Experience") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = expSummary,
                onValueChange = { expSummary = it },
                label = { Text("Experience Summary") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Confirm Button
            Button(
                onClick = {
                    val updateDto = UserProfileUpdateDto(
                        fullName = name.takeIf { it.isNotBlank() },
                        collegeUniversity = university.takeIf { it.isNotBlank() },
                        degree = degree.takeIf { it.isNotBlank() },
                        graduationYear = gradYearText.toIntOrNull(),
                        technicalSkills = technicalSkills.toList(),
                        experienceYears = expYearsText.toFloatOrNull() ?: 0f,
                        workExperience = expSummary,
                        isConfirmedByUser = true
                    )
                    resumeViewModel.confirmExtractedDetails(updateDto) {
                        onConfirmComplete()
                    }
                },
                enabled = uiState !is ResumeUiState.Uploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                if (uiState is ResumeUiState.Uploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm & Save Verified Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
