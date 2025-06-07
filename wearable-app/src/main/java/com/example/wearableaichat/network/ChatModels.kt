package com.example.wearableaichat.network

data class ChatRequest(val message: String)

data class ChatResponse(
    val response: String?,
    val error: String?
)
