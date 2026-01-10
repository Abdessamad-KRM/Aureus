package com.example.aureus.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.aureus.MainActivity
import com.example.aureus.R
import com.example.aureus.util.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service
 * Handles push notifications
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle notification data
        remoteMessage.data.let { data ->
            val type = data["type"] ?: "info"
            val title = data["title"] ?: "MyBank"
            val body = data["body"] ?: ""

            when (type) {
                "transaction" -> showNotification(
                    title,
                    body,
                    Constants.CHANNEL_TRANSACTION,
                    Constants.NOTIFICATION_ID_TRANSACTION
                )
                "low_balance" -> showNotification(
                    title,
                    body,
                    Constants.CHANNEL_ALERTS,
                    Constants.NOTIFICATION_ID_LOW_BALANCE
                )
                else -> showNotification(
                    title,
                    body,
                    Constants.CHANNEL_INFO,
                    Constants.NOTIFICATION_ID_INFO
                )
            }
        }

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            showNotification(
                notification.title ?: "MyBank",
                notification.body ?: "",
                Constants.CHANNEL_INFO,
                Constants.NOTIFICATION_ID_INFO
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server if needed
        sendTokenToServer(token)
    }

    private fun showNotification(title: String, body: String, channelId: String, notificationId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    Constants.CHANNEL_TRANSACTION,
                    "Transaction Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new transactions"
                },
                NotificationChannel(
                    Constants.CHANNEL_ALERTS,
                    "Alert Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Important alerts like low balance"
                },
                NotificationChannel(
                    Constants.CHANNEL_INFO,
                    "Info Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General information notifications"
                }
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Send token to your backend server
        // This will be used to send targeted push notifications
    }
}