package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Transaction Repository Interface
 */
interface TransactionRepository {

    suspend fun getTransactions(forceRefresh: Boolean = false): Flow<List<Transaction>>

    suspend fun getTransactionsByAccount(accountId: String, forceRefresh: Boolean = false): Flow<List<Transaction>>

    suspend fun getRecentTransactions(accountId: String, limit: Int): Flow<List<Transaction>>

    suspend fun refreshTransactions(accountId: String): Resource<Unit>

    suspend fun refreshAllTransactions(): Resource<Unit>
}