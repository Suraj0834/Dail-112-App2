package com.dial112.data.model.request

data class CreateCaseRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
