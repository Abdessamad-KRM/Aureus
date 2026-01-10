package com.example.aureus.ui.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.Transaction
import com.example.aureus.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Transaction ViewModel
 * Handles transaction logic
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<Resource<List<Transaction>>>(Resource.Loading)
    val transactionsState: StateFlow<Resource<List<Transaction>>> = _transactionsState.asStateFlow()

    private val _accountTransactionsState = MutableStateFlow<Resource<List<Transaction>>>(Resource.Loading)
    val accountTransactionsState: StateFlow<Resource<List<Transaction>>> = _accountTransactionsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadAllTransactions() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                transactionRepository.getTransactions(true).collect { transactions ->
                    _transactionsState.value = Resource.Success(transactions)
                }
            } catch (e: Exception) {
                _transactionsState.value = Resource.Error("Failed to load transactions: ${e.message}", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadTransactionsByAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                transactionRepository.getTransactionsByAccount(accountId, true).collect { transactions ->
                    _accountTransactionsState.value = Resource.Success(transactions)
                }
            } catch (e: Exception) {
                _accountTransactionsState.value = Resource.Error("Failed to load transactions: ${e.message}", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadRecentTransactions(accountId: String, limit: Int = 10) {
        viewModelScope.launch {
            try {
                transactionRepository.getRecentTransactions(accountId, limit).collect { transactions ->
                    _accountTransactionsState.value = Resource.Success(transactions)
                }
            } catch (e: Exception) {
                _accountTransactionsState.value = Resource.Error("Failed to load transactions: ${e.message}", e)
            }
        }
    }

    fun refreshTransactions(accountId: String) {
        loadTransactionsByAccount(accountId)
    }

    fun resetStates() {
        _transactionsState.value = Resource.Loading
        _accountTransactionsState.value = Resource.Loading
    }
}