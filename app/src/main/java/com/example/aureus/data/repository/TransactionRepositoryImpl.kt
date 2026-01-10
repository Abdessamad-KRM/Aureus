package com.example.aureus.data.repository

import com.example.aureus.data.local.dao.TransactionDao
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.remote.RetrofitClient
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.Transaction
import com.example.aureus.domain.model.TransactionType
import com.example.aureus.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Transaction Repository Implementation
 */
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val tokenGetter: () -> String?
) : TransactionRepository {

    private val transactionApi = RetrofitClient.transactionApiService

    override suspend fun getTransactions(forceRefresh: Boolean): Flow<List<Transaction>> {
        if (forceRefresh) {
            refreshAllTransactions()
        }
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getTransactionsByAccount(
        accountId: String,
        forceRefresh: Boolean
    ): Flow<List<Transaction>> {
        if (forceRefresh) {
            refreshTransactions(accountId)
        }
        return transactionDao.getTransactionsByAccountId(accountId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getRecentTransactions(accountId: String, limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactionsByAccountId(accountId, limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun refreshTransactions(accountId: String): Resource<Unit> {
        return try {
            val token = "Bearer ${tokenGetter()}"
            val response = transactionApi.getTransactionsByAccount(token, accountId)

            if (response.isSuccessful && response.body() != null) {
                val transactions = response.body()!!.map { it.toEntity(accountId) }
                transactionDao.insertTransactions(transactions)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to fetch transactions: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun refreshAllTransactions(): Resource<Unit> {
        return try {
            val token = "Bearer ${tokenGetter()}"
            val response = transactionApi.getTransactions(token)

            if (response.isSuccessful && response.body() != null) {
                val transactions = response.body()!!.map { dto ->
                    dto.toEntity(dto.accountId)
                }
                transactionDao.insertTransactions(transactions)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to fetch transactions: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }
}

// Extension functions for mapping
fun com.example.aureus.data.remote.dto.TransactionResponse.toEntity(accountId: String): TransactionEntity {
    return TransactionEntity(
        id = id,
        accountId = accountId,
        type = type,
        amount = amount,
        description = description,
        category = category,
        merchant = merchant,
        date = date,
        balanceAfter = balanceAfter
    )
}

fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        accountId = accountId,
        type = if (type == "CREDIT") TransactionType.CREDIT else TransactionType.DEBIT,
        amount = amount,
        description = description,
        category = category,
        merchant = merchant,
        date = date,
        balanceAfter = balanceAfter
    )
}