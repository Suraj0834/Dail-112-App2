package com.dial112.data.model.response

data class HotspotsResponse(
    val success: Boolean,
    val hotspots: List<CrimeHotspot>
)

data class CrimeHotspot(
    val latitude: Double,
    val longitude: Double,
    val riskScore: Double,
    val crimeCount: Int,
    val area: String?
)
