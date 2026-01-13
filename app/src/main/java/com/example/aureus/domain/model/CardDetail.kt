package com.example.aureus.domain.model

import java.util.Date

/**
 * Détails complets d'une carte bancaire
 * Utilisé pour l'écran CardDetailScreen avec informations étendues
 */
data class CardDetail(
    val id: String,
    val userId: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val cvv: String,  // Partiellement masqué
    val cardType: CardType,
    val cardColor: CardColor,
    val isDefault: Boolean,
    val isActive: Boolean,
    val balance: Double,
    val currency: String,
    val dailyLimit: Double,
    val monthlyLimit: Double,
    val spendingToday: Double,
    val spendingMonth: Double,
    val status: CardStatus = CardStatus.ACTIVE,
    val createdAt: Date,
    val lastUsed: Date? = null,
    val securitySettings: CardSecuritySettings = CardSecuritySettings()
)

data class CardSecuritySettings(
    val requirePinForOnline: Boolean = true,
    val requirePinForContactless: Boolean = true,
    val maxDailyAmount: Double = 10000.0,
    val abroadEnabled: Boolean = false
)