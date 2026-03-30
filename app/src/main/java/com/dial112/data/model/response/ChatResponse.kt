package com.dial112.data.model.response

data class ChatResponse(
    val success: Boolean,
    val response: String,
    val sessionId: String?
)
