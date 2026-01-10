package com.example.aureus.data.sharedprefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences pour suivre l'état de l'authentification
 * Utilisé pour savoir si l'utilisateur a déjà lié son téléphone et configuré son PIN
 */
@Singleton
class AuthPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var hasPhoneLinked: Boolean
        get() = prefs.getBoolean("has_phone_linked", false)
        set(value) = prefs.edit().putBoolean("has_phone_linked", value).apply()

    var hasPinSetup: Boolean
        get() = prefs.getBoolean("has_pin_setup", false)
        set(value) = prefs.edit().putBoolean("has_pin_setup", value).apply()

    /**
     * Réinitialiser toutes les préférences (logout)
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}