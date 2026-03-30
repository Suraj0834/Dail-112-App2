package com.dial112.data.model.response

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)
