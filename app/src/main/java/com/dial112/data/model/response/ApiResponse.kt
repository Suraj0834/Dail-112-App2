package com.dial112.data.model.response

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)
