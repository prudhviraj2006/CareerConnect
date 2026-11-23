package com.careerconnect.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.careerconnect.app.data.models.UserProfileUpdateDto
import com.careerconnect.app.data.utils.IndiaLocations
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
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currentCity by remember { mutableStateOf("Bengaluru") }
    var currentState by remember { mutableStateOf("Karnataka") }

    // Fresher / Experienced Toggle
    var isFresher by remember { mutableStateOf(true) }

    // Education
    var educationLevel by remember { mutableStateOf("Bachelor's Degree") }
    var university by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var gradYearText by remember { mutableStateOf("2025") }

    // Experience
    var expYearsText by remember { mutableStateOf("0.0") }
    var workExp by remember { mutableStateOf("") }
    var internshipExp by remember { mutableStateOf("") }

    // Projects & Certifications
    var projects by remember { mutableStateOf("") }
    var certifications by remember { mutableStateOf("") }

    // Preferences
    var preferredRole by remember { mutableStateOf("Software Engineer") }
    var preferredLocation by remember { mutableStateOf("Bengaluru, India") }
    var employmentType by remember { mutableStateOf("Full-time") }
    var expectedSalary by remember { mutableStateOf("₹6 LPA") }

    // Skills
    var techSkillInput by remember { mutableStateOf("") }
    val technicalSkills = remember { mutableStateListOf("Kotlin", "Android", "Jetpack Compose", "Git", "REST APIs") }

    var softSkillInput by remember { mutableStateOf("") }
    val softSkills = remember { mutableStateListOf("Problem Solving", "Team Collaboration", "Communication") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Dropdown state
    var stateMenuExpanded by remember { mutableStateOf(false) }

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
                .imePadding()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Complete Your Career Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Skill-based job matching in India requires accurate education, experience, and verified skills.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error display
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

            // Quick Auto-Fill via Resume
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FC)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Have a Resume?", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 14.sp)
                        Text("Upload PDF/DOCX to auto-extract your skills and education.", fontSize = 12.sp, color = TextSecondaryLight)
                    }
                    Button(
                        onClick = onNavigateToResumeUpload,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Personal & Contact
            Text("1. Personal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    validationError = null
                },
                label = { Text("Full Name", color = NavyPrimary) },
                placeholder = { Text("e.g. Rahul Sharma") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    validationError = null
                },
                label = { Text("Indian Mobile Number", color = NavyPrimary) },
                placeholder = { Text("+91 9876543210") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // State & City
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = currentState,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("State / UT", color = NavyPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { stateMenuExpanded = !stateMenuExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select State")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { stateMenuExpanded = true },
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(
                        expanded = stateMenuExpanded,
                        onDismissRequest = { stateMenuExpanded = false }
                    ) {
                        IndiaLocations.statesAndUTs.forEach { state ->
                            DropdownMenuItem(
                                text = { Text(state) },
                                onClick = {
                                    currentState = state
                                    stateMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = currentCity,
                    onValueChange = { currentCity = it },
                    label = { Text("City", color = NavyPrimary) },
                    placeholder = { Text("e.g. Bengaluru") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Employment Status
            Text("2. Career Stage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = isFresher,
                    onClick = {
                        isFresher = true
                        expYearsText = "0.0"
                    },
                    label = { Text("Fresher / Student") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !isFresher,
                    onClick = {
                        isFresher = false
                        if (expYearsText == "0.0") expYearsText = "1.5"
                    },
                    label = { Text("Experienced") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (!isFresher) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = expYearsText,
                    onValueChange = { expYearsText = it },
                    label = { Text("Total Years of Experience", color = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Education
            Text("3. Education Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = university,
                onValueChange = { university = it },
                label = { Text("College / University", color = NavyPrimary) },
                placeholder = { Text("e.g. IIT Madras, Anna University, VTU") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree (e.g. B.Tech / B.E.)", color = NavyPrimary) },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = gradYearText,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
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
                label = { Text("Specialization / Branch", color = NavyPrimary) },
                placeholder = { Text("e.g. Computer Science, Electronics") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Technical Skills
            Text("4. Technical Skills (${technicalSkills.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = techSkillInput,
                    onValueChange = { techSkillInput = it },
                    label = { Text("Add Technical Skill", color = NavyPrimary) },
                    placeholder = { Text("e.g. Python, SQL, Docker") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (techSkillInput.isNotBlank() && !technicalSkills.contains(techSkillInput.trim())) {
                            technicalSkills.add(techSkillInput.trim())
                            techSkillInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Technical Skill", tint = AccentBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                                contentDescription = "Remove $skill",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { technicalSkills.remove(skill) }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Soft Skills
            Text("5. Soft Skills (${softSkills.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = softSkillInput,
                    onValueChange = { softSkillInput = it },
                    label = { Text("Add Soft Skill", color = NavyPrimary) },
                    placeholder = { Text("e.g. Communication, Problem Solving") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (softSkillInput.isNotBlank() && !softSkills.contains(softSkillInput.trim())) {
                            softSkills.add(softSkillInput.trim())
                            softSkillInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Soft Skill", tint = AccentBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                softSkills.forEach { skill ->
                    InputChip(
                        selected = true,
                        onClick = { softSkills.remove(skill) },
                        label = { Text(skill) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove $skill",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { softSkills.remove(skill) }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Projects & Certifications
            Text("6. Projects & Certifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = projects,
                onValueChange = { projects = it },
                label = { Text("Key Academic / Personal Projects", color = NavyPrimary) },
                placeholder = { Text("e.g. CareerConnect Android app using Compose & MVVM") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = certifications,
                onValueChange = { certifications = it },
                label = { Text("Certifications", color = NavyPrimary) },
                placeholder = { Text("e.g. Google Associate Android Developer, AWS Certified") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Job Preferences
            Text("7. Job Preferences (India)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredRole,
                onValueChange = { preferredRole = it },
                label = { Text("Preferred Job Role", color = NavyPrimary) },
                placeholder = { Text("e.g. Android Developer, Data Analyst") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = preferredLocation,
                onValueChange = { preferredLocation = it },
                label = { Text("Preferred Location (e.g. Bengaluru, Hyderabad, Remote)", color = NavyPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = expectedSalary,
                onValueChange = { expectedSalary = it },
                label = { Text("Expected Salary in INR (e.g. ₹6 LPA or ₹30,000/mo)", color = NavyPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Profile Button (Guaranteed Visible Label)
            Button(
                onClick = {
                    val yearInt = gradYearText.toIntOrNull()
                    when {
                        fullName.isBlank() -> {
                            validationError = "Please enter your full name."
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        }
                        phone.isNotBlank() && !IndiaLocations.isValidIndianPhone(phone) -> {
                            validationError = "Please enter a valid 10-digit Indian phone number (+91)."
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        }
                        yearInt == null || yearInt < 1970 || yearInt > 2035 -> {
                            validationError = "Please enter a valid graduation year between 1970 and 2035."
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        }
                        technicalSkills.isEmpty() -> {
                            validationError = "Please add at least 1 technical skill for job matching."
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        }
                        else -> {
                            validationError = null
                            isSaving = true
                            val dto = UserProfileUpdateDto(
                                fullName = fullName.trim(),
                                phoneNumber = if (phone.isNotBlank()) IndiaLocations.formatIndianPhone(phone) else null,
                                educationLevel = educationLevel,
                                collegeUniversity = university.trim().takeIf { it.isNotBlank() },
                                degree = degree.trim().takeIf { it.isNotBlank() },
                                specialization = specialization.trim().takeIf { it.isNotBlank() },
                                graduationYear = yearInt,
                                technicalSkills = technicalSkills.toList(),
                                softSkills = softSkills.toList(),
                                workExperience = workExp.takeIf { it.isNotBlank() },
                                experienceYears = if (isFresher) 0f else (expYearsText.toFloatOrNull() ?: 0f),
                                internshipExperience = internshipExp.takeIf { it.isNotBlank() },
                                projects = projects.trim().takeIf { it.isNotBlank() },
                                certifications = certifications.trim().takeIf { it.isNotBlank() },
                                preferredJobRole = preferredRole.trim().takeIf { it.isNotBlank() },
                                preferredLocation = "$currentCity, $currentState",
                                expectedSalary = expectedSalary.trim().takeIf { it.isNotBlank() },
                                employmentTypePreference = employmentType,
                                isConfirmedByUser = true
                            )
                            profileViewModel.updateProfile(dto) {
                                isSaving = false
                                onNavigateToHome()
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
                        text = "Save Profile & Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
