package com.careerconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Server URL Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Backend Server Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connects to the 24/7 Cloud Backend (HTTPS). You can point to your own cloud instance (Render, Railway, Koyeb) or leave as default. No laptop or local computer required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it; urlSavedMessage = false },
                        label = { Text("Base API URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            RetrofitClient.setBaseUrl(serverUrl.trim())
                            urlSavedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Save Server URL")
                    }
                    if (urlSavedMessage) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Server URL updated successfully.", color = MatchHigh, fontSize = 12.sp)
                    }
                }
            }

            // Notification Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Notification Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Job Match Alerts",
                        subtitle = "Notify when high compatibility jobs are found",
                        checked = prefs.jobMatchesEnabled,
                        onCheckedChange = {
                            notificationViewModel.updatePreferences(prefs.copy(jobMatchesEnabled = it))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                    SettingToggleRow(
                        title = "Recommendation Updates",
                        subtitle = "Alert on weekly top career recommendations",
                        checked = prefs.recommendationsEnabled,
                        onCheckedChange = {
                            notificationViewModel.updatePreferences(prefs.copy(recommendationsEnabled = it))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                    SettingToggleRow(
                        title = "Application Tracking Alerts",
                        subtitle = "Reminders on application status updates",
                        checked = prefs.applicationUpdatesEnabled,
                        onCheckedChange = {
                            notificationViewModel.updatePreferences(prefs.copy(applicationUpdatesEnabled = it))
                        }
                    )
                }
            }

            // UN SDG 8 Information Card
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
                            text = "CareerConnect promotes sustained, inclusive and sustainable economic growth, full and productive employment and decent work for all through fair, skill-based compatibility matching.",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            // Sign Out Button
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MatchLow)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
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
