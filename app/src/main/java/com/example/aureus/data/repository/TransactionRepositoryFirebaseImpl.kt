package com.example.aureus.data.repository

import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.Transaction
import com.example.aureus.domain.model.TransactionType
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transaction Repository Implementation - Firebase-based
 * Utilise FirebaseDataManager pour toutes les opérations Firestore
 */
@Singleton
class TransactionRepositoryFirebaseImpl @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager
) : TransactionRepositoryFirebase {

    // ==================== READ OPERATIONS ====================

    override fun getTransactions(userId: String, limit: Int): Flow<List<Transaction>> {
        return firebaseDataManager.getUserTransactions(userId, limit).map { transactionsList ->
            transactionsList.mapNotNull { mapToTransaction(it) }
        }
    }

    override fun getRecentTransactions(userId: String, limit: Int): Flow<List<Transaction>> {
        return firebaseDataManager.getRecentTransactions(userId, limit).map { transactionsList ->
            transactionsList.mapNotNull { mapToTransaction(it) }
        }
    }

    override fun getTransactionsByAccount(userId: String, accountId: String, limit: Int): Flow<List<Transaction>> {
        return firebaseDataManager.getUserTransactions(userId, limit).map { transactionsList ->
            transactionsList
                .filter { it["accountId"] == accountId }
                .mapNotNull { mapToTransaction(it) }
        }
    }

    override fun getTransactionsByType(userId: String, type: String, limit: Int): Flow<List<Transaction>> {
        return firebaseDataManager.getUserTransactions(userId, limit).map { transactionsList ->
            transactionsList
                .filter { it["type"] == type }
                .mapNotNull { mapToTransaction(it) }
        }
    }

    override fun getTransactionsByCategory(
        userId: String,
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>> {
        return firebaseDataManager.getTransactionsByCategoryList(userId, category, startDate, endDate).map { transactionsList ->
            transactionsList.mapNotNull { mapToTransaction(it) }
        }
    }

    override suspend fun getTransactionById(transactionId: String): Resource<Transaction> {
        return try {
            val result = firebaseDataManager.getTransactionById(transactionId)
            if (result.isSuccess) {
                val data = result.getOrNull() ?: return Resource.Error("Transaction not found")
                val transaction = mapToTransaction(data)
                if (transaction != null) {
                    Resource.Success(transaction)
                } else {
                    Resource.Error("Failed to parse transaction data")
                }
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to get transaction")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    // ==================== WRITE OPERATIONS ====================

    override suspend fun createTransaction(transaction: Transaction): Resource<String> {
        val transactionData: Map<String, Any> = mapOf(
            "transactionId" to "trx_${System.currentTimeMillis()}",
            "userId" to transaction.accountId, // Note: accountId sera remplacé par userId lors de l'appel
            "accountId" to transaction.accountId,
            "cardId" to if (transaction.accountId.isNotBlank()) transaction.accountId else "",
            "type" to (if (transaction.type == TransactionType.CREDIT) "INCOME" else "EXPENSE"),
            "category" to (transaction.category ?: "OTHER"),
            "title" to transaction.description,
            "description" to transaction.description,
            "merchant" to (transaction.merchant ?: ""),
            "amount" to transaction.amount,
            "recipientName" to "",
            "recipientAccount" to "",
            "status" to "COMPLETED",
            "balanceAfter" to transaction.balanceAfter,
            "paymentMethod" to "card",
            "createdAt" to java.util.Date(),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        val result = firebaseDataManager.createTransaction(transactionData)
        return if (result.isSuccess) {
            Resource.Success(transactionData["transactionId"] as String)
        } else {
            Resource.Error(result.exceptionOrNull()?.message ?: "Failed to create transaction")
        }
    }

    override suspend fun updateTransaction(transactionId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            val result = firebaseDataManager.updateTransaction(transactionId, updates)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to update transaction")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Resource<Unit> {
        return try {
            val result = firebaseDataManager.deleteTransaction(transactionId)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to delete transaction")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    // ==================== SEARCH & FILTER ====================

    override fun searchTransactions(userId: String, query: String, limit: Int): Flow<List<Transaction>> {
        return getTransactions(userId, limit).map { transactions ->
            val lowerQuery = query.lowercase()
            transactions.filter { transaction ->
                transaction.description.lowercase().contains(lowerQuery) ||
                transaction.merchant?.lowercase()?.contains(lowerQuery) == true ||
                transaction.category?.lowercase()?.contains(lowerQuery) == true
            }
        }
    }

    override fun getTransactionsByDateRange(
        userId: String,
        startDate: Date,
        endDate: Date,
        limit: Int
    ): Flow<List<Transaction>> {
        return firebaseDataManager.getUserTransactions(userId, 500).map { transactionsList ->
            transactionsList
                .mapNotNull { mapToTransaction(it) }
                .filter { transaction ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val transactionDate = try {
                        dateFormat.parse(transaction.date)
                    } catch (e: Exception) {
                        null
                    }
                    transactionDate != null &&
                    !transactionDate.before(startDate) &&
                    !transactionDate.after(endDate)
                }
                .take(limit)
        }
    }

    // ==================== AGGREGATION ====================

    override fun getTotalIncome(userId: String, startDate: Date, endDate: Date): Flow<Double> {
        return getTransactionsByDateRange(userId, startDate, endDate, 1000).map { transactions ->
            transactions
                .filter { it.type == TransactionType.CREDIT }
                .sumOf { it.amount }
        }
    }

    override fun getTotalExpense(userId: String, startDate: Date, endDate: Date): Flow<Double> {
        return getTransactionsByDateRange(userId, startDate, endDate, 1000).map { transactions ->
            transactions
                .filter { it.type == TransactionType.DEBIT }
                .sumOf { it.amount }
        }
    }

    override fun getCategoryExpenses(userId: String, startDate: Date, endDate: Date): Flow<Map<String, Double>> {
        return getTransactionsByDateRange(userId, startDate, endDate, 1000).map { transactions ->
            transactions
                .filter { it.type == TransactionType.DEBIT }
                .groupBy { it.category ?: "OTHER" }
                .mapValues { (_, transactions) ->
                    transactions.sumOf { it.amount }
                }
        }
    }

    override fun getMonthlyStatistics(userId: String, months: Int): Flow<Map<String, Pair<Double, Double>>> {
        return firebaseDataManager.getMonthlyStatistics(userId, months).map { monthlyStatsList ->
            val monthlyStatsMap = mutableMapOf<String, Pair<Double, Double>>()
            monthlyStatsList.forEach { stat ->
                val month = stat["month"] as? String ?: ""
                val income = stat["income"] as? Double ?: 0.0
                val expense = stat["expense"] as? Double ?: 0.0
                monthlyStatsMap[month] = Pair(income, expense)
            }
            monthlyStatsMap
        }
    }

    // ==================== MAPPING FUNCTIONS ====================

    /**
     * Convertit une Map Firestore (DocumentSnapshot) en objet Transaction
     */
    private fun mapToTransaction(data: Map<String, Any>): Transaction? {
        return try {
            val id = data["transactionId"] as? String ?: data["id"] as? String ?: return null
            val accountId = data["accountId"] as? String ?: return null
            val typeStr = data["type"] as? String ?: "EXPENSE"
            val amount = data["amount"] as? Double ?: 0.0
            val description = data["description"] as? String ?: data["title"] as? String ?: ""
            val category = data["category"] as? String
            val merchant = data["merchant"] as? String
            val date = data["date"] as? String ?: formatDate(data["createdAt"])
            val balanceAfter = data["balanceAfter"] as? Double ?: 0.0

            Transaction(
                id = id,
                accountId = accountId,
                type = if (typeStr == "INCOME") TransactionType.CREDIT else TransactionType.DEBIT,
                amount = amount,
                description = description,
                category = category,
                merchant = merchant,
                date = date,
                balanceAfter = balanceAfter
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convertit un timestamp Firestore en String date
     */
    private fun formatDate(timestamp: Any?): String {
        return when (timestamp) {
            is com.google.firebase.Timestamp -> {
                val date = timestamp.toDate()
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            }
            is Date -> {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp)
            }
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}