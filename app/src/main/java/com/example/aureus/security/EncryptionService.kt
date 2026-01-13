package com.example.aureus.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import java.security.MessageDigest

/**
 * Service de chiffrement pour données sensibles
 * Utilise Android Keystore pour stocker les clés
 */
@Singleton
class EncryptionService @Inject constructor() {

    private val KEY_ALIAS = "AureusMasterKey"
    private val KEYSTORE = "AndroidKeyStore"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val IV_LENGTH = 12

    private val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    init {
        generateKeyIfNotExists()
    }

    /**
     * Génère une clé de chiffrement si n'existe pas
     */
    private fun generateKeyIfNotExists() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false) // Pas d'auth pour chiffrement
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Chiffre les données
     */
    fun encrypt(data: String): EncryptionResult {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getPrivateKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            return EncryptionResult(
                encryptedData = Base64.getEncoder().encodeToString(encryptedBytes),
                iv = Base64.getEncoder().encodeToString(iv)
            )
        } catch (e: Exception) {
            throw EncryptionException("Failed to encrypt data: ${e.message}")
        }
    }

    /**
     * Déchiffre les données
     */
    fun decrypt(encryptedData: String, iv: String): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getPrivateKey()

            val ivBytes = Base64.getDecoder().decode(iv)
            val encryptedBytes = Base64.getDecoder().decode(encryptedData)

            val ivSpec = IvParameterSpec(ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decrypt data: ${e.message}")
        }
    }

    /**
     * Hash un PIN avec SHA-256 (sécurisé, pas réversible)
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hash un numéro de carte (MASKED: ne conserver que les 4 derniers chiffres)
     */
    fun maskCardNumber(cardNumber: String): String {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        return if (digitsOnly.length >= 4) {
            "**** **** **** " + digitsOnly.takeLast(4)
        } else {
            "**** **** **** ****"
        }
    }

    /**
     * Tokeniser un numéro de carte (pour stockage sécurisé)
     */
    fun tokenizeCardNumber(cardNumber: String): String {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        // Dans une vraie implémentation, utiliser un service de tokenisation comme Stripe
        // Pour l'instant, retourner le numéro masqué
        return maskCardNumber(cardNumber)
    }

    private fun getPrivateKey(): SecretKey {
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            ?: throw IllegalStateException("Key not found in keystore")
        return entry.secretKey
    }
}

/**
 * Résultat du chiffrement
 */
data class EncryptionResult(
    val encryptedData: String,
    val iv: String
)

/**
 * Exception de chiffrement
 */
class EncryptionException(message: String) : Exception(message)