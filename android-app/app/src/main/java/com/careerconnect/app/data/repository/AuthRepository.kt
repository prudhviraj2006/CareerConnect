package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.models.*
import com.careerconnect.app.data.preferences.UserPreferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val apiService: ApiService,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val accessTokenFlow: Flow<String?> = preferencesDataStore.accessTokenFlow
    val isOnboardingCompletedFlow: Flow<Boolean> = preferencesDataStore.isOnboardingCompletedFlow

    private fun extractErrorMessage(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return try {
            val json = org.json.JSONObject(errorBody)
            if (json.has("detail")) {
                val detail = json.get("detail")
                if (detail is org.json.JSONArray && detail.length() > 0) {
                    detail.getJSONObject(0).optString("msg", fallback)
                } else {
                    detail.toString()
                }
            } else if (json.has("message")) {
                json.getString("message")
            } else {
                fallback
            }
        } catch (_: Exception) {
            errorBody.take(150)
        }
    }

    suspend fun register(req: RegisterRequest): Result<TokenResponse> {
        return try {
            var firebaseToken: String? = null
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(req.email.trim(), req.password).await()
                val fbUser = authResult.user
                try {
                    fbUser?.updateProfile(userProfileChangeRequest { displayName = req.fullName })?.await()
                } catch (_: Exception) {}
                try {
                    fbUser?.sendEmailVerification()?.await()
                } catch (_: Exception) {}
                val tokenResult = fbUser?.getIdToken(true)?.await()
                firebaseToken = tokenResult?.token
            } catch (e: Exception) {
                // If user exists or Firebase offline, fallback to backend registration
            }

            val response = if (!firebaseToken.isNullOrBlank()) {
                apiService.authenticateWithFirebase(
                    FirebaseAuthRequest(
                        firebaseToken = firebaseToken,
                        email = req.email.trim(),
                        fullName = req.fullName,
                        phoneNumber = req.phoneNumber
                    )
                )
            } else {
                apiService.register(req)
            }

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                preferencesDataStore.saveAuthTokens(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                    userId = token.user.id,
                    email = token.user.email,
                    name = token.user.fullName
                )
                Result.success(token)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = extractErrorMessage(rawError, "Registration failed.")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(req: LoginRequest): Result<TokenResponse> {
        return try {
            var firebaseToken: String? = null
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(req.email.trim(), req.password).await()
                val fbUser = authResult.user
                val tokenResult = fbUser?.getIdToken(true)?.await()
                firebaseToken = tokenResult?.token
            } catch (e: Exception) {
                // If not registered in Firebase yet, will fall back to backend DB credentials
            }

            val response = if (!firebaseToken.isNullOrBlank()) {
                apiService.authenticateWithFirebase(
                    FirebaseAuthRequest(
                        firebaseToken = firebaseToken,
                        email = req.email.trim()
                    )
                )
            } else {
                apiService.login(req)
            }

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                preferencesDataStore.saveAuthTokens(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                    userId = token.user.id,
                    email = token.user.email,
                    name = token.user.fullName
                )
                Result.success(token)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = extractErrorMessage(rawError, "Invalid login credentials.")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            try {
                firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            } catch (_: Exception) {}
            val response = apiService.forgotPassword(ForgotPasswordRequest(email.trim()))
            if (response.isSuccessful) {
                Result.success("Password reset instructions sent.")
            } else {
                Result.success("If an account exists for this email, password reset instructions have been sent.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(token: String, newPass: String): Result<String> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(token, newPass))
            if (response.isSuccessful) {
                Result.success("Password has been reset successfully.")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed reset."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<String> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Result.success("Verification email sent to ${user.email}.")
            } else {
                Result.failure(Exception("No authenticated user found."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(email: String, fullName: String? = null, idToken: String? = null): Result<TokenResponse> {
        return try {
            var firebaseToken = idToken
            if (!idToken.isNullOrBlank()) {
                try {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(credential).await()
                    val fbUser = authResult.user
                    val tokenResult = fbUser?.getIdToken(true)?.await()
                    if (!tokenResult?.token.isNullOrBlank()) {
                        firebaseToken = tokenResult?.token
                    }
                } catch (e: Exception) {
                    // Fall back to direct ID token
                }
            }

            val response = if (!firebaseToken.isNullOrBlank()) {
                apiService.authenticateWithFirebase(
                    FirebaseAuthRequest(
                        firebaseToken = firebaseToken,
                        email = email,
                        fullName = fullName
                    )
                )
            } else {
                apiService.loginWithGoogle(
                    GoogleLoginRequest(email = email, fullName = fullName, idToken = idToken)
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                preferencesDataStore.saveAuthTokens(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                    userId = token.user.id,
                    email = token.user.email,
                    name = token.user.fullName
                )
                preferencesDataStore.setOnboardingCompleted(true)
                Result.success(token)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = extractErrorMessage(rawError, "Google authentication failed.")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setOnboardingCompleted() {
        preferencesDataStore.setOnboardingCompleted(true)
    }

    suspend fun logout() {
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {}
        preferencesDataStore.clearAuth()
    }
}
