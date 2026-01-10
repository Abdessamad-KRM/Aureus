package com.example.aureus.data.remote.dto

/**
 * Data Transfer Object for Transaction Response
 */
data class TransactionResponse(
    val id: String,
    val accountId: String,
    val type: String, // "CREDIT" or "DEBIT"
    val amount: Double,
    val description: String,
    val category: String? = null,
    val merchant: String? = null,
    val date: String,
    val balanceAfter: Double
)