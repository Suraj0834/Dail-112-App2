package com.dial112.data.model.response

data class FaceRecognitionResponse(
    val success: Boolean,
    val match: Boolean,
    val confidence: Double,
    val criminal: Criminal?
)
