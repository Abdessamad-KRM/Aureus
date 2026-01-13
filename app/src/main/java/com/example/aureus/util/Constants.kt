package com.example.aureus.util

/**
 * App Constants
 */
object Constants {

    // API
    const val BASE_URL = "https://api.mybank.test/"
    const val TIMEOUT_SECONDS = 30L

    // SharedPreferences
    const val PREFS_NAME = "MyBankPrefs"

    // Notification Channels - Unified convention
    const val CHANNEL_TRANSACTION = "transactions"
    const val CHANNEL_ALERTS = "alerts"
    const val CHANNEL_TRANSFER = "transfers"
    const val CHANNEL_INFO = "aureus_notifications"

    // Notification IDs
    const val NOTIFICATION_ID_TRANSACTION = 1001
    const val NOTIFICATION_ID_LOW_BALANCE = 1002
    const val NOTIFICATION_ID_INFO = 1003
    const val NOTIFICATION_ID_TRANSFER = 1004
}