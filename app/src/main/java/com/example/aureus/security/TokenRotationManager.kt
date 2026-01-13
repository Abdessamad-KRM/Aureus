package com.example.aureus.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GetTokenResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token Rotation Manager - Phase 2 Sécurité
 *
 * Gère la rotation automatique des tokens Firebase Auth:
 * - Refresh automatique avant expiration
 * - Mise en cache des tokens
 * - Validation de token expiré
 * - Retry automatique en cas d'erreur
 *
 * Conformité OWASP:
 * - JWT tokens doivent être courts (15-30 min)
 * - Refresh tokens doivent être limités en durée
 * - Rotation régulière des tokens
 */
@Singleton
class TokenRotationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "TokenRotationManager"

        // ⏱️ Durées de token (en millisecondes)
        private const val TOKEN_REFRESH_THRESHOLD_MS = 5 * 60 * 1000L // 5 min avant expiration
        private const val MAX_TOKEN_AGE_MS = 30 * 60 * 1000L           // 30 min max

        // Clés de stockage
        private const val PREF_NAME = "token_rotation"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_ISSUED_AT = "token_issued_at"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    }

    /**
     * SharedPreferences pour stocker les tokens
     * Note: Tokens sont stockés dans SharedPreferences normaux car ils expirent rapidement (30 min)
     * et le risque de compromission d'un token JWT à courte durée de vie est limité
     */
    private val tokenPrefs: SharedPreferences by lazy {
        // Utiliser un préfixe pour différencier des autres SharedPreferences
        context.getSharedPreferences("${context.packageName}_tokens", Context.MODE_PRIVATE)
    }

    /**
     * Obtenir un access token valide
     * - Retourne le token en cache si valide
     * - Refresh automatiquement si proche de l'expiration
     * - Retourne null si erreur
     */
    suspend fun getValidAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            // Vérifier si l'utilisateur est connecté
            val currentUser = auth.currentUser ?: return@withContext null

            // Obtenir le token en cache
            val cachedToken = getCachedAccessToken()
            val issuedAt = getCachedTokenIssuedAt()

            if (cachedToken != null && issuedAt > 0) {
                val tokenAge = System.currentTimeMillis() - issuedAt

                // ✅ Token encore valide (pas proche de l'expiration)
                if (tokenAge < TOKEN_REFRESH_THRESHOLD_MS) {
                    Log.d(TAG, "Using cached token (age: ${tokenAge / 1000}s)")
                    return@withContext cachedToken
                }

                // ✅ Token proche de l'expiration => Refresh automatique
                Log.d(TAG, "Token expiring soon, refreshing (age: ${tokenAge / 1000}s)")
                return@withContext refreshAccessToken(currentUser)
            }

            // ❌ Pas de token en cache => Nouveau token
            Log.d(TAG, "No cached token, fetching new token")
            return@withContext refreshAccessToken(currentUser)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting valid access token", e)
            // Retry une fois
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    return@withContext refreshAccessToken(currentUser)
                }
            } catch (retryException: Exception) {
                Log.e(TAG, "Retry failed", retryException)
            }
            return@withContext null
        }
    }

    /**
     * Refresh l'access token depuis Firebase
     */
    private suspend fun refreshAccessToken(user: com.google.firebase.auth.FirebaseUser): String? {
        return try {
            val tokenResult = user.getIdToken(false) as GetTokenResult
            val newToken = tokenResult.token

            if (newToken == null) {
                return null
            }

            val issuedAt = System.currentTimeMillis()
            val expiresAt = issuedAt + (tokenResult.expirationTimestamp * 1000)

            // ✅ Sauvegarder le nouveau token
            cacheAccessToken(newToken, issuedAt, expiresAt)

            Log.d(TAG, "Access token refreshed successfully")
            newToken
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing access token", e)
            null
        }
    }

    /**
     * Forcer le refresh immédiat du token
     */
    suspend fun forceRefreshToken(): String? = withContext(Dispatchers.IO) {
        try {
            clearCachedTokens()
            val currentUser = auth.currentUser ?: return@withContext null
            refreshAccessToken(currentUser)
        } catch (e: Exception) {
            Log.e(TAG, "Error force refreshing token", e)
            null
        }
    }

    /**
     * Vérifier si le token est valide
     */
    fun isTokenValid(): Boolean {
        val issuedAt = getCachedTokenIssuedAt()
        if (issuedAt <= 0) return false

        val tokenAge = System.currentTimeMillis() - issuedAt
        return tokenAge < MAX_TOKEN_AGE_MS
    }

    /**
     * Obtenir le token en cache
     */
    private fun getCachedAccessToken(): String? {
        return tokenPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Obtenir le timestamp d'émission du token
     */
    private fun getCachedTokenIssuedAt(): Long {
        return tokenPrefs.getLong(KEY_TOKEN_ISSUED_AT, 0L)
    }

    /**
     * Obtenir le timestamp d'expiration du token
     */
    private fun getCachedTokenExpiresAt(): Long {
        return tokenPrefs.getLong(KEY_TOKEN_EXPIRES_AT, 0L)
    }

    /**
     * Cache le token
     */
    private fun cacheAccessToken(token: String, issuedAt: Long, expiresAt: Long) {
        tokenPrefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putLong(KEY_TOKEN_ISSUED_AT, issuedAt)
            putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            commit()
        }
    }

    /**
     * Nettoyer les tokens en cache (logout)
     */
    fun clearCachedTokens() {
        Log.d(TAG, "Clearing cached tokens")
        tokenPrefs.edit().clear().commit()
    }

    /**
     * Obtenir les informations sur le token
     */
    fun getTokenInfo(): TokenInfo {
        val issuedAt = getCachedTokenIssuedAt()
        val expiresAt = getCachedTokenExpiresAt()

        if (issuedAt == 0L) {
            return TokenInfo.NONE
        }

        val ageMs = System.currentTimeMillis() - issuedAt
        val remainingMs = expiresAt - System.currentTimeMillis()
        val isExpiringSoon = ageMs >= TOKEN_REFRESH_THRESHOLD_MS
        val isExpired = ageMs >= MAX_TOKEN_AGE_MS

        return TokenInfo(
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            ageMinutes = TimeUnit.MILLISECONDS.toMinutes(ageMs),
            remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs),
            isExpiringSoon = isExpiringSoon,
            isExpired = isExpired
        )
    }

    /**
     * Gérer les erreurs d'authentification
     * Refresh automatiquement le token si erreur de token expiré
     */
    suspend fun handleAuthError(error: Throwable): TokenRefreshResult {
        if (error is FirebaseAuthException) {
            when (error.errorCode) {
                "ERROR_ID_TOKEN_EXPIRED",
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_USER_TOKEN_EXPIRED" -> {
                    Log.d(TAG, "Token expired, attempting refresh")
                    val newToken = forceRefreshToken()
                    if (newToken != null) {
                        return TokenRefreshResult.SUCCESS(newToken ?: "")
                    }
                }
            }
        }
        return TokenRefreshResult.FAILED(error.message ?: "Unknown error")
    }
}

/**
 * Informations sur le token
 */
data class TokenInfo(
    val issuedAt: Long,
    val expiresAt: Long,
    val ageMinutes: Long,
    val remainingMinutes: Long,
    val isExpiringSoon: Boolean,
    val isExpired: Boolean
) {
    companion object {
        val NONE = TokenInfo(0, 0, 0, 0, false, true)
    }
}

/**
 * Résultat du refresh de token
 */
sealed class TokenRefreshResult {
    data class SUCCESS(val token: String) : TokenRefreshResult()
    data class FAILED(val error: String) : TokenRefreshResult()
}