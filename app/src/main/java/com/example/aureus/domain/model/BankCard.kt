package com.example.aureus.domain.model

/**
 * Bank Card Data Model
 * Used by CardScreens and Firebase integration
 * Enhanced with all fields for Phase 3
 */
data class BankCard(
    val id: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val balance: Double,
    val cardType: CardType,
    val cardColor: CardColor,
    val isDefault: Boolean = false,
    // New fields for Phase 3
    val accountId: String = "",
    val isActive: Boolean = true,
    val status: CardStatus = CardStatus.ACTIVE,
    val dailyLimit: Double = 10000.0,
    val monthlyLimit: Double = 50000.0,
    val spendingToday: Double = 0.0,
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class CardType {
    VISA, MASTERCARD, AMEX, DISCOVER
}

enum class CardColor {
    NAVY, GOLD, BLACK, BLUE, PURPLE, GREEN
}

enum class CardStatus {
    ACTIVE, FROZEN, EXPIRED, BLOCKED, PENDING
}