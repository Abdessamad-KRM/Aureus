package com.example.aureus.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Suit les tentatives de PIN et gère le verrouillage
 */
@Singleton
class PinAttemptTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "PinSecurity",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ATTEMPT_COUNT = "attempt_count"
        private const val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
        private const val KEY_LOCKOUT_START = "lockout_start"
        private const val KEY_IS_LOCKED = "is_locked"

        private const val MAX_ATTEMPTS = 3
        private const val LOCKOUT_DURATION_MS = 5 * 60 * 1000 // 5 minutes
    }

    /**
     * Vérifie si le compte est verrouillé
     */
    fun isLocked(): Boolean {
        // Vérifier si le lockout a expiré
        if (prefs.getBoolean(KEY_IS_LOCKED, false)) {
            val lockoutStart = prefs.getLong(KEY_LOCKOUT_START, 0)
            val elapsed = System.currentTimeMillis() - lockoutStart

            if (elapsed >= LOCKOUT_DURATION_MS) {
                // Lockout expiré, réinitialiser
                resetAttempts()
                return false
            }
            return true
        }
        return false
    }

    /**
     * Retourne le temps restant en secondes
     */
    fun getLockoutTimeRemaining(): Int {
        val lockoutStart = prefs.getLong(KEY_LOCKOUT_START, 0)
        val elapsed = System.currentTimeMillis() - lockoutStart
        val remaining = LOCKOUT_DURATION_MS - elapsed
        return (remaining / 1000).coerceAtLeast(0).toInt()
    }

    /**
     * Enregistre une tentative échouée
     */
    fun recordFailedAttempt(): Int {
        val currentCount = prefs.getInt(KEY_ATTEMPT_COUNT, 0) + 1

        prefs.edit {
            putInt(KEY_ATTEMPT_COUNT, currentCount)
            putLong(KEY_LAST_ATTEMPT_TIME, System.currentTimeMillis())
        }

        // Vérifier si on atteint la limite
        if (currentCount >= MAX_ATTEMPTS) {
            lockAccount()
        }

        return currentCount
    }

    /**
     * Réinitialise les tentatives (appelé après PIN correct)
     */
    fun resetAttempts() {
        prefs.edit {
            remove(KEY_ATTEMPT_COUNT)
            remove(KEY_LAST_ATTEMPT_TIME)
            remove(KEY_LOCKOUT_START)
            putBoolean(KEY_IS_LOCKED, false)
        }
    }

    /**
     * Retourne le nombre de tentatives restantes
     */
    fun getAttemptsRemaining(): Int {
        return MAX_ATTEMPTS - prefs.getInt(KEY_ATTEMPT_COUNT, 0)
    }

    /**
     * Vérifie si on a atteint la limite
     */
    fun hasReachedLimit(): Boolean {
        return prefs.getInt(KEY_ATTEMPT_COUNT, 0) >= MAX_ATTEMPTS
    }

    /**
     * Retourne le nombre actuel de tentatives
     */
    fun getCurrentAttemptCount(): Int {
        return prefs.getInt(KEY_ATTEMPT_COUNT, 0)
    }

    /**
     * Verrouille le compte
     */
    private fun lockAccount() {
        prefs.edit {
            putBoolean(KEY_IS_LOCKED, true)
            putLong(KEY_LOCKOUT_START, System.currentTimeMillis())
        }
    }

    /**
     * Retourne l'heure de la dernière tentative
     */
    fun getLastAttemptTime(): Date {
        return Date(prefs.getLong(KEY_LAST_ATTEMPT_TIME, 0))
    }
}