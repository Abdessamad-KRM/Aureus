package com.example.aureus.domain.model

/**
 * Domain Model for User
 */
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val createdAt: String,
    val updatedAt: String
)