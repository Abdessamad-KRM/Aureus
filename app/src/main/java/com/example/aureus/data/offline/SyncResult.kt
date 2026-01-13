package com.example.aureus.data.offline

/**
 * Résultat de synchronisation
 */
sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}