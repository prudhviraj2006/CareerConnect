package com.careerconnect.app.data.api

import android.content.Context
import com.careerconnect.app.data.preferences.UserPreferencesDataStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Live Cloud Backend (HTTPS) hosted on Render.com
    private var customBaseUrl: String? = null

    private const val DEFAULT_BASE_URL = "https://careerconnect-backend-cpki.onrender.com/"

    fun getBaseUrl(): String {
        return customBaseUrl ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(url: String) {
        customBaseUrl = if (url.endsWith("/")) url else "$url/"
        retrofitInstance = null
    }

    private var retrofitInstance: Retrofit? = null

    fun getApiService(context: Context): ApiService {
        if (retrofitInstance == null) {
            val preferencesDataStore = UserPreferencesDataStore(context)

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(preferencesDataStore))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofitInstance = Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofitInstance!!.create(ApiService::class.java)
    }
}
