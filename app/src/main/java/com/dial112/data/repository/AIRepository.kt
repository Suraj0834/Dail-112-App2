package com.dial112.data.repository

import com.dial112.data.api.ApiService
import com.dial112.data.local.TokenManager
import com.dial112.data.model.request.ChatRequest
import com.dial112.data.model.response.*
import com.dial112.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AIRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun recognizeFace(imageFile: File): Resource<FaceRecognitionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val requestBody = imageFile.asRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

                val response = apiService.faceRecognition(token, part)

                if (response.isSuccessful && response.body() != null) {
                    val faceResponse = response.body()!!
                    if (faceResponse.success) {
                        Resource.Success(faceResponse)
                    } else {
                        Resource.Error("Face recognition failed")
                    }
                } else {
                    Resource.Error("Failed to recognize face: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun recognizeNumberPlate(imageFile: File): Resource<ANPRResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val requestBody = imageFile.asRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

                val response = apiService.anprRecognition(token, part)

                if (response.isSuccessful && response.body() != null) {
                    val anprResponse = response.body()!!
                    if (anprResponse.success) {
                        Resource.Success(anprResponse)
                    } else {
                        Resource.Error("ANPR recognition failed")
                    }
                } else {
                    Resource.Error("Failed to recognize number plate: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun detectWeapon(imageFile: File): Resource<WeaponDetectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val requestBody = imageFile.asRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

                val response = apiService.detectWeapon(token, part)

                if (response.isSuccessful && response.body() != null) {
                    val weaponResponse = response.body()!!
                    if (weaponResponse.success) {
                        Resource.Success(weaponResponse)
                    } else {
                        Resource.Error("Weapon detection failed")
                    }
                } else {
                    Resource.Error("Failed to detect weapon: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getCrimeHotspots(): Resource<List<CrimeHotspot>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getCrimeHotspots(token)

                if (response.isSuccessful && response.body() != null) {
                    val hotspotsResponse = response.body()!!
                    if (hotspotsResponse.success) {
                        Resource.Success(hotspotsResponse.hotspots)
                    } else {
                        Resource.Error("Failed to fetch hotspots")
                    }
                } else {
                    Resource.Error("Failed to fetch hotspots: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun chatWithAI(message: String, sessionId: String? = null): Resource<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val request = ChatRequest(message, sessionId)
                val response = apiService.chatWithAI(token, request)

                if (response.isSuccessful && response.body() != null) {
                    val chatResponse = response.body()!!
                    if (chatResponse.success) {
                        Resource.Success(chatResponse)
                    } else {
                        Resource.Error("Chat failed")
                    }
                } else {
                    Resource.Error("Failed to chat with AI: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
