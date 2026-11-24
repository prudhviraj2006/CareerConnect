package com.careerconnect.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.data.models.JobDto
import com.careerconnect.app.data.utils.IndiaLocations
import com.careerconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerConnectTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = NavyPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = NavyPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun MatchScoreBadge(score: Float?, hasSufficientInfo: Boolean = true) {
    if (score == null || !hasSufficientInfo) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Text(
                text = "Score N/A",
                color = MatchInfo,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        return
    }

    val (badgeBg, badgeColor) = when {
        score >= 75f -> Color(0xFFD1FAE5) to MatchHigh
        score >= 50f -> Color(0xFFFEF3C7) to MatchModerate
        else -> Color(0xFFFEE2E2) to MatchLow
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeBg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${score.toInt()}% Match",
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCard(
    job: JobDto,
    onClick: () -> Unit,
    onSaveToggle: () -> Unit,
    isSaved: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Company & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = job.companyName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = job.companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        job.sourceName?.let {
                            Text(
                                text = "Source: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiaryLight
                            )
                        }
                    }
                }

                IconButton(onClick = onSaveToggle) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Job",
                        tint = if (isSaved) AccentBlue else TextTertiaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NavyPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Responsive Metadata Chips using FlowRow to prevent clipping
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (job.isIndia) {
                    MetadataChip(icon = Icons.Outlined.CheckCircle, text = "India Focus")
                }
                job.location?.let {
                    MetadataChip(icon = Icons.Outlined.LocationOn, text = it)
                }
                job.employmentType?.let {
                    MetadataChip(icon = Icons.Outlined.WorkOutline, text = it)
                }
                job.experienceLevel?.let {
                    MetadataChip(icon = Icons.Outlined.School, text = it)
                }
                if (job.isRemote) {
                    MetadataChip(icon = Icons.Outlined.Computer, text = "Remote")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = IndiaLocations.formatIndianSalary(job.salary),
                style = MaterialTheme.typography.labelMedium,
                color = if (job.salary != null) AccentBlue else TextSecondaryLight,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Match Score & Required Skills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MatchScoreBadge(score = job.matchScore)

                if (job.requiredSkills.isNotEmpty()) {
                    Text(
                        text = "${job.requiredSkills.size} skills required",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}

@Composable
fun MetadataChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondaryLight,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = TextSecondaryLight,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SkillChip(name: String, isMatched: Boolean? = null) {
    val (bgColor, textColor, borderStroke) = when (isMatched) {
        true -> Triple(Color(0xFFD1FAE5), MatchHigh, null)
        false -> Triple(Color(0xFFFEE2E2), MatchLow, null)
        null -> Triple(Color(0xFFF8F9FA), NavyPrimary, CardBorder)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = borderStroke?.let { androidx.compose.foundation.BorderStroke(1.dp, it) }
    ) {
        Text(
            text = name,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NavyLight,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = NavyPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = NavyPrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = actionButtonText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LoadingView(message: String = "Loading...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AccentBlue)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )
        }
    }
}

@Composable
fun ErrorView(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MatchLow,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = NavyPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyPrimary,
                contentColor = Color.White,
                disabledContainerColor = NavyPrimary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Standardized High-Contrast Text Field Colors ensuring pure black (#000000) input text,
 * clearly visible borders, distinct focused states, and accessible placeholder & label text.
 */
@Composable
fun careerConnectTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF000000),
    unfocusedTextColor = Color(0xFF000000),
    disabledTextColor = Color(0xFF64748B),
    errorTextColor = Color(0xFFB91C1C),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color(0xFFF8FAFC),
    errorContainerColor = Color.White,
    cursorColor = AccentBlue,
    errorCursorColor = Color(0xFFEF4444),
    selectionColors = TextSelectionColors(
        handleColor = AccentBlue,
        backgroundColor = Color(0xFFBFDBFE)
    ),
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = Color(0xFF94A3B8),
    disabledBorderColor = Color(0xFFE2E8F0),
    errorBorderColor = Color(0xFFEF4444),
    focusedLabelColor = AccentBlue,
    unfocusedLabelColor = NavyPrimary,
    disabledLabelColor = Color(0xFF94A3B8),
    errorLabelColor = Color(0xFFDC2626),
    focusedPlaceholderColor = Color(0xFF64748B),
    unfocusedPlaceholderColor = Color(0xFF64748B),
    disabledPlaceholderColor = Color(0xFF94A3B8),
    errorPlaceholderColor = Color(0xFF64748B),
    focusedLeadingIconColor = AccentBlue,
    unfocusedLeadingIconColor = Color(0xFF64748B),
    disabledLeadingIconColor = Color(0xFF94A3B8),
    errorLeadingIconColor = Color(0xFFDC2626),
    focusedTrailingIconColor = AccentBlue,
    unfocusedTrailingIconColor = Color(0xFF64748B),
    disabledTrailingIconColor = Color(0xFF94A3B8),
    errorTrailingIconColor = Color(0xFFDC2626)
)

/**
 * Standardized High-Contrast Text Style ensuring pure black (#000000) input typography.
 */
@Composable
fun careerConnectTextStyle() = TextStyle(
    color = Color(0xFF000000),
    fontSize = 15.sp,
    fontWeight = FontWeight.Normal
)

