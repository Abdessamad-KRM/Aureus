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

    // Notification Channels
    const val CHANNEL_TRANSACTION = "channel_transactions"
    const val CHANNEL_ALERTS = "channel_alerts"
    const val CHANNEL_INFO = "channel_info"

    // Notification IDs
    const val NOTIFICATION_ID_TRANSACTION = 1001
    const val NOTIFICATION_ID_LOW_BALANCE = 1002
    const val NOTIFICATION_ID_INFO = 1003
}