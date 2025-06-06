package com.example.phone_app.network // Changed package name

data class ChatRequest(val message: String)

data class ChatResponse(
    val response: String?,
    val error: String?
)
