package com.careerconnect.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.R
import com.careerconnect.app.ui.theme.AccentBlue
import com.careerconnect.app.ui.theme.NavyPrimary
import com.careerconnect.app.ui.theme.TextSecondaryLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    isOnboardingCompleted: Boolean,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        isVisible = true
        delay(1800)
        when {
            isLoggedIn -> onNavigateToHome()
            !isOnboardingCompleted -> onNavigateToOnboarding()
            else -> onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 800))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Official CareerConnect Horizontal Brand Logo (Wordmark + "Find • Match • Grow")
                    Image(
                        painter = painterResource(id = R.drawable.careerconnect_logo_horizontal),
                        contentDescription = "CareerConnect Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(990f / 235f),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Intelligent Skill-Based Job Matching",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = AccentBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // UN SDG 8 Badge
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text(
                    text = "Promoting Decent Work & Economic Growth (UN SDG 8)",
                    color = NavyPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
