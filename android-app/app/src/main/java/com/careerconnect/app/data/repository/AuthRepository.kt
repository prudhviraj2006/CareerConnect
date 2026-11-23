package com.careerconnect.app.data.repository

import com.careerconnect.app.data.api.ApiService
import com.careerconnect.app.data.models.*
import com.careerconnect.app.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val apiService: ApiService,
    private val preferencesDataStore: UserPreferencesDataStore
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
            val response = apiService.register(req)
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
            val response = apiService.login(req)
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
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success("Password reset instructions generated.")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed request."))
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

    suspend fun loginWithGoogle(email: String, fullName: String? = null, idToken: String? = null): Result<TokenResponse> {
        return try {
            val response = apiService.loginWithGoogle(
                GoogleLoginRequest(email = email, fullName = fullName, idToken = idToken)
            )
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
        preferencesDataStore.clearAuth()
    }
}
