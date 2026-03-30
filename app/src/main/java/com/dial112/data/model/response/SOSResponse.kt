package com.dial112.data.model.response

data class SOSResponse(
    val success: Boolean,
    val message: String,
    val sos: SOS?
)

data class SOS(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val status: String,
    val createdAt: String
)
