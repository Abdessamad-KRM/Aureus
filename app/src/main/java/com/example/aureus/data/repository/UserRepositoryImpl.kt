package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseAuthManager
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.NotificationSettings
import com.example.aureus.domain.model.SecuritySettings
import com.example.aureus.domain.model.User
import com.example.aureus.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for User data
 * Uses Firebase Auth, Firestore, and Storage for profile management
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val firebaseDataManager: FirebaseDataManager
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<User?> {
        return firebaseDataManager.getUser(userId).map { userData ->
            userData?.let { mapToUser(it) }
        }
    }

    override suspend fun updateProfile(userId: String, userData: Map<String, Any>): Result<Unit> {
        return firebaseDataManager.updateUser(userId, userData)
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: String): Result<String> {
        return firebaseDataManager.uploadProfileImage(userId, imageUri)
    }

    override suspend fun updateNotificationSettings(
        userId: String,
        settings: Map<String, Boolean>
    ): Result<Unit> {
        // Convert settings to nested map for Firestore
        val nestedSettings = mapOf(
            "notificationSettings" to settings,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        return firebaseDataManager.updateUser(userId, nestedSettings)
    }

    override suspend fun updateSecuritySettings(
        userId: String,
        settings: Map<String, Any>
    ): Result<Unit> {
        // Convert settings to nested map for Firestore
        val nestedSettings = mapOf(
            "securitySettings" to settings,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        return firebaseDataManager.updateUser(userId, nestedSettings)
    }

    override suspend fun changePassword(newPassword: String): Result<Unit> {
        return firebaseAuthManager.updatePassword(newPassword)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return firebaseAuthManager.resetPassword(email)
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> {
        return try {
            // Auth deletion
            if (firebaseAuthManager.currentUser?.uid == userId) {
                firebaseAuthManager.currentUser?.delete()
            }

            // Firestore document deletion will be handled by Firebase Auth triggers or manually
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreferredLanguage(userId: String, language: String): Result<Unit> {
        return firebaseDataManager.updateUser(userId, mapOf(
            "preferredLanguage" to language,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        ))
    }

    override suspend fun registerFcmToken(
        userId: String,
        token: String,
        deviceInfo: Map<String, Any>?
    ): Result<Unit> {
        return firebaseDataManager.registerFcmToken(userId, token, deviceInfo)
    }

    override suspend fun getUserFcmTokens(userId: String): Result<List<String>> {
        return firebaseDataManager.getUserFcmTokens(userId)
    }

    override suspend fun removeFcmToken(userId: String, token: String): Result<Unit> {
        return firebaseDataManager.removeFcmToken(userId, token)
    }

    /**
     * Convert Firestore Map to User domain model
     */
    private fun mapToUser(userData: Map<String, Any>): User {
        val timestampFormatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault())

        val notificationSettingsMap = (userData["notificationSettings"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val securitySettingsMap = (userData["securitySettings"] as? Map<*, *>) ?: emptyMap<String, Any>()

        return User(
            id = userData["userId"] as? String ?: userData["id"] as? String ?: "",
            email = userData["email"] as? String ?: "",
            firstName = userData["firstName"] as? String ?: "",
            lastName = userData["lastName"] as? String ?: "",
            phone = userData["phone"] as? String,
            createdAt = userData["createdAt"]?.toString() ?: "",
            updatedAt = userData["updatedAt"]?.toString() ?: "",
            profileImage = userData["profileImage"] as? String,
            address = userData["address"] as? String,
            city = userData["city"] as? String,
            country = userData["country"] as? String ?: "Morocco",
            preferredLanguage = userData["preferredLanguage"] as? String ?: "fr",
            notificationSettings = NotificationSettings(
                pushNotifications = notificationSettingsMap["pushNotifications"] as? Boolean ?: true,
                emailNotifications = notificationSettingsMap["emailNotifications"] as? Boolean ?: true,
                transactionAlerts = notificationSettingsMap["transactionAlerts"] as? Boolean ?: true,
                lowBalanceAlerts = notificationSettingsMap["lowBalanceAlerts"] as? Boolean ?: true,
                promotionalEmails = notificationSettingsMap["promotionalEmails"] as? Boolean ?: false
            ),
            securitySettings = SecuritySettings(
                biometricEnabled = securitySettingsMap["biometricEnabled"] as? Boolean ?: false,
                twoFactorAuth = securitySettingsMap["twoFactorAuth"] as? Boolean ?: false,
                pinCode = securitySettingsMap["pinCode"] as? String,
                sessionTimeout = (securitySettingsMap["sessionTimeout"] as? Number)?.toInt() ?: 30
            ),
            isEmailVerified = userData["isEmailVerified"] as? Boolean ?: false,
            isPhoneVerified = userData["isPhoneVerified"] as? Boolean ?: false
        )
    }
}