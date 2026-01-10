package com.example.aureus.data.remote.dto

/**
 * Data Transfer Object for User Response
 */
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val createdAt: String,
    val updatedAt: String
)