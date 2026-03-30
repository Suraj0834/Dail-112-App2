package com.dial112.data.repository

import com.dial112.data.api.ApiService
import com.dial112.data.local.TokenManager
import com.dial112.data.model.response.Criminal
import com.dial112.data.model.response.PCRVan
import com.dial112.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PoliceRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun searchCriminal(query: String): Resource<List<Criminal>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.searchCriminal(token, query)

                if (response.isSuccessful && response.body() != null) {
                    val searchResponse = response.body()!!
                    if (searchResponse.success) {
                        Resource.Success(searchResponse.criminals ?: emptyList())
                    } else {
                        Resource.Error(searchResponse.message)
                    }
                } else {
                    Resource.Error("Failed to search criminals: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getPCRVans(): Resource<List<PCRVan>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getPCRVans(token)

                if (response.isSuccessful && response.body() != null) {
                    val vansResponse = response.body()!!
                    if (vansResponse.success) {
                        Resource.Success(vansResponse.vans ?: emptyList())
                    } else {
                        Resource.Error(vansResponse.message)
                    }
                } else {
                    Resource.Error("Failed to fetch PCR vans: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun updateVanLocation(
        vanId: String,
        latitude: Double,
        longitude: Double
    ): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val location = mapOf("latitude" to latitude, "longitude" to longitude)
                val response = apiService.updateVanLocation(token, vanId, location)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.message)
                    } else {
                        Resource.Error(apiResponse.message)
                    }
                } else {
                    Resource.Error("Failed to update van location: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
