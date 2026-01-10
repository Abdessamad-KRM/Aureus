package com.example.aureus.domain.repository

import com.example.aureus.domain.model.Account
import com.example.aureus.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Account Repository Interface
 */
interface AccountRepository {

    suspend fun getAccounts(forceRefresh: Boolean = false): Flow<List<Account>>

    suspend fun getAccountById(accountId: String): Flow<Account?>

    suspend fun refreshAccounts(): Resource<Unit>

    suspend fun getTotalBalance(): Double
}