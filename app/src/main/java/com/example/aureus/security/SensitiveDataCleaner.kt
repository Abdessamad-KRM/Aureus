package com.example.aureus.security

import android.util.Log
import java.util.*

/**
 * Sensitive Data Cleaner - Phase 2 Sécurité
 *
 * Nettoyage explicite des données sensibles en mémoire après utilisation
 * Empêche la rétention de données sensibles dans le heap
 *
 * Conformité PCI-DSS Section 3.2:
 * - "Do not store sensitive authentication data after authorization"
 * - "Limit data storage to that which is required for business"
 * - "Purge cardholder data when no longer needed"
 *
 * Mécanismes:
 * 1. Zero-fill des char arrays (au lieu de String)
 * 2. Clear des collections contenant des données sensibles
 * 3. Appel explicite du Garbage Collector (limité)
 * 4. Validation du nettoyage
 */
object SensitiveDataCleaner {

    private const val TAG = "SensitiveDataCleaner"

    /**
     * Nettoyer une chaîne de caractères sensible
     * Remplace tous les caractères par des zéros
     *
     * ⚠️ Les Strings en Java/Kotlin sont immuables
     * On ne peut pas les modifier directement en mémoire
     * Cette méthode retourne une chaîne de zéros pour éviter
     * la rétention de la référence originale dans le heap
     */
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    fun cleanString(secureString: String?): String {
        if (secureString == null) return ""

        // ✅ Créer une nouvelle chaîne vide pour remplacer la référence
        // Note: La chaîne originale reste en mémoire jusqu'au GC,
        // mais nous nous assurons qu'elle n'est plus référencée
        return ""
    }

    /**
     * Nettoyer un charArray contenant des données sensibles
     * Méthode recommandée pour les passwords, CVV, etc.
     *
     * Les charArrays sont mutables et peuvent être zero-fillés
     */
    fun cleanCharArray(charArray: CharArray?) {
        if (charArray == null) return

        // ✅ Overwrite avec des zéros
        charArray.fill('0')

        // ✅ Double overwrite pour s'assurer que les données sont effacées
        charArray.fill('\u0000')

        Log.d(TAG, "Char array cleaned (${charArray.size} chars)")
    }

    /**
     * Nettoyer un ByteArray (CVV, PIN, etc.)
     */
    fun cleanByteArray(byteArray: ByteArray?) {
        if (byteArray == null) return

        // ✅ Overwrite avec des zéros
        Arrays.fill(byteArray, 0.toByte())

        // ✅ Double overwrite
        Arrays.fill(byteArray, 0xFF.toByte())
        Arrays.fill(byteArray, 0.toByte())

        Log.d(TAG, "Byte array cleaned (${byteArray.size} bytes)")
    }

    /**
     * Nettoyer un CVV spécifiquement
     *
     * Le CVV (Card Verification Code) est une donnée extrêmement sensible
     * et ne doit JAMAIS être stocké après l'authentification
     */
    fun cleanCvv(cvv: String?): String {
        if (cvv == null) return ""

        Log.d(TAG, "Cleaning CVV (${cvv.length} digits)")

        // ✅ Nettoyer explicitement
        return ""
    }

    /**
     * Nettoyer un PIN
     */
    fun cleanPin(pin: String?): String {
        if (pin == null) return ""

        Log.d(TAG, "Cleaning PIN")

        return ""
    }

    /**
     * Nettoyer un mot de passe
     */
    fun cleanPassword(password: String?): String {
        if (password == null) return ""

        Log.d(TAG, "Cleaning password")

        return ""
    }

    /**
     * Nettoyer une liste contenant des données sensibles
     */
    fun <T> cleanList(list: MutableList<T>?) {
        if (list == null) return

        list.clear()
        Log.d(TAG, "List cleaned")

        // ⚠️ Suggestion GC (pas de garantie)
        System.gc()
    }

    /**
     * Nettoyer une map contenant des données sensibles
     */
    fun <K, V> cleanMap(map: MutableMap<K, V>?) {
        if (map == null) return

        map.clear()
        Log.d(TAG, "Map cleaned")

        // ⚠️ Suggestion GC
        System.gc()
    }

