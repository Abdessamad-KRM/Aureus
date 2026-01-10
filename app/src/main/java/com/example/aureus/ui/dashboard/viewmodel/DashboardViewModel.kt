package com.example.aureus.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.domain.model.Account
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard ViewModel
 * Handles dashboard logic with accounts
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _accountsState = MutableStateFlow<Resource<List<Account>>>(Resource.Loading)
    val accountsState: StateFlow<Resource<List<Account>>> = _accountsState.asStateFlow()

    private val _totalBalanceState = MutableStateFlow<Double>(0.0)
    val totalBalanceState: StateFlow<Double> = _totalBalanceState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                accountRepository.getAccounts(true).collect { accounts ->
                    _accountsState.value = Resource.Success(accounts)
                    calculateTotalBalance(accounts)
                }
            } catch (e: Exception) {
                _accountsState.value = Resource.Error("Failed to load accounts: ${e.message}", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshAccounts() {
        loadAccounts()
    }

    private fun calculateTotalBalance(accounts: List<Account>) {
        val total = accounts.sumOf { it.balance }
        _totalBalanceState.value = total
    }
}