package com.dial112.data.model.request

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "citizen"
)
