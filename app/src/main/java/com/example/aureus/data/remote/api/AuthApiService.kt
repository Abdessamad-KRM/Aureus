package com.example.aureus.data.remote.api

import com.example.aureus.data.remote.dto.LoginRequest
import com.example.aureus.data.remote.dto.LoginResponse
import com.example.aureus.data.remote.dto.RegisterRequest
import com.example.aureus.data.remote.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Authentication API Service
 * Handles all authentication-related API calls
 */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserResponse>
}