package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Notification
import com.example.aureus.domain.model.NotificationPreferences
import com.example.aureus.domain.model.NotificationType
import com.example.aureus.domain.model.QuietHours
import com.example.aureus.domain.repository.NotificationRepository
import com.example.aureus.util.TimeoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper pour exécuter les opérations Firestore sur IO dispatcher avec timeouts
 */
private suspend fun <T> onFirestore(
    timeoutMs: Long = TimeoutManager.FIREBASE_READ_TIMEOUT,
    block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val result = TimeoutManager.withReadTimeout(timeoutMs) {
            block()
        }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Helper pour les opérations d'écriture Firestore avec timeout
 */
private suspend fun <T> onFirestoreWrite(
    timeoutMs: Long = TimeoutManager.FIREBASE_WRITE_TIMEOUT,
    block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val result = TimeoutManager.withWriteTimeout(timeoutMs) {
            block()
        }
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager
) : NotificationRepository {

    // Collection Firestore pour les notifications
    private val notificationsCollection = firebaseDataManager.firestore.collection("notifications")

    override fun getUserNotifications(userId: String): Flow<List<Notification>> {
        return callbackFlow {
            val listener = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50) // Limiter à 50 notifications
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        mapToNotification(doc.data.orEmpty())
                    } ?: emptyList()

                    trySend(notifications)
                }

            awaitClose {
                listener.remove()
            }
        }
    }

    override fun getUnreadNotifications(userId: String): Flow<List<Notification>> {
        return callbackFlow {
            val listener = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        mapToNotification(doc.data.orEmpty())
                    } ?: emptyList()

                    trySend(notifications)
                }

            awaitClose {
                listener.remove()
            }
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return onFirestoreWrite {
            notificationsCollection.document(notificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "readAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()
            Unit
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return onFirestoreWrite {
            val snapshots = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firebaseDataManager.firestore.batch()
            snapshots.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "isRead" to true,
                    "readAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
            }

            batch.commit().await()
            Unit
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return onFirestoreWrite {
            notificationsCollection.document(notificationId).delete().await()
            Unit
        }
    }

    override fun getNotificationPreferences(userId: String): Flow<NotificationPreferences> {
        return callbackFlow {
            val listener = firebaseDataManager.firestore.collection("users")
                .document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        trySend(NotificationPreferences())
                        return@addSnapshotListener
                    }

                    val prefsMap = document?.get("notificationPreferences") as? Map<*, *>
                    trySend(mapToNotificationPreferences(prefsMap ?: emptyMap<String, Any>()))
                }

            awaitClose {
                listener.remove()
            }
        }
    }

    override suspend fun updateNotificationPreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Result<Unit> {
        return onFirestoreWrite {
            firebaseDataManager.firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "notificationPreferences" to mapOf(
                            "pushEnabled" to preferences.pushEnabled,
                            "emailEnabled" to preferences.emailEnabled,
                            "transactionAlerts" to preferences.transactionAlerts,
                            "lowBalanceAlerts" to preferences.lowBalanceAlerts,
                            "transferAlerts" to preferences.transferAlerts,
                            "promotionalNotifications" to preferences.promotionalNotifications,
                            "quietHours" to preferences.quietHours?.let {
                                mapOf(
                                    "enabled" to it.enabled,
                                    "startTime" to it.startTime,
                                    "endTime" to it.endTime
                                )
                            }
                        ),
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()
            Unit
        }
    }

    override suspend fun saveNotification(notification: Notification): Result<Unit> {
        return onFirestoreWrite {
            notificationsCollection.document(notification.id)
                .set(
                    mapOf(
                        "id" to notification.id,
                        "userId" to notification.userId,
                        "title" to notification.title,
                        "body" to notification.body,
                        "type" to notification.type.name,
                        "data" to notification.data,
                        "isRead" to notification.isRead,
                        "timestamp" to notification.timestamp,
                        "imageUrl" to notification.imageUrl,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()
            Unit
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return callbackFlow {
            val listener = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    val count = if (error != null) {
                        0
                    } else {
                        snapshot?.documents?.size ?: 0
                    }
                    trySend(count)
                }

            awaitClose {
                listener.remove()
            }
        }
    }

    private fun mapToNotification(data: Map<String, Any>): Notification {
        val typeStr = data["type"] as? String ?: "INFO"
        val type = try {
            NotificationType.valueOf(typeStr)
        } catch (e: Exception) {
            NotificationType.INFO
        }

        val timestamp = when (val ts = data["timestamp"]) {
            is com.google.firebase.Timestamp -> ts.toDate()
            is java.util.Date -> ts
            else -> java.util.Date()
        }

        return Notification(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            body = data["body"] as? String ?: "",
            type = type,
            data = (data["data"] as? Map<String, String>) ?: emptyMap(),
            isRead = data["isRead"] as? Boolean ?: false,
            timestamp = timestamp,
            imageUrl = data["imageUrl"] as? String
        )
    }

    private fun mapToNotificationPreferences(data: Map<*, *>): NotificationPreferences {
        val quietHoursMap = data["quietHours"] as? Map<*, *>

        return NotificationPreferences(
            pushEnabled = data["pushEnabled"] as? Boolean ?: true,
            emailEnabled = data["emailEnabled"] as? Boolean ?: true,
            transactionAlerts = data["transactionAlerts"] as? Boolean ?: true,
            lowBalanceAlerts = data["lowBalanceAlerts"] as? Boolean ?: true,
            transferAlerts = data["transferAlerts"] as? Boolean ?: true,
            promotionalNotifications = data["promotionalNotifications"] as? Boolean ?: false,
            quietHours = quietHoursMap?.let {
                QuietHours(
                    enabled = it["enabled"] as? Boolean ?: false,
                    startTime = it["startTime"] as? String ?: "22:00",
                    endTime = it["endTime"] as? String ?: "08:00"
                )
            }
        )
    }
}