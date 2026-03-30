package com.dial112.data.model.request

data class ChatRequest(
    val message: String,
    val sessionId: String? = null
)
