package com.dial112.data.model.response

data class CaseResponse(
    val success: Boolean,
    val message: String,
    val case: Case?
)

data class CasesResponse(
    val success: Boolean,
    val message: String,
    val cases: List<Case>?
)

data class AssignedOfficer(
    val id: String?,
    val name: String?,
    val badgeId: String?,
    val phone: String?
)

data class Case(
    val id: String?,
    val title: String?,
    val description: String?,
    val category: String?,
    val status: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val userId: String?,
    val assignedOfficer: AssignedOfficer?,
    val assignedTo: String?,
    val priority: String? = "Medium",
    val createdAt: String?,
    val updatedAt: String?
)
