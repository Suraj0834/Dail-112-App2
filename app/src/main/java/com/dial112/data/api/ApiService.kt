package com.dial112.data.api

import com.dial112.data.model.request.*
import com.dial112.data.model.response.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ENDPOINTS ====================

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<LoginResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<ApiResponse>

    // ==================== CASES / FIR ENDPOINTS ====================

    @POST("api/cases")
    suspend fun createCase(
        @Header("Authorization") token: String,
        @Body request: CreateCaseRequest
    ): Response<CaseResponse>

    @GET("api/cases")
    suspend fun getCases(
        @Header("Authorization") token: String
    ): Response<CasesResponse>

    @GET("api/cases/{id}")
    suspend fun getCaseById(
        @Header("Authorization") token: String,
        @Path("id") caseId: String
    ): Response<CaseResponse>

    @PUT("api/cases/{id}/assign")
    suspend fun assignCase(
        @Header("Authorization") token: String,
        @Path("id") caseId: String,
        @Body request: Map<String, String>
    ): Response<CaseResponse>

    // ==================== SOS ENDPOINTS ====================

    @POST("api/sos")
    suspend fun triggerSOS(
        @Header("Authorization") token: String,
        @Body request: SOSRequest
    ): Response<SOSResponse>

    @GET("api/sos/history")
    suspend fun getSOSHistory(
        @Header("Authorization") token: String
    ): Response<SOSHistoryResponse>

    @GET("api/sos/active")
    suspend fun getActiveSOSAlerts(
        @Header("Authorization") token: String
    ): Response<SOSHistoryResponse>

    @POST("api/sos/{id}/assign")
    suspend fun respondToSOS(
        @Header("Authorization") token: String,
        @Path("id") sosId: String
    ): Response<ApiResponse>

    // ==================== CRIMINALS ENDPOINTS (Police only) ====================

    @GET("api/criminals")
    suspend fun searchCriminal(
        @Header("Authorization") token: String,
        @Query("search") query: String
    ): Response<CriminalSearchResponse>

    @GET("api/criminals/{id}")
    suspend fun getCriminalById(
        @Header("Authorization") token: String,
        @Path("id") criminalId: String
    ): Response<ApiResponse>

    // ==================== PCR VAN ENDPOINTS (Police only) ====================

    @GET("api/pcr-vans")
    suspend fun getPCRVans(
        @Header("Authorization") token: String
    ): Response<PCRVanResponse>

    @PUT("api/pcr-vans/{id}/location")
    suspend fun updateVanLocation(
        @Header("Authorization") token: String,
        @Path("id") vanId: String,
        @Body location: Map<String, Double>
    ): Response<ApiResponse>

    // ==================== AI SERVICE ENDPOINTS ====================

    @Multipart
    @POST("api/ai/face-recognition")
    suspend fun faceRecognition(
        @Header("Authorization") token: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<FaceRecognitionResponse>

    @Multipart
    @POST("api/ai/anpr")
    suspend fun anprRecognition(
        @Header("Authorization") token: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<ANPRResponse>

    @Multipart
    @POST("api/ai/detect-weapon")
    suspend fun detectWeapon(
        @Header("Authorization") token: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<WeaponDetectionResponse>

    @GET("api/ai/hotspots")
    suspend fun getCrimeHotspots(
        @Header("Authorization") token: String
    ): Response<HotspotsResponse>

    @POST("api/ai/chat")
    suspend fun chatWithAI(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
