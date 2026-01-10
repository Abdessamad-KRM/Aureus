package com.example.aureus.data.remote.dto

/**
 * Data Transfer Object for Login Request
 */
data class LoginRequest(
    val email: String,
    val password: String
)