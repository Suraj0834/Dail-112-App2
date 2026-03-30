package com.dial112.data.repository

import com.dial112.data.api.ApiService
import com.dial112.data.local.TokenManager
import com.dial112.data.model.request.LoginRequest
import com.dial112.data.model.request.RegisterRequest
import com.dial112.data.model.response.ApiResponse
import com.dial112.data.model.response.LoginResponse
import com.dial112.data.model.response.User
import com.dial112.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                        // Save token and user
                        tokenManager.saveToken(loginResponse.token)
                        tokenManager.saveUser(loginResponse.user)
                        Resource.Success(loginResponse)
                    } else {
                        Resource.Error(loginResponse.message)
                    }
                } else {
                    Resource.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String = "citizen"
    ): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(
                    RegisterRequest(name, email, phone, password, role)
                )
                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()!!
                    if (registerResponse.success && registerResponse.token != null && registerResponse.user != null) {
                        // Save token and user
                        tokenManager.saveToken(registerResponse.token)
                        tokenManager.saveUser(registerResponse.user)
                        Resource.Success(registerResponse)
                    } else {
                        Resource.Error(registerResponse.message)
                    }
                } else {
                    Resource.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getProfile(): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.getProfile(token)
                if (response.isSuccessful && response.body() != null) {
                    val profileResponse = response.body()!!
                    if (profileResponse.success && profileResponse.user != null) {
                        // Update saved user
                        tokenManager.saveUser(profileResponse.user)
                        Resource.Success(profileResponse.user)
                    } else {
                        Resource.Error(profileResponse.message)
                    }
                } else {
                    Resource.Error("Failed to fetch profile: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun updateProfile(user: User): Resource<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = apiService.updateProfile(token, user)
                if (response.isSuccessful && response.body() != null) {
                    val updateResponse = response.body()!!
                    if (updateResponse.success) {
                        // Update saved user
                        tokenManager.saveUser(user)
                        Resource.Success(updateResponse)
                    } else {
                        Resource.Error(updateResponse.message)
                    }
                } else {
                    Resource.Error("Update failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        tokenManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getCurrentUser(): User? {
        return tokenManager.getUser()
    }
}
