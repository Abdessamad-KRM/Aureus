package com.example.aureus.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics Manager - Gère tous les events et metrics pour l'app Aureus Banking
 *
 * Tracke les événements utilisateur, erreurs, et performance pour:
 * - Firebase Analytics (User Behavior)
 * - Firebase Crashlytics (Crash Reporting)
 * - Firebase Performance Monitoring
 */
@Singleton
class AnalyticsManager @Inject constructor() {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics = Firebase.crashlytics

    // ==================== AUTH EVENTS ====================

    /**
     * Tracker l'inscription utilisateur
     */
    fun trackSignUp(method: String, userId: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SIGN_UP,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, method)
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker le login utilisateur
     */
    fun trackLogin(method: String, userId: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.LOGIN,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, method)
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker logout
     */
    fun trackLogout(userId: String) {
        firebaseAnalytics.logEvent(
            "logout",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker l'activation du mode hors ligne
     */
    fun trackOfflineModeEnabled(userId: String) {
        firebaseAnalytics.logEvent(
            "offline_mode_enabled",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    // ==================== TRANSACTION EVENTS ====================

    /**
     * Tracker création de transaction
     */
    fun trackTransactionCreated(
        userId: String,
        type: String,
        category: String,
        amount: Double,
        method: String
    ) {
        firebaseAnalytics.logEvent(
            "transaction_created",
            Bundle().apply {
                putString("user_id", userId)
                putString("type", type)
                putString("category", category)
                putDouble("amount", amount)
                putString("payment_method", method)
            }
        )
    }

    /**
     * Tracker échec transaction
     */
    fun trackTransactionFailed(userId: String, error: String) {
        firebaseAnalytics.logEvent(
            "transaction_failed",
            Bundle().apply {
                putString("user_id", userId)
                putString("error", error)
            }
        )
    }

    // ==================== TRANSFER EVENTS ====================

    /**
     * Tracker l'envoi d'argent
     */
    fun trackTransferSent(
        userId: String,
        amount: Double,
        recipient: String,
        method: String
    ) {
        firebaseAnalytics.logEvent(
            "transfer_sent",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("amount", amount)
                putString("recipient", recipient)
                putString("method", method)
            }
        )
    }

    /**
     * Tracker réception d'argent
     */
    fun trackTransferReceived(
        userId: String,
        amount: Double,
        sender: String
    ) {
        firebaseAnalytics.logEvent(
            "transfer_received",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("amount", amount)
                putString("sender", sender)
            }
        )
    }

    /**
     * Tracker demande d'argent envoyée
     */
    fun trackTransferRequested(
        userId: String,
        amount: Double,
        contact: String
    ) {
        firebaseAnalytics.logEvent(
            "transfer_requested",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("amount", amount)
                putString("contact", contact)
            }
        )
    }

    // ==================== CARD EVENTS ====================

    /**
     * Tracker ajout de carte
     */
    fun trackCardAdded(userId: String, cardType: String) {
        firebaseAnalytics.logEvent(
            "card_added",
            Bundle().apply {
                putString("user_id", userId)
                putString("card_type", cardType)
            }
        )
    }

    /**
     * Tracker blocage de carte
     */
    fun trackCardBlocked(userId: String, cardId: String, reason: String) {
        firebaseAnalytics.logEvent(
            "card_blocked",
            Bundle().apply {
                putString("user_id", userId)
                putString("card_id", cardId)
                putString("reason", reason)
            }
        )
    }

    /**
     * Tracker déblocage de carte
     */
    fun trackCardUnblocked(userId: String, cardId: String) {
        firebaseAnalytics.logEvent(
            "card_unblocked",
            Bundle().apply {
                putString("user_id", userId)
                putString("card_id", cardId)
            }
        )
    }

    /**
     * Tracker visualisation de détail carte
     */
    fun trackCardViewed(userId: String, cardId: String) {
        firebaseAnalytics.logEvent(
            "card_viewed",
            Bundle().apply {
                putString("user_id", userId)
                putString("card_id", cardId)
            }
        )
    }

    // ==================== CONTACT EVENTS ====================

    /**
     * Tracker ajout de contact
     */
    fun trackContactAdded(userId: String) {
        firebaseAnalytics.logEvent(
            "contact_added",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker suppression de contact
     */
    fun trackContactRemoved(userId: String) {
        firebaseAnalytics.logEvent(
            "contact_removed",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    // ==================== BIOMETRIC EVENTS ====================

    /**
     * Tracker utilisation biométrie
     */
    fun trackBiometricUsed(userId: String, success: Boolean) {
        firebaseAnalytics.logEvent(
            "biometric_auth",
            Bundle().apply {
                putString("user_id", userId)
                putString("success", success.toString())
            }
        )
    }

    /**
     * Tracker activation du verrouillage biométrique
     */
    fun trackBiometricEnabled(userId: String) {
        firebaseAnalytics.logEvent(
            "biometric_enabled",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    /**
     * Tracker désactivation du verrouillage biométrique
     */
    fun trackBiometricDisabled(userId: String) {
        firebaseAnalytics.logEvent(
            "biometric_disabled",
            Bundle().apply {
                putString("user_id", userId)
            }
        )
    }

    // ==================== SCREEN VIEW EVENTS ====================

    /**
     * Tracker vue d'écran
     */
    fun trackScreenView(screenName: String) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString("screen_class", screenName)
            }
        )
    }

    // ==================== ERROR TRACKING ====================

    /**
     * Logger une erreur (non-fatal)
     */
    fun trackError(
        tag: String,
        message: String,
        userId: String?
    ) {
        crashlytics.apply {
            setCustomKey("tag", tag)
            setCustomKey("user_id", userId ?: "guest")
            recordException(Exception(message))
        }
    }

    /**
     * Logger une exception avec contexte
     */
    fun trackException(exception: Exception, userId: String?, context: Map<String, String> = emptyMap()) {
        crashlytics.apply {
            userId?.let { setUserId(it) }
            context.forEach { (key, value) ->
                setCustomKey(key, value)
            }
            recordException(exception)
        }
    }

    /**
     * Logger une erreur de base de données
     */
    fun trackDatabaseError(error: String, operation: String, userId: String?) {
        crashlytics.apply {
            setCustomKey("database_operation", operation)
            setCustomKey("user_id", userId ?: "guest")
            setCustomKey("error_type", "database_error")
            recordException(Exception("Database Error: $error"))
        }
    }

    /**
     * Logger une erreur réseau
     */
    fun trackNetworkError(error: String, endpoint: String, userId: String?) {
        crashlytics.apply {
            setCustomKey("endpoint", endpoint)
            setCustomKey("user_id", userId ?: "guest")
            setCustomKey("error_type", "network_error")
            recordException(Exception("Network Error: $error"))
        }
    }

    // ==================== PERFORMANCE TRACKING ====================

    /**
     * Démarrer un trace pour performance
     */
    fun startTrace(traceName: String): Trace {
        return FirebasePerformance.getInstance().newTrace(traceName).apply {
            start()
        }
    }

    /**
     * Arrêter un trace
     */
    fun stopTrace(trace: Trace) {
        trace.stop()
    }

    /**
     * Ajouter un attribut à un trace
     */
    fun putTraceAttribute(trace: Trace, key: String, value: String) {
        trace.putAttribute(key, value)
    }

    /**
     * Ajouter un métrique à un trace
     */
    fun putTraceMetric(trace: Trace, metricName: String, value: Long) {
        trace.putMetric(metricName, value)
    }

    /**
     * Tracker une opération avec trace automatique
     */
    inline fun <T> trackOperation(
        operation: String,
        userId: String?,
        block: () -> T
    ): T {
        val trace = FirebasePerformance.getInstance().newTrace(operation).apply {
            start()
            if (userId != null) {
                putAttribute("user_id", userId)
            }
        }
        
        return try {
            val result = block()
            trace.stop()
            result
        } catch (e: Exception) {
            trace.putAttribute("error", e.message ?: "Unknown error")
            trace.stop()
            throw e
        }
    }

    // ==================== CUSTOM EVENTS ====================

    /**
     * Tracker balance check
     */
    fun trackBalanceCheck(userId: String, balance: Double, source: String) {
        firebaseAnalytics.logEvent(
            "balance_check",
            Bundle().apply {
                putString("user_id", userId)
                putDouble("balance", balance)
                putString("source", source)
            }
        )
    }

    /**
     * Tracker visualisation des statistiques
     */
    fun trackStatisticsViewed(userId: String, timeRange: String) {
        firebaseAnalytics.logEvent(
            "statistics_viewed",
            Bundle().apply {
                putString("user_id", userId)
                putString("time_range", timeRange)
            }
        )
    }

    /**
     * Tracker setting change
     */
    fun trackSettingChanged(userId: String, setting: String, oldValue: String, newValue: String) {
        firebaseAnalytics.logEvent(
            "setting_changed",
            Bundle().apply {
                putString("user_id", userId)
                putString("setting", setting)
                putString("old_value", oldValue)
                putString("new_value", newValue)
            }
        )
    }

    /**
     * Tracker notification opened
     */
    fun trackNotificationOpened(userId: String, notificationType: String) {
        firebaseAnalytics.logEvent(
            "notification_opened",
            Bundle().apply {
                putString("user_id", userId)
                putString("notification_type", notificationType)
            }
        )
    }

    /**
     * Tracker notification dismissed
     */
    fun trackNotificationDismissed(userId: String, notificationType: String) {
        firebaseAnalytics.logEvent(
            "notification_dismissed",
            Bundle().apply {
                putString("user_id", userId)
                putString("notification_type", notificationType)
            }
        )
    }

    /**
     * Tracker app open via notification
     */
    fun trackAppOpenedViaNotification(userId: String, notificationType: String) {
        firebaseAnalytics.logEvent(
            "app_opened_via_notification",
            Bundle().apply {
                putString("user_id", userId)
                putString("notification_type", notificationType)
            }
        )
    }

    // ==================== USER PROPERTIES ====================

    /**
     * Définir User ID pour analytics
     */
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }

    /**
     * Définir des User Properties personnalisés
     */
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Initialiser les User Properties d'un utilisateur
     */
    fun setUserProperties(
        userId: String,
        accountType: String,
        country: String,
        preferredLanguage: String
    ) {
        setUserId(userId)
        setUserProperty("account_type", accountType)
        setUserProperty("country", country)
        setUserProperty("preferred_language", preferredLanguage)
    }

    /**
     * Effacer les données utilisateur (logout)
     */
    fun clearUserData() {
        firebaseAnalytics.resetAnalyticsData()
        // Note: Crashlytics userId persists until app restart
        // but set a temporary value
        crashlytics.setUserId("guest")
    }
}