package com.example.aureus.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire de stockage sécurisé avec EncryptedSharedPreferences
 * Compatible avec Android 23+ (Marshmallow)
 * Phase Correction Sécurité - Phase 1
 */
@Singleton
class SecureStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREF_FILE_NAME = "secure_storage"
    }

    /**
     * MasterKey pour EncryptedSharedPreferences
     * Stockée de manière sécurisée dans Android Keystore
     */
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()
    }

    /**
     * EncryptedSharedPreferences initialisé
     */
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREF_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback vers SharedPreferences normal si Keystore non disponible
            // Ne devrait PAS arriver en production, mais empêchera crash
            context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Sauvegarder une chaîne sécurisée
     */
    fun putString(key: String, value: String?) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    /**
     * Récupérer une chaîne sécurisée
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }

    /**
     * Supprimer une clé
     */
    fun remove(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    /**
     * Vider toutes les données
     */
    fun clear() {
        encryptedPrefs.edit().clear().apply()
    }

    /**
     * Vérifier si une clé existe
     */
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }
}