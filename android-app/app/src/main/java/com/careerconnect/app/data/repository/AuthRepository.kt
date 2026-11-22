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
                val errorMsg = response.errorBody()?.string() ?: "Registration failed."
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
                val errorMsg = response.errorBody()?.string() ?: "Invalid login credentials."
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

    suspend fun loginStandalone(): Result<TokenResponse> {
        val standaloneUser = UserDto(
            id = 1,
            email = "candidate@careerconnect.cloud",
            fullName = "Alex Chen",
            phoneNumber = "+1-555-0199",
            isActive = true,
            isVerified = true
        )
        val token = TokenResponse(
            accessToken = "cloud_standalone_token_2026",
            tokenType = "bearer",
            refreshToken = "cloud_refresh_token_2026",
            user = standaloneUser
        )
        preferencesDataStore.saveAuthTokens(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            userId = token.user.id,
            email = token.user.email,
            name = token.user.fullName
        )
        preferencesDataStore.setOnboardingCompleted(true)
        return Result.success(token)
    }

    suspend fun setOnboardingCompleted() {
        preferencesDataStore.setOnboardingCompleted(true)
    }

    suspend fun logout() {
        preferencesDataStore.clearAuth()
    }
}
