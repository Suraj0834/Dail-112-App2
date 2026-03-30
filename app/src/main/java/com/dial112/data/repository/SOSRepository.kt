package com.dial112.data.repository

import com.dial112.data.api.ApiService
import com.dial112.data.local.TokenManager
import com.dial112.data.model.request.SOSRequest
import com.dial112.data.model.response.SOS
import com.dial112.data.model.response.SOSResponse
import com.dial112.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SOSRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun triggerSOS(
        latitude: Double,
        longitude: Double,
        address: String? = null
    ): Resource<SOS> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val request = SOSRequest(latitude, longitude, address)
                val response = apiService.triggerSOS(token, request)

                if (response.isSuccessful && response.body() != null) {
                    val sosResponse = response.body()!!
                    if (sosResponse.success && sosResponse.sos != null) {
                        Resource.Success(sosResponse.sos)
                    } else {
                        Resource.Error(sosResponse.message)
                    }
                } else {
                    Resource.Error("Failed to trigger SOS: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getSOSHistory(): Resource<List<SOS>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getSOSHistory(token)

                if (response.isSuccessful && response.body() != null) {
                    val sosHistoryResponse = response.body()!!
                    if (sosHistoryResponse.success) {
                        Resource.Success(sosHistoryResponse.sosAlerts ?: emptyList())
                    } else {
                        Resource.Error(sosHistoryResponse.message)
                    }
                } else {
                    Resource.Error("Failed to fetch SOS history: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getActiveSOSAlerts(): Resource<List<SOS>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getActiveSOSAlerts(token)

                if (response.isSuccessful && response.body() != null) {
                    val sosAlertsResponse = response.body()!!
                    if (sosAlertsResponse.success) {
                        Resource.Success(sosAlertsResponse.sosAlerts ?: emptyList())
                    } else {
                        Resource.Error(sosAlertsResponse.message)
                    }
                } else {
                    Resource.Error("Failed to fetch active SOS alerts: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun respondToSOS(sosId: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.respondToSOS(token, sosId)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.message)
                    } else {
                        Resource.Error(apiResponse.message)
                    }
                } else {
                    Resource.Error("Failed to respond to SOS: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
