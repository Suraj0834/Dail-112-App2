package com.dial112.data.model.response

data class PCRVanResponse(
    val success: Boolean,
    val message: String,
    val vans: List<PCRVan>?
)

data class PCRVan(
    val id: String,
    val vehicleNumber: String,
    val officerId: String,
    val officerName: String,
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    val lastUpdated: String?
)
