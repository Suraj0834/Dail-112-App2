package com.dial112.data.model.response

data class CriminalSearchResponse(
    val success: Boolean,
    val message: String,
    val criminals: List<Criminal>?
)

data class Criminal(
    val id: String,
    val name: String,
    val age: Int?,
    val description: String?,
    val crimes: List<String>?,
    val status: String,
    val imageUrl: String?,
    val lastSeen: String?,
    val dangerLevel: String? = "Medium"
)
