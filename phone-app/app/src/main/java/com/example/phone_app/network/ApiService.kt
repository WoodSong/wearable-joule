package com.example.phone_app.network // Changed package name

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("chat") // Endpoint as defined in backend-service/app.py
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/" // Changed for Android emulator to reach host's localhost from a different app module if needed, though 127.0.0.1 should also work. 10.0.2.2 is more standard.

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Add OkHttp client with interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
