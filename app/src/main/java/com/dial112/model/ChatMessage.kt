package com.dial112.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val attachmentUrl: String? = null,
    val isTyping: Boolean = false
)
