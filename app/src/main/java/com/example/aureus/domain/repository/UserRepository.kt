package com.example.aureus.domain.repository

import com.example.aureus.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User data management
 * Handles profile operations with Firebase backend
 */
interface UserRepository {

    /**
     * Obtenir le profil utilisateur courant en temps réel
     * @return Flow<User> - État du profil en temps réel
     */
    fun getUserProfile(userId: String): Flow<User?>

    /**
     * Mettre à jour le profil utilisateur
     * @param userId ID de l'utilisateur
     * @param userData Données à mettre à jour
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun updateProfile(userId: String, userData: Map<String, Any>): Result<Unit>

    /**
     * Upload une image de profil
     * @param userId ID de l'utilisateur
     * @param imageUri URI de l'image locale
     * @return Result<String> - URL de l'image uploadée ou erreur
     */
    suspend fun uploadProfileImage(userId: String, imageUri: String): Result<String>

    /**
     * Mettre à jour les paramètres de notification
     * @param userId ID de l'utilisateur
     * @param settings Paramètres de notification
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun updateNotificationSettings(
        userId: String,
        settings: Map<String, Boolean>
    ): Result<Unit>

    /**
     * Mettre à jour les paramètres de sécurité
     * @param userId ID de l'utilisateur
     * @param settings Paramètres de sécurité
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun updateSecuritySettings(
        userId: String,
        settings: Map<String, Any>
    ): Result<Unit>

    /**
     * Changer le mot de passe utilisateur
     * @param newPassword Nouveau mot de passe
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun changePassword(newPassword: String): Result<Unit>

    /**
     * Envoyer email de réinitialisation du mot de passe
     * @param email Email de l'utilisateur
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun resetPassword(email: String): Result<Unit>

    /**
     * Supprimer le compte utilisateur
     * @param userId ID de l'utilisateur
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun deleteAccount(userId: String): Result<Unit>

    /**
     * Mettre à jour la langue préférée
     * @param userId ID de l'utilisateur
     * @param language Code de la langue (fr, en, ar, es, de)
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun updatePreferredLanguage(userId: String, language: String): Result<Unit>

    /**
     * Enregistrer le FCM token pour les notifications push
     * @param userId ID de l'utilisateur
     * @param token FCM token
     * @param deviceInfo Informations sur l'appareil (optionnel)
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun registerFcmToken(
        userId: String,
        token: String,
        deviceInfo: Map<String, Any>? = null
    ): Result<Unit>

    /**
     * Obtenir les FCM tokens de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Result<List<String>> - Liste des tokens ou erreur
     */
    suspend fun getUserFcmTokens(userId: String): Result<List<String>>

    /**
     * Supprimer un FCM token
     * @param userId ID de l'utilisateur
     * @param token FCM token à supprimer
     * @return Result<Unit> - Succès ou erreur
     */
    suspend fun removeFcmToken(userId: String, token: String): Result<Unit>
}