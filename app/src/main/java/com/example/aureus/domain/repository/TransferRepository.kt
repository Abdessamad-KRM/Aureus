package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Transfer Repository Interface - Gestion des transferts entre utilisateurs
 * ✅ PHASE 3: Repository pour transferts atomiques via Cloud Functions
 */
interface TransferRepository {

    // ==================== TRANSFERS ====================

    /**
     * Transférer de l'argent à un autre utilisateur
     * Utilise Cloud Function pour transaction atomique
     */
    suspend fun transferMoney(
        recipientUserId: String,
        amount: Double,
        description: String = ""
    ): Resource<TransferResult>

    /**
     * Créer une demande d'argent
     */
    suspend fun createMoneyRequest(
        recipientUserId: String,
        amount: Double,
        reason: String = ""
    ): Resource<String>

    /**
     * Valider si un userId existe et récupérer ses infos
     */
    suspend fun validateUserId(userId: String): Resource<UserInfo>

    // ==================== TRANSACTIONS DE TRANSFERT ====================

    /**
     * Obtenir les transferts sortants
     */
    fun getOutgoingTransfers(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Obtenir les transferts entrants
     */
    fun getIncomingTransfers(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Obtenir les demandes d'argent reçues
     */
    fun getMoneyRequestsReceived(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Obtenir les demandes d'argent envoyées
     */
    fun getMoneyRequestsSent(userId: String, limit: Int = 50): Flow<List<Map<String, Any>>>

    /**
     * Accepter une demande d'argent
     */
    suspend fun acceptMoneyRequest(requestId: String): Resource<String>

    /**
     * Refuser une demande d'argent
     */
    suspend fun rejectMoneyRequest(requestId: String): Resource<String>

    // ==================== LIMITES ET QUOTAS ====================

    /**
     * Vérifier les limites de transfert
     */
    suspend fun checkTransferLimits(userId: String): Resource<TransferLimits>
}

/**
 * Résultats de transfert
 */
data class TransferResult(
    val success: Boolean,
    val transactionId: String,
    val recipientTransactionId: String,
    val senderBalance: Double,
    val recipientBalance: Double,
    val amount: Double,
    val timestamp: String
)

/**
 * Infos utilisateur validé
 */
data class UserInfo(
    val exists: Boolean,
    val userId: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?
)

/**
 * Limites de transfert
 */
data class TransferLimits(
    val dailyLimit: Double,
    val dailyUsed: Double,
    val dailyRemaining: Double,
    val monthlyLimit: Double,
    val monthlyUsed: Double,
    val monthlyRemaining: Double,
    val maxTransferAmount: Double
)