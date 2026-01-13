package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Notification
import com.example.aureus.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Interface pour gérer les notifications
 */
interface NotificationRepository {

    // Obtenir toutes les notifications d'un utilisateur
    fun getUserNotifications(userId: String): Flow<List<Notification>>

    // Obtenir les notifications non lues
    fun getUnreadNotifications(userId: String): Flow<List<Notification>>

    // Marquer comme lu
    suspend fun markAsRead(notificationId: String): Result<Unit>

    // Marquer toutes comme lues
    suspend fun markAllAsRead(userId: String): Result<Unit>

    // Supprimer une notification
    suspend fun deleteNotification(notificationId: String): Result<Unit>

    // Obtenir les préférences
    fun getNotificationPreferences(userId: String): Flow<NotificationPreferences>

    // Mettre à jour les préférences
    suspend fun updateNotificationPreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Result<Unit>

    // Sauvegarder une notification reçue (pour l'historique)
    suspend fun saveNotification(notification: Notification): Result<Unit>

    // Obtenir le compteur de notifications non lues
    fun getUnreadCount(userId: String): Flow<Int>
}