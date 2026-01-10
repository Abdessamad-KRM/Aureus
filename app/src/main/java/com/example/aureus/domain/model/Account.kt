package com.example.aureus.domain.model

/**
 * Domain Model for Account
 */
data class Account(
    val id: String,
    val accountNumber: String,
    val accountType: String,
    val balance: Double,
    val currency: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)