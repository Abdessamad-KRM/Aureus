package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Transaction Repository Interface - Firebase-based
 * Remplace l'implémentation Retrofit pour 100% dynamique avec Firestore
 */
interface TransactionRepositoryFirebase {

    // ==================== READ OPERATIONS ====================

    /**
     * Obtenir toutes les transactions d'un utilisateur en temps réel
     */
    fun getTransactions(userId: String, limit: Int = 50): Flow<List<Transaction>>

    /**
     * Obtenir les transactions récentes pour HomeScreen
     */
    fun getRecentTransactions(userId: String, limit: Int = 10): Flow<List<Transaction>>

    /**
     * Obtenir les transactions par compte
     */
    fun getTransactionsByAccount(userId: String, accountId: String, limit: Int = 50): Flow<List<Transaction>>

    /**
     * Obtenir les transactions filtrées par type (INCOME/EXPENSE)
     */
    fun getTransactionsByType(userId: String, type: String, limit: Int = 50): Flow<List<Transaction>>

    /**
     * Obtenir les transactions par catégorie
     */
    fun getTransactionsByCategory(userId: String, category: String, startDate: Date, endDate: Date): Flow<List<Transaction>>

    /**
     * Obtenir une transaction par son ID
     */
    suspend fun getTransactionById(transactionId: String): Resource<Transaction>

    // ==================== WRITE OPERATIONS ====================

    /**
     * Créer une nouvelle transaction
     */
    suspend fun createTransaction(transaction: Transaction): Resource<String>

    /**
     * Mettre à jour une transaction existante
     */
    suspend fun updateTransaction(transactionId: String, updates: Map<String, Any>): Resource<Unit>

    /**
     * Supprimer une transaction
     */
    suspend fun deleteTransaction(transactionId: String): Resource<Unit>

    // ==================== SEARCH & FILTER ====================

    /**
     * Rechercher des transactions par mot-clé (titre/merchant)
     */
    fun searchTransactions(userId: String, query: String, limit: Int = 20): Flow<List<Transaction>>

    /**
     * Obtenir les transactions dans une plage de dates
     */
    fun getTransactionsByDateRange(userId: String, startDate: Date, endDate: Date, limit: Int = 50): Flow<List<Transaction>>

    // ==================== AGGREGATION ====================

    /**
     * Obtenir le total des revenus pour une période
     */
    fun getTotalIncome(userId: String, startDate: Date, endDate: Date): Flow<Double>

    /**
     * Obtenir le total des dépenses pour une période
     */
    fun getTotalExpense(userId: String, startDate: Date, endDate: Date): Flow<Double>

    /**
     * Obtenir les dépenses groupées par catégorie
     */
    fun getCategoryExpenses(userId: String, startDate: Date, endDate: Date): Flow<Map<String, Double>>

    /**
     * Obtenir les statistiques mensuelles (revenus/dépenses par mois)
     */
    fun getMonthlyStatistics(userId: String, months: Int = 6): Flow<Map<String, Pair<Double, Double>>>
}