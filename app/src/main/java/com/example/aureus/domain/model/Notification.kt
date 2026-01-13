package com.example.aureus.domain.model

import java.util.Date

/**
 * Modèle de notification pour l'historique
 */
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val timestamp: Date = Date(),
    val imageUrl: String? = null
)

/**
 * Types de notifications supportés
 */
enum class NotificationType {
    TRANSACTION,
    TRANSFER_RECEIVED,
    TRANSFER_SENT,
    BALANCE_ALERT,
    SECURITY_ALERT,
    PROMOTION,
    INFO,
    SYSTEM
}

/**
 * Modèle de préférences de notification
 */
data class NotificationPreferences(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val transactionAlerts: Boolean = true,
    val lowBalanceAlerts: Boolean = true,
    val transferAlerts: Boolean = true,
    val promotionalNotifications: Boolean = false,
    val quietHours: QuietHours? = null
)

/**
 * Config des heures silencieuses
 */
data class QuietHours(
    val enabled: Boolean = false,
    val startTime: String = "22:00", // 10 PM
    val endTime: String = "08:00"   // 8 AM
)