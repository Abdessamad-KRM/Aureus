package com.example.aureus.data.remote.dto

/**
 * Data Transfer Object for Register Request
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)