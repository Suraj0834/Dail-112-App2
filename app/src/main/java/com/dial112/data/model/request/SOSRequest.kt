package com.dial112.data.model.request

data class SOSRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
