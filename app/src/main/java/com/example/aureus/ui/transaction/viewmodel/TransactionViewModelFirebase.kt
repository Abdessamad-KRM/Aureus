package com.example.aureus.ui.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Transaction ViewModel - Firebase-based
 * Utilise TransactionRepositoryFirebase pour des données 100% dynamiques
 * Remplace l'ancien TransactionViewModel qui utilisait Retrofit
 */
@HiltViewModel
class TransactionViewModelFirebase @Inject constructor(
    private val transactionRepository: TransactionRepositoryFirebase,
    private val firebaseDataManager: FirebaseDataManager
) : ViewModel() {

    // ==================== UI STATES ====================

    private val _transactionsState = MutableStateFlow<TransactionUiState>(TransactionUiState())
    val transactionsState: StateFlow<TransactionUiState> = _transactionsState.asStateFlow()

    private val _filteredTransactionsState = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val filteredTransactionsState: StateFlow<List<Map<String, Any>>> = _filteredTransactionsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // ==================== DATA LOADING ====================

    init {
        loadTransactions()
        getStatistics()
    }

    /**
     * Charger toutes les transactions en temps réel
     */
    fun loadTransactions() {
        val userId = firebaseDataManager.currentUserId()
        if (userId == null) {
            _transactionsState.value = _transactionsState.value.copy(isLoading = false, error = "User not logged in")
            return
        }

        viewModelScope.launch {
            _transactionsState.value = _transactionsState.value.copy(isLoading = true)

            transactionRepository.getTransactions(userId).collect { transactions ->
                _transactionsState.value = _transactionsState.value.copy(
                    isLoading = false,
                    transactions = transactions,
                    error = null
                )
                // Apply current filter
                applyFilters()
            }
        }
    }

    /**
     * Charger les transactions récentes (pour HomeScreen)
     */
    fun loadRecentTransactions(limit: Int = 10) {
        val userId = firebaseDataManager.currentUserId()
        if (userId == null) return

        viewModelScope.launch {
            transactionRepository.getRecentTransactions(userId, limit).collect { transactions ->
                // Convert List<Transaction> to List<Map<String, Any>> for UI compatibility
                val transactionsMap = transactions.map { transactionToMap(it) }
                _filteredTransactionsState.value = transactionsMap
            }
        }
    }

    /**
     * Rafraîchir les transactions (pull-to-refresh)
     */
    fun refreshTransactions() {
        val userId = firebaseDataManager.currentUserId() ?: return
        _isRefreshing.value = true

        viewModelScope.launch {
            transactionRepository.getTransactions(userId).collect { transactions ->
                _transactionsState.value = _transactionsState.value.copy(isLoading = false, transactions = transactions, error = null)
                applyFilters()
                _isRefreshing.value = false
            }
        }
    }

    // ==================== FILTERING ====================

    /**
     * Filtrer par type (All/Income/Expense)
     */
    fun filterByType(type: String) {
        _selectedFilter.value = type
        applyFilters()
    }

    /**
     * Filtrer par catégorie
     */
    fun filterByCategory(category: String) {
        _searchQuery.value = category
        applyFilters()
    }

    /**
     * Rechercher par mot-clé
     */
    fun searchTransactions(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Search method alias - matches plan specification
     */
    fun search(query: String) {
        searchTransactions(query)
    }

    /**
     * Filtrer par plage de dates
     */
    fun filterByDateRange(startDate: Date, endDate: Date) {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            _isRefreshing.value = true
            transactionRepository.getTransactionsByDateRange(userId, startDate, endDate, 100)
                .collect { transactions ->
                    _transactionsState.value = _transactionsState.value.copy(
                        isLoading = false,
                        transactions = transactions,
                        error = null
                    )
                    _isRefreshing.value = false
                }
        }
    }

    /**
     * Filtrer par date (Today/ThisWeek/ThisMonth/ThisYear)
     */
    fun filterByDatePeriod(period: DatePeriod) {
        val calendar = Calendar.getInstance()
        val (startDate, endDate) = when (period) {
            DatePeriod.Today -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.time, Date())
            }
            DatePeriod.ThisWeek -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.time, Date())
            }
            DatePeriod.ThisMonth -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.time, Date())
            }
            DatePeriod.ThisYear -> {
                calendar.set(Calendar.MONTH, 0)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.time, Date())
            }
            DatePeriod.All -> Pair(Date(0), Date())
        }

        filterByDateRange(startDate, endDate)
    }

    /**
     * Appliquer tous les filtres actifs
     */
    private fun applyFilters() {
        var filtered = _transactionsState.value.transactions

        // Filter by type
        when (_selectedFilter.value) {
            "Income" -> {
                filtered = filtered.filter { it.isCredit }
            }
            "Expense" -> {
                filtered = filtered.filter { it.isDebit }
            }
        }

        // Filter by search query
        if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter { transaction ->
                transaction.description.lowercase().contains(query) ||
                transaction.merchant?.lowercase()?.contains(query) == true ||
                transaction.category?.lowercase()?.contains(query) == true
            }
        }

        // Update UI state
        _filteredTransactionsState.value = filtered.map { transactionToMap(it) }
    }

    // ==================== STATISTICS ====================

    /**
     * Obtenir les statistiques pour la période actuelle
     */
    fun getStatistics() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startDate = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            val endDate = Date()

            // Get income and expense totals
            val incomeFlow = transactionRepository.getTotalIncome(userId, startDate, endDate)
            val expenseFlow = transactionRepository.getTotalExpense(userId, startDate, endDate)
            val categoryExpenses = transactionRepository.getCategoryExpenses(userId, startDate, endDate)

            // Collect income
            incomeFlow.first()?.let { totalIncome ->
                _transactionsState.value = _transactionsState.value.copy(totalIncome = totalIncome)
            }

            // Collect expense
            expenseFlow.first()?.let { totalExpense ->
                _transactionsState.value = _transactionsState.value.copy(totalExpense = totalExpense)
            }

            // Collect category breakdown
            categoryExpenses.first()?.let { categoryBreak ->
                _transactionsState.value = _transactionsState.value.copy(categoryBreakdown = categoryBreak)
            }
        }
    }

    /**
     * Obtenir les statistiques mensuelles pour Charts
     */
    fun getMonthlyStatistics(months: Int = 6) {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            transactionRepository.getMonthlyStatistics(userId, months).collect { monthlyStats ->
                _transactionsState.value = _transactionsState.value.copy(monthlyStatistics = monthlyStats)
            }
        }
    }

    // ==================== ACTION ====================

    fun resetFilters() {
        _selectedFilter.value = "All"
        _searchQuery.value = ""
        applyFilters()
    }

    fun reset() {
        _transactionsState.value = TransactionUiState()
        _filteredTransactionsState.value = emptyList()
        _isRefreshing.value = false
        _searchQuery.value = ""
        _selectedFilter.value = "All"
    }

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Convert Transaction object to Map for UI compatibility
     */
    private fun transactionToMap(transaction: com.example.aureus.domain.model.Transaction): Map<String, Any> {
        // Parse date string to Date object
        val createdAt = try {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(transaction.date) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        return mapOf(
            "id" to transaction.id,
            "transactionId" to transaction.id,
            "accountId" to transaction.accountId,
            "title" to transaction.description,
            "description" to transaction.description,
            "amount" to transaction.amount,
            "type" to (if (transaction.isCredit) "INCOME" else "EXPENSE"),
            "category" to (transaction.category ?: "OTHER"),
            "merchant" to (transaction.merchant ?: ""),
            "date" to transaction.date,
            "createdAt" to createdAt,
            "balanceAfter" to transaction.balanceAfter
        )
    }
}

// ==================== DATA CLASSES ====================

/**
 * UI State pour TransactionScreen
 */
data class TransactionUiState(
    val isLoading: Boolean = true,
    val transactions: List<com.example.aureus.domain.model.Transaction> = emptyList(),
    val error: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    val monthlyStatistics: Map<String, Pair<Double, Double>> = emptyMap()
)

/**
 * Périodes de date pour filtrage
 */
enum class DatePeriod {
    All, Today, ThisWeek, ThisMonth, ThisYear
}