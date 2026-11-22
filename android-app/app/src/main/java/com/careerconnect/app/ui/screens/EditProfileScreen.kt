package com.careerconnect.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.careerconnect.app.data.utils.IndiaLocations
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val currentProfile by profileViewModel.currentProfile.collectAsState()

    var fullName by remember(currentProfile) { mutableStateOf(currentProfile?.fullName ?: "") }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile?.phoneNumber ?: "") }
    var university by remember(currentProfile) { mutableStateOf(currentProfile?.collegeUniversity ?: "") }
    var degree by remember(currentProfile) { mutableStateOf(currentProfile?.degree ?: "") }
    var specialization by remember(currentProfile) { mutableStateOf(currentProfile?.specialization ?: "") }
    var gradYearText by remember(currentProfile) { mutableStateOf(currentProfile?.graduationYear?.toString() ?: "") }
    var expYearsText by remember(currentProfile) { mutableStateOf(currentProfile?.experienceYears?.toString() ?: "0.0") }
    var preferredRole by remember(currentProfile) { mutableStateOf(currentProfile?.preferredJobRole ?: "") }
    var preferredLocation by remember(currentProfile) { mutableStateOf(currentProfile?.preferredLocation ?: "Bengaluru, India") }
    var expectedSalary by remember(currentProfile) { mutableStateOf(currentProfile?.expectedSalary ?: "") }
    var employmentType by remember(currentProfile) { mutableStateOf(currentProfile?.employmentTypePreference ?: "Full-time") }

    val technicalSkills = remember(currentProfile) {
        mutableStateListOf<String>().apply {
            addAll(currentProfile?.technicalSkills ?: emptyList())
        }
    }
    var newSkill by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Edit Profile",
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (validationError != null) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError!!,
                        color = Color(0xFF991B1B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text("Personal & Contact", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name", color = NavyPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Indian Mobile Number", color = NavyPrimary) },
                placeholder = { Text("+91 9876543210") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Education Details", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = university,
                onValueChange = { university = it },
                label = { Text("College / University", color = NavyPrimary) },
                placeholder = { Text("e.g. IIT Delhi, Anna University") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree", color = NavyPrimary) },
                    placeholder = { Text("B.Tech") },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = gradYearText,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            gradYearText = it
                        }
                    },
                    label = { Text("Grad Year", color = NavyPrimary) },
                    placeholder = { Text("2025") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Specialization", color = NavyPrimary) },
                placeholder = { Text("Computer Science, ECE, Mechanical") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Confirmed Technical Skills", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    label = { Text("Add Skill", color = NavyPrimary) },
                    placeholder = { Text("e.g. Kotlin, Python, SQL") },
                    singleLine = true,
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
                technicalSkills.forEach { s ->
                    InputChip(
                        selected = true,
                        onClick = { technicalSkills.remove(s) },
                        label = { Text(s) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove $s",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { technicalSkills.remove(s) }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Experience & Target Roles", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = expYearsText,
                onValueChange = { expYearsText = it },
                label = { Text("Experience Years (0 for Fresher)", color = NavyPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredRole,
                onValueChange = { preferredRole = it },
                label = { Text("Preferred Job Role", color = NavyPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredLocation,
                onValueChange = { preferredLocation = it },
                label = { Text("Preferred Location in India", color = NavyPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = expectedSalary,
                onValueChange = { expectedSalary = it },
                label = { Text("Expected Salary in INR (e.g. ₹6 LPA)", color = NavyPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Changes Button (Guaranteed Visible Text)
            Button(
                onClick = {
                    val yearInt = gradYearText.toIntOrNull()
                    when {
                        fullName.isBlank() -> validationError = "Please enter your full name."
                        phone.isNotBlank() && !IndiaLocations.isValidIndianPhone(phone) -> {
                            validationError = "Please enter a valid 10-digit Indian phone number (+91)."
                        }
                        yearInt != null && (yearInt < 1970 || yearInt > 2035) -> {
                            validationError = "Please enter a valid graduation year between 1970 and 2035."
                        }
                        else -> {
                            validationError = null
                            isSaving = true
                            val dto = UserProfileUpdateDto(
                                fullName = fullName.trim(),
                                phoneNumber = if (phone.isNotBlank()) IndiaLocations.formatIndianPhone(phone) else null,
                                collegeUniversity = university.trim().takeIf { it.isNotBlank() },
                                degree = degree.trim().takeIf { it.isNotBlank() },
                                specialization = specialization.trim().takeIf { it.isNotBlank() },
                                graduationYear = yearInt,
                                technicalSkills = technicalSkills.toList(),
                                experienceYears = expYearsText.toFloatOrNull() ?: 0f,
                                preferredJobRole = preferredRole.trim().takeIf { it.isNotBlank() },
                                preferredLocation = preferredLocation.trim().takeIf { it.isNotBlank() },
                                expectedSalary = expectedSalary.trim().takeIf { it.isNotBlank() },
                                employmentTypePreference = employmentType,
                                isConfirmedByUser = true
                            )
                            profileViewModel.updateProfile(dto) {
                                isSaving = false
                                onNavigateBack()
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = NavyPrimary.copy(alpha = 0.6f),
                    disabledContentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Save Changes",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
