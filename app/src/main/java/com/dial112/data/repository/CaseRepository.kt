package com.dial112.data.repository

import com.dial112.data.api.ApiService
import com.dial112.data.local.TokenManager
import com.dial112.data.model.request.CreateCaseRequest
import com.dial112.data.model.response.Case
import com.dial112.data.model.response.CaseResponse
import com.dial112.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CaseRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun createCase(
        title: String,
        description: String,
        category: String,
        location: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Resource<Case> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val request = CreateCaseRequest(title, description, category, location, latitude, longitude)
                val response = apiService.createCase(token, request)

                if (response.isSuccessful && response.body() != null) {
                    val caseResponse = response.body()!!
                    if (caseResponse.success && caseResponse.case != null) {
                        Resource.Success(caseResponse.case)
                    } else {
                        Resource.Error(caseResponse.message)
                    }
                } else {
                    Resource.Error("Failed to create case: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getCases(): Resource<List<Case>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getCases(token)

                if (response.isSuccessful && response.body() != null) {
                    val casesResponse = response.body()!!
                    if (casesResponse.success) {
                        Resource.Success(casesResponse.cases ?: emptyList())
                    } else {
                        Resource.Error(casesResponse.message)
                    }
                } else {
                    Resource.Error("Failed to fetch cases: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getCaseById(caseId: String): Resource<Case> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getCaseById(token, caseId)

                if (response.isSuccessful && response.body() != null) {
                    val caseResponse = response.body()!!
                    if (caseResponse.success && caseResponse.case != null) {
                        Resource.Success(caseResponse.case)
                    } else {
                        Resource.Error(caseResponse.message)
                    }
                } else {
                    Resource.Error("Failed to fetch case: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun assignCase(caseId: String, officerId: String): Resource<Case> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val request = mapOf("officerId" to officerId)
                val response = apiService.assignCase(token, caseId, request)

                if (response.isSuccessful && response.body() != null) {
                    val caseResponse = response.body()!!
                    if (caseResponse.success && caseResponse.case != null) {
                        Resource.Success(caseResponse.case)
                    } else {
                        Resource.Error(caseResponse.message)
                    }
                } else {
                    Resource.Error("Failed to assign case: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
