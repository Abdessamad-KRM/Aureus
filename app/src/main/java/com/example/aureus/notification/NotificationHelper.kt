package com.example.aureus.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aureus.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper pour les notifications
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val CHANNEL_TRANSACTION_ID = "transactions"
        private const val CHANNEL_ALERT_ID = "alerts"
        private const val CHANNEL_TRANSFER_ID = "transfers"

        private const val NOTIFICATION_TRANSACTION = 1001
        private const val NOTIFICATION_BALANCE_ALERT = 1002
        private const val NOTIFICATION_TRANSFER = 1003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Créer les canaux de notification
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal transactions
            val transactionChannel = NotificationChannel(
                CHANNEL_TRANSACTION_ID,
                "Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les nouvelles transactions"
            }

            // Canal alerts
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes importantes (solde bas, sécurité)"
                enableVibration(true)
                enableLights(true)
            }

            // Canal transferts
            val transferChannel = NotificationChannel(
                CHANNEL_TRANSFER_ID,
                "Transfers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les transferts reçus/envoyés"
            }

            notificationManager.createNotificationChannel(transactionChannel)
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(transferChannel)
        }
    }

    /**
     * Vérifier les permissions de notification
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Notification de transaction
     */
    fun showTransactionNotification(title: String, amount: String, type: String) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
            return
        }

        val intent = Intent(context, com.example.aureus.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_TRANSACTION,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRANSACTION_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Amount: $amount MAD")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_TRANSACTION, builder.build())
        }
    }

    /**
     * Alert solde bas
     */
    fun showBalanceAlert(currentBalance: String, threshold: String) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
            return
        }

        val intent = Intent(context, com.example.aureus.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "dashboard")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_BALANCE_ALERT,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Low Balance Alert")
            .setContentText("Your balance ($currentBalance MAD) is below threshold ($threshold MAD)")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your balance is getting low. Consider adding funds to avoid transaction failures.")
            )

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_BALANCE_ALERT, builder.build())
        }
    }

    /**
     * Notification de transfert
     */
    fun showTransferNotification(amount: String, direction: String, fromTo: String) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
            return
        }

        val title = if (direction == "received") {
            "💰 Money Received!"
        } else {
            "💸 Money Sent"
        }

        val body = if (direction == "received") {
            "You received $amount MAD from $fromTo"
        } else {
            "You sent $amount MAD to $fromTo"
        }

        val intent = Intent(context, com.example.aureus.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "transactions")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_TRANSFER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRANSFER_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_TRANSFER, builder.build())
        }
    }

    /**
     * Enregistrer le FCM token dans Firestore
     */
    fun registerFcmToken(token: String) {
        val userId = auth.currentUser?.uid ?: return

        scope.launch {
            try {
                // Stocker le token dans users/{userId}/fcmTokens/{token}
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("fcmTokens")
                    .document(token)
                    .set(
                        mapOf(
                            "token" to token,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "deviceInfo" to getDeviceInfo()
                        )
                    )
            } catch (e: Exception) {
                android.util.Log.e("NotificationHelper", "Error registering FCM token", e)
            }
        }
    }

    /**
     * Obtenir les informations de l'appareil
     */
    private fun getDeviceInfo(): Map<String, Any> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "osVersion" to android.os.Build.VERSION.RELEASE,
            "sdkVersion" to android.os.Build.VERSION.SDK_INT
        )
    }

    /**
     * Nettoyer les anciens tokens FCM
     */
    fun cleanupOldTokens() {
        val userId = auth.currentUser?.uid ?: return

        scope.launch {
            try {
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("fcmTokens")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val cutoffDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days

                        snapshot.documents.forEach { doc ->
                            val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0
                            if (createdAt < cutoffDate) {
                                doc.reference.delete()
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("NotificationHelper", "Error cleaning up old tokens", e)
            }
        }
    }
}