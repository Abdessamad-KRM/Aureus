package com.example.aureus.data.remote.dto

/**
 * Data Transfer Object for Account Response
 */
data class AccountResponse(
    val id: String,
    val accountNumber: String,
    val accountType: String,
    val balance: Double,
    val currency: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)