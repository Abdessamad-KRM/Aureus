package com.example.aureus.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire sécurisé des identifiants - Version avec EncryptedSharedPreferences
 * Phase Correction Sécurité - MIGRATION COMPLETE
 *
 * SÉCURITÉ BANCAIRE:
 * - Stockage avec EncryptedSharedPreferences (Android Keystore)
 * - Auto-sauvegarde (comme suggestions Android)
 * - Maximum 3 comptes sauvegardés
 * - Mots de passe CHIFFRÉS au repos
 */
@Singleton
class SecureCredentialManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorageManager
) {

    companion object {
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts"
        private const val KEY_QUICK_LOGIN_ENABLED = "quick_login_enabled"
        private const val MAX_SAVED_ACCOUNTS = 3
    }

    /**
     * Vérifier si Quick Login est activé
     */
    fun isQuickLoginEnabled(): Boolean {
        val value = secureStorage.getString(KEY_QUICK_LOGIN_ENABLED, "false")
        return value?.toBoolean() ?: false
    }

    /**
     * Sauvegarde pour Login (email + password seulement)
     */
    suspend fun saveAccount(email: String, password: String): Result<Unit> {
        return saveRegisterCredentials(
            email = email,
            password = password,
            firstName = "",
            lastName = "",
            phone = ""
        )
    }

    /**
     * Sauvegarde complète pour Register (tous les champs)
     */
    suspend fun saveRegisterCredentials(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accounts = getSavedAccountsInternal()

                // Vérifier limite
                if (accounts.size >= MAX_SAVED_ACCOUNTS) {
                    val oldestAccount = accounts.minByOrNull { it.lastUsed }
                    if (oldestAccount != null) {
                        removeAccountInternal(oldestAccount.id)
                    }
                }

                // Générer clé pour mot de passe
                val passwordKey = "pwd_${System.currentTimeMillis()}_${UUID.randomUUID()}"
                // ✅ CHIFFRÉ dans EncryptedSharedPreferences!
                secureStorage.putString("pwd_$passwordKey", password)

                // Vérifier si compte existe déjà
                val existingIndex = accounts.indexOfFirst { it.email == email }
                if (existingIndex >= 0) {
                    // MAJ compte existant
                    val oldPasswordKey = accounts[existingIndex].passwordKey
                    if (oldPasswordKey != null) {
                        secureStorage.remove("pwd_$oldPasswordKey")
                    }
                    accounts[existingIndex] = accounts[existingIndex].copy(
                        firstName = firstName.takeIf { it.isNotBlank() } ?: accounts[existingIndex].firstName,
                        lastName = lastName.takeIf { it.isNotBlank() } ?: accounts[existingIndex].lastName,
                        phone = phone.takeIf { it.isNotBlank() } ?: accounts[existingIndex].phone,
                        lastUsed = System.currentTimeMillis(),
                        passwordKey = passwordKey
                    )
                } else {
                    // Nouveau compte
                    val newAccount = SecureAccount(
                        id = UUID.randomUUID().toString(),
                        email = email,
                        label = email.split("@").first().take(12),
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        lastUsed = System.currentTimeMillis(),
                        passwordKey = passwordKey
                    )
                    accounts.add(newAccount)
                }

                // Sauvegarder
                secureStorage.putString(KEY_SAVED_ACCOUNTS, accountsToJson(accounts))
                secureStorage.putString(KEY_QUICK_LOGIN_ENABLED, "true")

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Auto-fill pour LoginScreen (email + password)
     */
    suspend fun autoFill(): Result<CredentialPair> {
        return autoFillRegister().map { registerCredentials ->
            CredentialPair(
                email = registerCredentials.email,
                password = registerCredentials.password
            )
        }
    }

    /**
     * Auto-fill complet pour RegisterScreen (tous les champs)
     */
    suspend fun autoFillRegister(): Result<RegisterCredentials> {
        return withContext(Dispatchers.IO) {
            try {
                if (!isQuickLoginEnabled()) {
                    return@withContext Result.failure(SecurityException("Quick Login désactivé"))
                }

                val accounts = getSavedAccountsInternal()
                if (accounts.isEmpty()) {
                    return@withContext Result.failure(
                        java.util.NoSuchElementException("Aucun compte sauvegardé")
                    )
                }

                // Compte le plus récemment utilisé
                val account = accounts.maxByOrNull { it.lastUsed }
                    ?: return@withContext Result.failure(
                        java.util.NoSuchElementException("Compte non trouvé")
                    )

                // ✅ Récupérer mot de passe CHIFFRÉ
                val password = account.passwordKey?.let { key ->
                    secureStorage.getString("pwd_$key")
                }

                if (password == null) {
                    return@withContext Result.failure(
                        SecurityException("Mot de passe introuvable")
                    )
                }

                // Renvoyer TOUTES les informations
                Result.success(
                    RegisterCredentials(
                        email = account.email,
                        password = password,
                        firstName = account.firstName,
                        lastName = account.lastName,
                        phone = account.phone
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Récupérer les comptes sauvegardés (sans mot de passe)
     */
    suspend fun getSavedAccounts(): Result<List<QuickLoginAccount>> {
        return withContext(Dispatchers.IO) {
            try {
                val accounts = getSavedAccountsInternal()
                val displayAccounts = accounts.map { secureAccount ->
                    QuickLoginAccount(
                        id = secureAccount.id,
                        email = secureAccount.email,
                        maskedPassword = "****",
                        label = secureAccount.label,
                        lastUsed = secureAccount.lastUsed
                    )
                }
                Result.success(displayAccounts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Clear all accounts
     */
    suspend fun clearAll(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accounts = getSavedAccountsInternal()
                accounts.forEach { account ->
                    account.passwordKey?.let { key ->
                        secureStorage.remove("pwd_$key")
                    }
                }

                secureStorage.remove(KEY_SAVED_ACCOUNTS)
                secureStorage.putString(KEY_QUICK_LOGIN_ENABLED, "false")

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==================== PRIVATE METHODS ====================

    private fun getSavedAccountsInternal(): MutableList<SecureAccount> {
        val accountsJson = secureStorage.getString(KEY_SAVED_ACCOUNTS, null)
        return if (accountsJson != null) {
            try {
                jsonToAccounts(accountsJson)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }

    private fun removeAccountInternal(accountId: String) {
        val accounts = getSavedAccountsInternal()
        val accountToRemove = accounts.find { it.id == accountId }

        accountToRemove?.passwordKey?.let { key ->
            secureStorage.remove("pwd_$key")
        }

        val updatedAccounts = accounts.filter { it.id != accountId }
        secureStorage.putString(KEY_SAVED_ACCOUNTS, accountsToJson(updatedAccounts))

        if (updatedAccounts.isEmpty()) {
            secureStorage.putString(KEY_QUICK_LOGIN_ENABLED, "false")
        }
    }

    private fun accountsToJson(accounts: List<SecureAccount>): String {
        val jsonArray = JSONArray()
        for (account in accounts) {
            val json = JSONObject().apply {
                put("id", account.id)
                put("email", account.email)
                put("label", account.label)
                put("firstName", account.firstName)
                put("lastName", account.lastName)
                put("phone", account.phone)
                put("lastUsed", account.lastUsed)
                put("passwordKey", account.passwordKey)
            }
            jsonArray.put(json)
        }
        return jsonArray.toString()
    }

    private fun jsonToAccounts(json: String): MutableList<SecureAccount> {
        val accounts = mutableListOf<SecureAccount>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                accounts.add(
                    SecureAccount(
                        id = jsonObj.getString("id"),
                        email = jsonObj.getString("email"),
                        label = jsonObj.getString("label"),
                        firstName = jsonObj.optString("firstName", ""),
                        lastName = jsonObj.optString("lastName", ""),
                        phone = jsonObj.optString("phone", ""),
                        lastUsed = jsonObj.getLong("lastUsed"),
                        passwordKey = if (jsonObj.has("passwordKey") && !jsonObj.isNull("passwordKey")) {
                            jsonObj.getString("passwordKey")
                        } else null
                    )
                )
            }
        } catch (e: JSONException) {
        }
        return accounts
    }
}

// ==================== DATA CLASSES ====================

/**
 * Compte sécurisé (stockage interne complet)
 */
private data class SecureAccount(
    val id: String,
    val email: String,
    val label: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val lastUsed: Long,
    val passwordKey: String?
)

/**
 * Compte Quick Login pour UI (sans mot de passe en clair)
 */
data class QuickLoginAccount(
    val id: String,
    val email: String,
    val maskedPassword: String,
    val label: String,
    val lastUsed: Long
)

/**
 * Identifiants pour LoginScreen (email + password seulement)
 */
data class CredentialPair(
    val email: String,
    val password: String
)

/**
 * Identifiants complets pour RegisterScreen (tous les champs)
 */
data class RegisterCredentials(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String
)

/**
 * Exception de sécurité
 */
class SecurityException(message: String) : Exception(message)