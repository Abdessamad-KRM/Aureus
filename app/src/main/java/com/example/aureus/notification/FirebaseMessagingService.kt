package com.example.aureus.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aureus.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Messaging Service
 * Gère les notifications push reçues
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "aureus_notifications"
        private const val CHANNEL_NAME = "Aureus Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for Aureus Banking"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        // ✅ SAUVEGARDER la notification dans Firestore pour l'historique
        saveNotificationToFirestore(remoteMessage)

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "Aureus Banking",
                body = it.body ?: "New notification",
                data = remoteMessage.data
            )
        }

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM Token: $token")
        sendTokenToServer(token)
    }

    /**
     * Envoyer la notification
     * Phase 4: Implémente des deep links pour navigation depuis notifications
     */
    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        // Phase 4: Extraire la destination pour le deep link
        val destination = data["destination"] ?: "home"
        val deepLink = when (destination) {
            "transactions" -> "aureus://transactions"
            "dashboard" -> "aureus://home"
            "cards" -> "aureus://cards"
            "notifications" -> "aureus://notifications"
            "profile" -> "aureus://profile"
            else -> "aureus://home"
        }

        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(deepLink)).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtras(Bundle().apply {
                data.forEach { (key, value) ->
                    putString(key, value)
                }
            })
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        createNotificationChannel()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Phase 4: Utiliser la nouvelle icône de notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, R.drawable.ic_notification_large))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )

        // Check notification permission before showing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(this)) {
                    notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
                }
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted")
            }
        } else {
            with(NotificationManagerCompat.from(this)) {
                notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
            }
        }
    }

    /**
     * Gérer message de type data
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]

        when (type) {
            "transaction" -> {
                // Nouvelle transaction
                showTransactionNotification(
                    title = data["title"] ?: "New Transaction",
                    amount = data["amount"] ?: "0",
                    type = data["transaction_type"] ?: "EXPENSE"
                )
            }
            "balance_alert" -> {
                // Alert solde bas
                showBalanceAlert(
                    currentBalance = data["balance"] ?: "0",
                    threshold = data["threshold"] ?: "1000"
                )
            }
            "transfer" -> {
                // Argent reçu/envoyé
                showTransferNotification(
                    amount = data["amount"] ?: "0",
                    direction = data["direction"] ?: "received",
                    fromTo = data["from_to"] ?: "Someone"
                )
            }
        }
    }

    private fun showTransactionNotification(title: String, amount: String, type: String) {
        // Phase 4: Deep link vers la page des transactions
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("aureus://transactions")).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("amount", amount)
            putExtra("type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Phase 4: Utiliser la nouvelle icône de notification
        val builder = NotificationCompat.Builder(this, "transactions")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, R.drawable.ic_notification_large))
            .setContentTitle(title)
            .setContentText("Amount: $amount MAD")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        showNotificationWithPermissionCheck(1001, builder.build())
    }

    private fun showBalanceAlert(currentBalance: String, threshold: String) {
        // Phase 4: Deep link vers le dashboard
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("aureus://home")).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "dashboard")
            putExtra("balance", currentBalance)
            putExtra("threshold", threshold)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1002,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Phase 4: Utiliser la nouvelle icône de notification
        val builder = NotificationCompat.Builder(this, "alerts")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, R.drawable.ic_notification_large))
            .setContentTitle("⚠️ Low Balance Alert")
            .setContentText("Your balance ($currentBalance MAD) is below threshold ($threshold MAD)")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your balance is getting low. Consider adding funds to avoid transaction failures.")
            )

        showNotificationWithPermissionCheck(1002, builder.build())
    }

    private fun showTransferNotification(amount: String, direction: String, fromTo: String) {
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

        // Phase 4: Deep link vers la page des transactions
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("aureus://transactions")).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "transactions")
            putExtra("amount", amount)
            putExtra("direction", direction)
            putExtra("fromTo", fromTo)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1003,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Phase 4: Utiliser la nouvelle icône de notification
        val builder = NotificationCompat.Builder(this, "transfers")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, R.drawable.ic_notification_large))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        showNotificationWithPermissionCheck(1003, builder.build())
    }

    private fun showNotificationWithPermissionCheck(notificationId: Int, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(this)) {
                    notify(notificationId, notification)
                }
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted")
            }
        } else {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, notification)
            }
        }
    }

    /**
     * Envoyer le token au serveur
     */
    private fun sendTokenToServer(token: String) {
        Log.d(TAG, "Sending FCM token to server: $token")
        // NotificationHelper will handle the token registration
        // The NotificationHelper will be injected and handle this when it's initialized
    }

    /**
     * ✅ NOUVEAU: Sauvegarder notification dans Firestore
     */
    private fun saveNotificationToFirestore(remoteMessage: RemoteMessage) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val notificationId = "notif_${System.currentTimeMillis()}"

        val notificationType = when {
            remoteMessage.data["type"] == "transaction" -> "TRANSACTION"
            remoteMessage.data["type"] == "transfer" -> {
                if (remoteMessage.data["direction"] == "received") {
                    "TRANSFER_RECEIVED"
                } else {
                    "TRANSFER_SENT"
                }
            }
            remoteMessage.data["type"] == "balance_alert" -> "BALANCE_ALERT"
            else -> "INFO"
        }

        val notificationData = mapOf(
            "id" to notificationId,
            "userId" to userId,
            "title" to (remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Aureus"),
            "body" to (remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""),
            "type" to notificationType,
            "data" to remoteMessage.data,
            "isRead" to false,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "imageUrl" to remoteMessage.data["imageUrl"],
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        Firebase.firestore.collection("notifications")
            .document(notificationId)
            .set(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification saved to Firestore: $notificationId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save notification to Firestore", e)
            }
    }

    /**
     * Créer le canal de notification (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            // Also create sub-channels
            val transactionChannel = NotificationChannel(
                "transactions",
                "Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les nouvelles transactions"
            }

            val alertChannel = NotificationChannel(
                "alerts",
                "Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes importantes (solde bas, sécurité)"
                enableVibration(true)
                enableLights(true)
            }

            val transferChannel = NotificationChannel(
                "transfers",
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
}