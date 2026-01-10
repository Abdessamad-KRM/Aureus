package com.example.aureus.domain.model

/**
 * Domain Model for Transaction
 */
data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val category: String? = null,
    val merchant: String? = null,
    val date: String,
    val balanceAfter: Double
) {
    val isCredit: Boolean
        get() = type == TransactionType.CREDIT

    val isDebit: Boolean
        get() = type == TransactionType.DEBIT
}

enum class TransactionType {
    CREDIT,
    DEBIT
}