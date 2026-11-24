package com.careerconnect.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.careerconnect.app.ui.components.CareerConnectTopBar
import com.careerconnect.app.ui.theme.*
import com.careerconnect.app.ui.viewmodels.ResumeUiState
import com.careerconnect.app.ui.viewmodels.ResumeViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun ResumeUploadScreen(
    resumeViewModel: ResumeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val context = LocalContext.current
    val uiState by resumeViewModel.uiState.collectAsState()

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long?>(null) }
    var localFile by remember { mutableStateOf<File?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is ResumeUiState.Parsed) {
            onNavigateToReview()
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(it) ?: ""
                var name = "resume.pdf"
                contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        name = cursor.getString(nameIdx)
                        selectedFileSize = cursor.getLong(sizeIdx)
                    }
                }
                selectedFileName = name

                // Validation
                val ext = name.substringAfterLast(".", "").lowercase()
                if (ext !in listOf("pdf", "docx", "doc")) {
                    localError = "Unsupported file type. Please select a PDF or DOCX file."
                    return@let
                }

                if ((selectedFileSize ?: 0L) > 10 * 1024 * 1024) {
                    localError = "File exceeds maximum size of 10MB."
                    return@let
                }

                // Copy to cache file
                val tempFile = File(context.cacheDir, "upload_$name")
                contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                localFile = tempFile
                localError = null
            } catch (e: Exception) {
                localError = "Failed to read file: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            CareerConnectTopBar(
                title = "Upload Resume",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Upload Your Resume",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We will extract skills, education, and experience for your review before saving.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Upload Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(
                            width = 2.dp,
                            color = if (selectedFileName != null) AccentBlue else CardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            filePicker.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/msword"
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedFileName != null) Icons.Default.Description else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (selectedFileName != null) AccentBlue else NavyLight,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedFileName ?: "Tap to select PDF or DOCX file",
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedFileSize != null) "${selectedFileSize!! / 1024} KB" else "Maximum file size: 10 MB",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message
                val err = localError ?: (uiState as? ResumeUiState.Error)?.message
                if (err != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = MatchLow,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transparency Notice
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Transparent Extraction Notice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CareerConnect extracts raw text and runs an open taxonomy keyword parser. Extracted data will NOT alter your verified profile until you inspect and confirm it.",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            // Bottom Action
            Button(
                onClick = {
                    localFile?.let {
                        resumeViewModel.uploadResumeFile(it)
                    }
                },
                enabled = localFile != null && uiState !is ResumeUiState.Uploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = NavyPrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                if (uiState is ResumeUiState.Uploading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Uploading & Extracting...", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                } else {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload & Extract Resume", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
