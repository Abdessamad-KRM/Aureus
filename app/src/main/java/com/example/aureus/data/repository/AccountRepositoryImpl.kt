package com.example.aureus.data.repository

import com.example.aureus.data.local.dao.AccountDao
import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.local.entity.AccountEntity
import com.example.aureus.data.remote.RetrofitClient
import com.example.aureus.domain.model.Account
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Account Repository Implementation
 */
class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val userDao: UserDao,
    private val tokenGetter: () -> String?
) : AccountRepository {

    private val accountApi = RetrofitClient.accountApiService

    override suspend fun getAccounts(forceRefresh: Boolean): Flow<List<Account>> {
        if (forceRefresh) {
            refreshAccounts()
        }
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAccountById(accountId: String): Flow<Account?> {
        return accountDao.getAccountById(accountId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun refreshAccounts(): Resource<Unit> {
        return try {
            val token = "Bearer ${tokenGetter()}"
            val response = accountApi.getAccounts(token)

            if (response.isSuccessful && response.body() != null) {
                val accounts = response.body()!!.map { it.toEntity(getUserId()) }
                accountDao.insertAccounts(accounts)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to fetch accounts: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun getTotalBalance(): Double {
        // Get all accounts and sum their balances
        // In a real implementation, this would be more efficient with a database query
        val accounts = accountDao.getAllAccounts()
        var totalBalance = 0.0
        // This would need to collect the flow in a real implementation
        return totalBalance
    }

    private fun getUserId(): String {
        // This should be injected from a proper source
        return ""
    }
}

// Extension functions for mapping
fun com.example.aureus.data.remote.dto.AccountResponse.toEntity(userId: String): AccountEntity {
    return AccountEntity(
        id = id,
        accountNumber = accountNumber,
        accountType = accountType,
        balance = balance,
        currency = currency,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userId = userId
    )
}

fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        accountNumber = accountNumber,
        accountType = accountType,
        balance = balance,
        currency = currency,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}