package com.dial112.data.model.response

data class WeaponDetectionResponse(
    val success: Boolean,
    val weaponDetected: Boolean,
    val detections: List<WeaponDetection>
)

data class WeaponDetection(
    val label: String,
    val confidence: Double,
    val bbox: List<Int>
)
