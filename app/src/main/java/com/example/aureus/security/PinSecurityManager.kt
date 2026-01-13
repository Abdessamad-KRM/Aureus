package com.example.aureus.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère la sécurité liée au PIN
 * - Vérification du PIN
 * - Limitation des tentatives
 * - Hashage du PIN
 */
@Singleton
class PinSecurityManager @Inject constructor() {

    private val _pinAttempts = MutableStateFlow(0)
    val pinAttempts: StateFlow<Int> = _pinAttempts.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _lockoutEndTime = MutableStateFlow<Long?>(null)
    val lockoutEndTime: StateFlow<Long?> = _lockoutEndTime.asStateFlow()

    private val MAX_ATTEMPTS = 3
    private val LOCKOUT_DURATION_MS = 5 * 60 * 1000 // 5 minutes

    /**
     * Hash un PIN avec SHA-256 pour stockage sécurisé
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Vérifie si le PIN correspond au hash stocké
     */
    fun verifyPin(inputPin: String, storedHash: String): Boolean {
        if (_isLocked.value) {
            return false
        }

        val inputHash = hashPin(inputPin)
        return inputHash == storedHash
    }

    /**
     * Enregistre une tentative échouée
     */
    fun recordFailedAttempt() {
        _pinAttempts.value++

        if (_pinAttempts.value >= MAX_ATTEMPTS) {
            lockAccount()
        }
    }

    /**
     * Réinitialise le compteur de tentatives (PIN correct)
     */
    fun resetAttempts() {
        _pinAttempts.value = 0
        _isLocked.value = false
        _lockoutEndTime.value = null
    }

    /**
     * Verrouille le compte temporairement
     */
    private fun lockAccount() {
        _isLocked.value = true
        _lockoutEndTime.value = System.currentTimeMillis() + LOCKOUT_DURATION_MS
    }

    /**
     * Vérifie si le compte est verrouillé
     */
    fun isAccountLocked(): Boolean {
        if (!_isLocked.value) return false

        val endTime = _lockoutEndTime.value ?: return false
        if (System.currentTimeMillis() > endTime) {
            // Le verrouillage est expiré
            resetAttempts()
            return false
        }

        return true
    }

    /**
     * Retourne le temps restant de verrouillage en secondes
     */
    fun getLockoutTimeRemaining(): Int {
        val endTime = _lockoutEndTime.value ?: return 0
        val remaining = endTime - System.currentTimeMillis()
        return if (remaining > 0) (remaining / 1000).toInt() else 0
    }
}