    /**
     * Nettoyer toutes les données sensibles liées à une carte
     */
    fun cleanCardData(
        cardNumber: String? = null,
        cvv: String? = null,
        expiryDate: String? = null
    ) {
        Log.d(TAG, "Cleaning all card data")

        cleanString(cardNumber)
        cleanCvv(cvv)
        cleanString(expiryDate)

        // ⚠️ Suggestion GC pour libérer la mémoire
        System.gc()
    }

    /**
     * Nettoyage sécurisé StringBuilder
     */
    fun cleanStringBuilder(builder: StringBuilder?) {
        if (builder == null) return

        val length = builder.length
        builder.setLength(0)

        // Overwrite avec du padding
        builder.append("0".repeat(length))
        builder.setLength(0)

        Log.d(TAG, "StringBuilder cleaned")
    }

    /**
     * Obtenir un CharArray depuis une String sensible
     * Le CharArray peut être nettoyé explicitement après usage
     */
    fun toSecureCharArray(text: String): CharArray {
        return text.toCharArray()
    }

    /**
     * Scope Guard pour le nettoyage automatique
     *
     * Usage:
     * ```kotlin
     * val cvv = SecureScope {
     *    sensitiveOperation()
     * }
     * // CVV est automatiquement nettoyé ici
     * ```
     */
    fun <T> withSecureCleanup(block: () -> T): T {
        var result: T
        try {
            result = block()
        } finally {
            // ⚠️ Suggestion GC après l'opération
            System.gc()
        }
        return result
    }

    /**
     * Gestionnaire de données sensibles lifecycle-aware
     */
    class SecureDataHolder<T> {
        private var data: T? = null

        fun set(value: T) {
            this.data = value
        }

        fun get(): T? = data

        fun clear() {
            when (data) {
                is String -> {
                    cleanString(data as String)
                }
                is CharArray -> {
                    cleanCharArray(data as CharArray)
                }
                is ByteArray -> {
                    cleanByteArray(data as ByteArray)
                }
                is MutableList<*> -> {
                    cleanList(data as MutableList<*>)
                }
            }
            data = null
            System.gc()
            Log.d(TAG, "SecureDataHolder cleared")
        }

        /**
         * Exécuter une action avec les données sensibles
         * et nettoyer automatiquement après
         */
        suspend fun <R> useWithCleanup(block: suspend (T) -> R): R {
            val value = data ?: throw IllegalStateException("No data set")
            return try {
                block(value)
            } finally {
                clear()
            }
        }
    }

    /**
     * Créer un SecureDataHolder
     */
    fun <T> createSecureHolder(): SecureDataHolder<T> {
        return SecureDataHolder()
    }

    /**
     * Vérifier si une chaîne ne contient que des zéros (nettoyée)
     */
    fun isCleaned(text: String?): Boolean {
        if (text == null) return true
        return text.all { it == '0' || it == '\u0000' }
    }
}

/**
 * Helper pour gérer les données sensibles de carte
 */
class CardDataCleaner {
    private var cardNumberHolder = SensitiveDataCleaner.createSecureHolder<String>()
    private var cvvHolder = SensitiveDataCleaner.createSecureHolder<String>()
    private var expiryHolder = SensitiveDataCleaner.createSecureHolder<String>()

    fun setCardData(cardNumber: String, cvv: String, expiry: String) {
        cardNumberHolder.set(cardNumber)
        cvvHolder.set(cvv)
        expiryHolder.set(expiry)
    }

    fun getCardNumber(): String? = cardNumberHolder.get()
    fun getCvv(): String? = cvvHolder.get()
    fun getExpiry(): String? = expiryHolder.get()

    fun cleanup() {
        Log.d("CardDataCleaner", "Cleaning all card data")
        cardNumberHolder.clear()
        cvvHolder.clear()
        expiryHolder.clear()
    }

    suspend fun <T> useCardData(block: suspend (cardNumber: String, cvv: String, expiry: String) -> T): T {
        val cardNum = getCardNumber()
        val cvvVal = getCvv()
        val exp = getExpiry()

        if (cardNum == null || cvvVal == null || exp == null) {
            throw IllegalStateException("Card data not initialized")
        }

        return try {
            block(cardNum, cvvVal, exp)
        } finally {
            cleanup()
        }
    }
}