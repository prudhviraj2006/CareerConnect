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
    var preferredLocation by remember(currentProfile) { mutableStateOf(currentProfile?.preferredLocation ?: "") }
    var employmentType by remember(currentProfile) { mutableStateOf(currentProfile?.employmentTypePreference ?: "Full-time") }

    val technicalSkills = remember(currentProfile) {
        mutableStateListOf<String>().apply {
            addAll(currentProfile?.technicalSkills ?: emptyList())
        }
    }
    var newSkill by remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Education Details", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

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
                    label = { Text("Degree") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = gradYearText,
                    onValueChange = { gradYearText = it },
                    label = { Text("Grad Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Technical Skills", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    label = { Text("Add Skill") },
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
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Preferences", fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

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
                label = { Text("Preferred Role") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredLocation,
                onValueChange = { preferredLocation = it },
                label = { Text("Preferred Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val updateDto = UserProfileUpdateDto(
                        fullName = fullName.trim(),
                        phoneNumber = phone.trim(),
                        collegeUniversity = university.trim(),
                        degree = degree.trim(),
                        specialization = specialization.trim(),
                        graduationYear = gradYearText.toIntOrNull(),
                        technicalSkills = technicalSkills.toList(),
                        experienceYears = expYearsText.toFloatOrNull() ?: 0f,
                        preferredJobRole = preferredRole.trim(),
                        preferredLocation = preferredLocation.trim(),
                        employmentTypePreference = employmentType,
                        isConfirmedByUser = true
                    )
                    profileViewModel.updateProfile(updateDto) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
