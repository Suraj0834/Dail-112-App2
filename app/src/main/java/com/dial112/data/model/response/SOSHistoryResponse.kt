package com.dial112.data.model.response

data class SOSHistoryResponse(
    val success: Boolean,
    val message: String,
    val sosAlerts: List<SOS>?
)
