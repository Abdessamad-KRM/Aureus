package com.example.aureus.domain.model

/**
 * Domain Model for User
 * Enriched with profile settings and multi-device support (Phase 5)
 */
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val createdAt: String,
    val updatedAt: String,
    // Profile settings (Phase 5 additions)
    val profileImage: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String = "Morocco",
    val preferredLanguage: String = "fr",
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val securitySettings: SecuritySettings = SecuritySettings(),
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)

/**
 * Notification settings for user profile
 */
data class NotificationSettings(
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = true,
    val transactionAlerts: Boolean = true,
    val lowBalanceAlerts: Boolean = true,
    val promotionalEmails: Boolean = false
)

/**
 * Security settings for user profile
 */
data class SecuritySettings(
    val biometricEnabled: Boolean = false,
    val twoFactorAuth: Boolean = false,
    val pinCode: String? = null,
    val sessionTimeout: Int = 30 // minutes
)

/**
 * Extension function to map Firebase card data to CardDetail domain model
 */
fun mapToCardDetail(cardData: Map<String, Any>): CardDetail {
    val date = com.google.firebase.Timestamp.now().toDate()
    return CardDetail(
        id = cardData["cardId"] as? String ?: "",
        userId = cardData["userId"] as? String ?: "",
        cardNumber = cardData["cardNumber"] as? String ?: "",
        cardHolder = cardData["cardHolder"] as? String ?: "",
        expiryDate = cardData["expiryDate"] as? String ?: "",
        cvv = "***",
        cardType = when (cardData["cardType"] as? String ?: "VISA") {
            "VISA" -> CardType.VISA
            "MASTERCARD" -> CardType.MASTERCARD
            "AMEX" -> CardType.AMEX
            "DISCOVER" -> CardType.DISCOVER
            else -> CardType.VISA
        },
        cardColor = when ((cardData["cardColor"] as? String ?: "navy").lowercase()) {
            "navy" -> CardColor.NAVY
            "gold" -> CardColor.GOLD
            "black" -> CardColor.BLACK
            "blue" -> CardColor.BLUE
            "purple" -> CardColor.PURPLE
            "green" -> CardColor.GREEN
            else -> CardColor.NAVY
        },
        isDefault = cardData["isDefault"] as? Boolean ?: false,
        isActive = cardData["isActive"] as? Boolean ?: true,
        balance = (cardData["balance"] as? Number)?.toDouble() ?: 0.0,
        currency = "MAD",
        dailyLimit = (cardData["dailyLimit"] as? Number)?.toDouble() ?: 10000.0,
        monthlyLimit = (cardData["monthlyLimit"] as? Number)?.toDouble() ?: 50000.0,
        spendingToday = (cardData["spendingToday"] as? Number)?.toDouble() ?: 0.0,
        spendingMonth = 0.0,  // Calculé depuis transactions
        status = when ((cardData["status"] as? String) ?: "ACTIVE") {
            "ACTIVE" -> CardStatus.ACTIVE
            "BLOCKED" -> CardStatus.BLOCKED
            "EXPIRED" -> CardStatus.EXPIRED
            "FROZEN" -> CardStatus.FROZEN
            "PENDING" -> CardStatus.PENDING
            else -> CardStatus.ACTIVE
        },
        createdAt = date,
        lastUsed = date
    )
}