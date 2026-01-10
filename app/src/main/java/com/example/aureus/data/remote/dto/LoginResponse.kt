package com.example.aureus.data.remote.dto

/**
 * Data Transfer Object for Login Response
 */
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: UserResponse
)