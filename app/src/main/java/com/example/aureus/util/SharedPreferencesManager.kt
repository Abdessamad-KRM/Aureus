package com.example.aureus.util

import android.content.Context
import com.example.aureus.security.SecureStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences Manager - Version sécurisée avec EncryptedSharedPreferences
 * Phase Correction Sécurité - MIGRATION COMPLETE
 */
@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
    private val secureStorage: SecureStorageManager
) {

    companion object {
        private const val PREFS_NAME = "MyBankPrefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_THEME = "app_theme"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    fun saveToken(token: String) {
        secureStorage.putString(KEY_TOKEN, token)
    }

    fun getToken(): String? {
        return secureStorage.getString(KEY_TOKEN)
    }

    fun saveRefreshToken(refreshToken: String) {
        secureStorage.putString(KEY_REFRESH_TOKEN, refreshToken)
    }

    fun getRefreshToken(): String? {
        return secureStorage.getString(KEY_REFRESH_TOKEN)
    }

    fun saveUserId(userId: String) {
        secureStorage.putString(KEY_USER_ID, userId)
    }

    fun getUserId(): String? {
        return secureStorage.getString(KEY_USER_ID)
    }

    fun saveUserEmail(email: String) {
        secureStorage.putString(KEY_USER_EMAIL, email)
    }

    fun getUserEmail(): String? {
        return secureStorage.getString(KEY_USER_EMAIL)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        // Pour booléens, on stocke comme string dans secureStorage
        secureStorage.putString(KEY_IS_LOGGED_IN, isLoggedIn.toString())
    }

    fun isLoggedIn(): Boolean {
        val value = secureStorage.getString(KEY_IS_LOGGED_IN, "false")
        return value?.toBoolean() ?: false
    }

    fun saveTheme(theme: String) {
        secureStorage.putString(KEY_THEME, theme)
    }

    fun getTheme(): String? {
        return secureStorage.getString(KEY_THEME)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        secureStorage.putString(KEY_ONBOARDING_COMPLETED, completed.toString())
    }

    fun isOnboardingCompleted(): Boolean {
        val value = secureStorage.getString(KEY_ONBOARDING_COMPLETED, "false")
        return value?.toBoolean() ?: false
    }

    fun clearUserData() {
        secureStorage.remove(KEY_TOKEN)
        secureStorage.remove(KEY_REFRESH_TOKEN)
        secureStorage.remove(KEY_USER_ID)
        secureStorage.remove(KEY_USER_EMAIL)
        secureStorage.putString(KEY_IS_LOGGED_IN, "false")
    }

    fun clearAll() {
        secureStorage.clear()
    }

    /**
     * MIGRATION: Migrer les anciennes données SharedPreferences vers EncryptedSharedPreferences
     * Appeler UNE SEULE FOIS au démarrage de l'app
     */
    fun migrateFromLegacy(context: Context) {
        try {
            val legacyPrefs = context.getSharedPreferences("MyBankPrefs", Context.MODE_PRIVATE)
            val encryptedKeys = listOf(
                KEY_TOKEN, KEY_REFRESH_TOKEN, KEY_USER_ID, KEY_USER_EMAIL, KEY_THEME
            )

            encryptedKeys.forEach { key ->
                val value = legacyPrefs.getString(key, null)
                if (value != null && !secureStorage.contains(key)) {
                    secureStorage.putString(key, value)
                }
            }

            // Migrer booleans
            val isLoggedIn = legacyPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
            if (!secureStorage.contains(KEY_IS_LOGGED_IN)) {
                secureStorage.putString(KEY_IS_LOGGED_IN, isLoggedIn.toString())
            }

            val onboardingCompleted = legacyPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
            if (!secureStorage.contains(KEY_ONBOARDING_COMPLETED)) {
                secureStorage.putString(KEY_ONBOARDING_COMPLETED, onboardingCompleted.toString())
            }

            // ✅ Succès: Nettoyer anciennes données
            legacyPrefs.edit().clear().apply()
        } catch (e: Exception) {
            // Échec silencieux - les données restent en SharedPreferences normal
            // L'app continuera de fonctionner
        }
    }
}