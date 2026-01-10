package com.example.aureus.util

import android.content.Context

/**
 * SharedPreferences Manager for lightweight data persistence
 */
class SharedPreferencesManager(context: Context) {

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
        sharedPrefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPrefs.getString(KEY_TOKEN, null)
    }

    fun saveRefreshToken(refreshToken: String) {
        sharedPrefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserId(userId: String) {
        sharedPrefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return sharedPrefs.getString(KEY_USER_ID, null)
    }

    fun saveUserEmail(email: String) {
        sharedPrefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPrefs.getString(KEY_USER_EMAIL, null)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveTheme(theme: String) {
        sharedPrefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getTheme(): String? {
        return sharedPrefs.getString(KEY_THEME, null)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun clearUserData() {
        sharedPrefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }.apply()
    }

    fun clearAll() {
        sharedPrefs.edit().clear().apply()
    }
}