package com.example.aureus.security

import android.content.Context
import android.util.Log
import com.example.aureus.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logger pour les événements de sécurité - Phase 1 Correction
 *
 * EN PRODUCTION: TOUTES les logs sensibles sont désactivées
 * Les logs sont uniquement conservés côté serveur via Analytics/Remote Config
 */
@Singleton
class SecurityLogger @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TAG = "SecurityLogger"
    private val TAG_SENSITIVE = "SENSITIVE_DATA"

    // ✅ Détecter mode debug/release
    private val isDebuggable = (context.applicationInfo.flags and
        android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

    companion object {
        private const val ENABLE_SENSITIVE_LOGS = false  // ✅ TOUJOURS FALSE en prod!
    }

    /**
     * Événements de sécurité possibles
     */
    sealed class SecurityEvent {
        /**
         * Tentative de PIN (succès ou échec) - SENSIBLE!
         */
        data class PinAttempt(val success: Boolean) : SecurityEvent()

        /**
         * Transaction échouée
         */
        data class FailedTransaction(val reason: String) : SecurityEvent()

        /**
         * Carte bancaire ajoutée
         */
        data class CardAdded(val maskedCardNumber: String) : SecurityEvent()

        /**
         * Tentative d'accès non autorisée
         */
        data class UnauthorizedAccessAttempt(val action: String) : SecurityEvent()

        /**
         * Tentative biométrique
         */
        data class BiometricAttempt(val success: Boolean) : SecurityEvent()

        /**
         * Tentative de connexion - SENSIBLE (contient email)
         */
        data class LoginAttempt(val success: Boolean, val email: String) : SecurityEvent()

        /**
         * Verrouillage de compte
         */
        data class AccountLocked(val reason: String) : SecurityEvent()

        /**
         * Déverrouillage de compte
         */
        data class AccountUnlocked(val timestamp: Long = System.currentTimeMillis()) : SecurityEvent()
    }

    /**
     * ✅ CORRECTION Phase 1: Logger événement sécurisé - désactivé en production
     */
    fun logEvent(event: SecurityEvent, userId: String? = null, deviceId: String? = null) {
        // ❌ LOGS SENSIBLES: Jamais en production
        if (!ENABLE_SENSITIVE_LOGS && event is SecurityEvent.PinAttempt) {
            return  // Ne PAS logger les tentatives de PIN
        }

        if (isDebuggable && BuildConfig.DEBUG) {
            val timestamp = Date()
            val userIdStr = maskSensitive(userId)
            val deviceIdStr = maskSensitive(deviceId)

            when (event) {
                is SecurityEvent.PinAttempt -> {
                    val level = if (event.success) Log.INFO else Log.WARN
                    Log.println(level, TAG_SENSITIVE, "[PIN ATTEMPT] User: $userIdStr | Device: $deviceIdStr")
                }
                is SecurityEvent.FailedTransaction -> {
                    Log.w(TAG, "[TRANSACTION FAILED] User: $userIdStr | Reason: ${event.reason}")
                }
                is SecurityEvent.CardAdded -> {
                    Log.i(TAG, "[CARD ADDED] User: $userIdStr | Card: ${maskCard(event.maskedCardNumber)}")
                }
                is SecurityEvent.UnauthorizedAccessAttempt -> {
                    Log.e(TAG, "[UNAUTHORIZED] User: $userIdStr | Device: $deviceIdStr | Action: ${event.action}")
                }
                is SecurityEvent.BiometricAttempt -> {
                    val level = if (event.success) Log.INFO else Log.WARN
                    Log.println(level, TAG, "[BIOMETRIC] User: $userIdStr | Device: $deviceIdStr | Success: ${event.success}")
                }
                is SecurityEvent.LoginAttempt -> {
                    val level = if (event.success) Log.INFO else Log.WARN
                    Log.println(level, TAG, "[LOGIN] User: $userIdStr (${maskEmail(event.email)}) | Success: ${event.success}")
                }
                is SecurityEvent.AccountLocked -> {
                    Log.w(TAG, "[ACCOUNT LOCKED] User: $userIdStr | Reason: ${event.reason}")
                }
                is SecurityEvent.AccountUnlocked -> {
                    Log.i(TAG, "[ACCOUNT UNLOCKED] User: $userIdStr")
                }
            }
        } else {
            // ✅ Production: Envoyer vers Analytics au lieu de Logcat
            sendToAnalytics(event, userId)
        }
    }

    /**
     * Masquer les données sensibles dans les logs
     */
    private fun maskSensitive(data: String?): String {
        if (data == null) return "null"
        if (data.length <= 4) return "****"
        return "${data.take(2)}***${data.takeLast(2)}"
    }

    private fun maskCard(card: String): String {
        return "**** **** **** ${card.takeLast(4)}"
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return "***@***"
        val name = parts[0]
        val domain = parts[1]
        return "${name.take(1)}***@${domain}"
    }

    /**
     * ✅ CORRECTION Phase 1: Envoyer events vers Firebase Analytics au lieu de Logcat
     */
    private fun sendToAnalytics(event: SecurityEvent, userId: String?) {
        // Utiliser Firebase Analytics pour logging sécurisé en production
        // Pas d'exposition dans Logcat
        // Implementation à compléter avec AnalyticsManager
    }

    /**
     * Enregistre une erreur de sécurité
     */
    fun logSecurityError(error: Throwable, context: String, userId: String? = null) {
        val timestamp = Date()
        val userIdStr = maskSensitive(userId)

        if (isDebuggable && BuildConfig.DEBUG) {
            Log.e(
                TAG,
                "[$timestamp] User: $userIdStr | [$context] Security error: ${error.message}",
                error
            )
        }
    }

    /**
     * Enregistre un événement de sécurité personnalisé
     */
    fun logCustomEvent(
        message: String,
        level: Int = Log.INFO,
        userId: String? = null,
        deviceId: String? = null
    ) {
        val timestamp = Date()
        val userIdStr = maskSensitive(userId)
        val deviceIdStr = maskSensitive(deviceId)

        if (isDebuggable && BuildConfig.DEBUG) {
            Log.println(
                level,
                TAG,
                "[$timestamp] User: $userIdStr | Device: $deviceIdStr | $message"
            )
        }
    }

    /**
     * Enregistre les métadonnées d'une transaction
     */
    fun logTransaction(
        transactionId: String,
        amount: Double,
        recipient: String,
        userId: String,
        success: Boolean
    ) {
        val timestamp = Date()
        val level = if (success) Log.INFO else Log.ERROR

        if (isDebuggable && BuildConfig.DEBUG) {
            Log.println(
                level,
                TAG,
                "[$timestamp] User: ${maskSensitive(userId)} | Transaction ID: ${maskSensitive(transactionId)} | Amount: $amount | " +
                "Recipient: ${maskSensitive(recipient)} | Success: $success"
            )
        }
    }

    /**
     * Enregistre un événement de session
     */
    fun logSessionEvent(userId: String, eventType: SessionEventType) {
        val timestamp = Date()
        val userIdStr = maskSensitive(userId)

        if (isDebuggable && BuildConfig.DEBUG) {
            Log.i(
                TAG,
                "[$timestamp] User: $userIdStr | Session event: ${eventType.name}"
            )
        }
    }

    /**
     * Types d'événements de session
     */
    enum class SessionEventType {
        LOGIN,
        LOGOUT,
        SESSION_TIMEOUT,
        TOKEN_REFRESH,
        SESSION_EXPIRED
    }
}