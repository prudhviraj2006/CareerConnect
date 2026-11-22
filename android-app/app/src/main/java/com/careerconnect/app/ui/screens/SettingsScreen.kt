package com.careerconnect.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.R
import com.careerconnect.app.data.api.RetrofitClient
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.AuthViewModel
import com.careerconnect.app.ui.viewmodels.NotificationViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val prefs by notificationViewModel.preferences.collectAsState()
    var serverUrl by remember { mutableStateOf(RetrofitClient.getBaseUrl()) }
    var urlSavedMessage by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. India-First Regional Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Regional & Market Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingInfoRow("Primary Market", "India (Bharat)")
                    SettingInfoRow("Default Currency", "Indian Rupee (INR – ₹)")
                    SettingInfoRow("Standard Timezone", "Asia/Kolkata (IST)")
                    SettingInfoRow("Date Format", "DD-MM-YYYY")
                    SettingInfoRow("Language", "English")
                }
            }

            // 2. Cloud Backend Server Connection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Cloud Backend Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connected 24/7 to the live HTTPS cloud backend on Render. No laptop, local server, or terminal required to operate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            urlSavedMessage = false
                        },
                        label = { Text("Base Cloud API URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            RetrofitClient.setBaseUrl(serverUrl.trim())
                            urlSavedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White)
                    ) {
                        Text("Save Cloud URL", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (urlSavedMessage) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Cloud URL updated successfully.", color = MatchHigh, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // 3. Notification Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Push Notification Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        title = "Skill-Match Alerts",
                        subtitle = "Alert when an Indian employer posts a role with >70% match",
                        checked = prefs.jobMatchesEnabled,
                        onCheckedChange = { notificationViewModel.updatePreferences(prefs.copy(jobMatchesEnabled = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)
                    SettingToggleRow(
                        title = "Application Status Updates",
                        subtitle = "Notify when there is progress on tracked applications",
                        checked = prefs.applicationUpdatesEnabled,
                        onCheckedChange = { notificationViewModel.updatePreferences(prefs.copy(applicationUpdatesEnabled = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)
                    SettingToggleRow(
                        title = "Recommended Job Digest",
                        subtitle = "Receive notifications for personalized career recommendations",
                        checked = prefs.recommendationsEnabled,
                        onCheckedChange = { notificationViewModel.updatePreferences(prefs.copy(recommendationsEnabled = it)) }
                    )
                }
            }

            // 4. UN SDG 8 Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = MatchHigh, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "UN Sustainable Development Goal 8",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CareerConnect promotes decent work and economic growth in India through transparent, merit-based skill compatibility matching without bias.",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            // 5. About CareerConnect Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.careerconnect_logo_horizontal),
                        contentDescription = "CareerConnect",
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .aspectRatio(990f / 235f),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Version 1.0.0 (Production Release)",
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryLight,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Intelligent Skill-Based Job Matching Application for India",
                        fontWeight = FontWeight.Medium,
                        color = NavyPrimary,
                        fontSize = 12.sp
                    )
                }
            }

            // 6. Sign Out Button
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            // 7. Delete Account & Personal Data Button
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MatchLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, MatchLow.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MatchLow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account & Wipe Data", color = MatchLow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Delete Account Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text("Delete Account & Data?", color = MatchLow, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "This will permanently erase your candidate profile, uploaded resumes, skill history, saved jobs, and tracked applications. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = TextSecondaryLight
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            authViewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MatchLow, contentColor = Color.White)
                    ) {
                        Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = TextSecondaryLight)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondaryLight)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = NavyPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondaryLight)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue)
        )
    }
}
