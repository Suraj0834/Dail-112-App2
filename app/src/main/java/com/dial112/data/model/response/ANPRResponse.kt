package com.dial112.data.model.response

data class ANPRResponse(
    val success: Boolean,
    val plateNumber: String?,
    val confidence: Double,
    val vehicle: VehicleInfo?
)

data class VehicleInfo(
    val ownerName: String?,
    val vehicleType: String?,
    val model: String?,
    val isStolenFlagged: Boolean
)
