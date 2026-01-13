package com.example.aureus.ui.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.local.AppDatabase
import com.example.aureus.data.offline.OfflineSyncManager
import com.example.aureus.data.offline.SyncStatus
import com.example.aureus.data.remote.firebase.FirebaseDataManager
import com.example.aureus.domain.model.Resource
import com.example.aureus.domain.model.StatisticPeriod
import com.example.aureus.domain.repository.StatisticRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.util.Calendar
import javax.inject.Inject

/**
 * StatisticsViewModel - Offline-First avec support Room cache
 * Phase 7: Offline-First Complete
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val firebaseDataManager: FirebaseDataManager,
    private val statisticRepository: StatisticRepository,
    private val database: AppDatabase,
    private val offlineSyncManager: OfflineSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(selectedPeriod = StatisticPeriod.MONTHLY))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _exportResult = MutableSharedFlow<Resource<String>>()
    val exportResult: SharedFlow<Resource<String>> = _exportResult.asSharedFlow()

    // Sync Status (Phase 7)
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus(false, null, 0, false))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        // Observer le statut de sync via Pattern Observer
        viewModelScope.launch {
            offlineSyncManager.syncStatusPublisher.syncStatusFlow.collect { status ->
                _syncStatus.value = status
            }
        }

        val userId = firebaseDataManager.currentUserId()
        if (userId != null) {
            loadStatistics(userId)
        } else {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
        }
    }

    private fun loadStatistics(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Charger le solde total en priorité (blocking) - Critical for UI
            val totalBalanceAsync = async {
                statisticRepository.getTotalBalance(userId).first()
            }
            _uiState.value = _uiState.value.copy(
                totalBalance = totalBalanceAsync.await()
            )

            // Charger les autres stats en parallèle (non-blocking) - Performance optimization
            async {
                val calendar = Calendar.getInstance()
                val endTime = calendar.time
                calendar.add(Calendar.MONTH, -6)
                val startTime = calendar.time

                // Revenus et dépenses totaux (en parallèle)
                val incomeAsync = async { statisticRepository.getTotalIncome(userId, startTime, endTime).first() }
                val expenseAsync = async { statisticRepository.getTotalExpense(userId, startTime, endTime).first() }

                _uiState.value = _uiState.value.copy(
                    totalIncome = incomeAsync.await(),
                    totalExpense = expenseAsync.await()
                )

                // Pourcentage de dépenses
                launch {
                    statisticRepository.getSpendingPercentage(userId, startTime, endTime).collect { percentage ->
                        _uiState.update { it.copy(spendingPercentage = percentage) }
                    }
                }

                // Statistiques par catégorie
                launch {
                    statisticRepository.getCategoryBreakdown(userId, startTime, endTime).collect { categories ->
                        _uiState.update { it.copy(categoryStats = categories.map { it.category to it.amount }) }
                    }
                }

                // Statistiques mensuelles
                launch {
                    statisticRepository.getMonthlyIncomeExpense(userId, 6).collect { monthlyStats ->
                        val monthlyStatsList = monthlyStats.map { stat ->
                            MonthlyStatData(
                                month = monthToName(stat.month),
                                income = stat.income,
                                expense = stat.expense,
                                year = stat.year,
                                monthIndex = stat.month
                            )
                        }
                        _uiState.update {
                            it.copy(
                                monthlyStats = monthlyStatsList,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                }

                // Tendances de dépenses
                launch {
                    statisticRepository.getSpendingTrends(userId, StatisticPeriod.MONTHLY).collect { trend ->
                        _uiState.update { it.copy(spendingTrend = trend) }
                    }
                }

                // Insights
                launch {
                    statisticRepository.getSpendingInsights(userId, StatisticPeriod.MONTHLY).collect { insights ->
                        _uiState.update { it.copy(insights = insights) }
                    }
                }
            }
        }
    }

    fun refreshStatistics() {
        val userId = firebaseDataManager.currentUserId() ?: return
        
        // Try to sync first if online (Phase 7)
        viewModelScope.launch {
            if (offlineSyncManager.getSyncStatus().isOnline) {
                val syncResult = offlineSyncManager.syncNow()
                when (syncResult) {
                    is com.example.aureus.data.offline.SyncResult.Success -> {
                        loadStatistics(userId)
                    }
                    is com.example.aureus.data.offline.SyncResult.Error -> {
                        // Sync failed, load anyway from cache
                        loadStatistics(userId)
                    }
                }
            } else {
                loadStatistics(userId)
            }
        }
    }

    fun changePeriod(period: StatisticPeriod) {
        val userId = firebaseDataManager.currentUserId() ?: return
        _uiState.update { it.copy(selectedPeriod = period) }
        loadStatistics(userId)
    }

    fun exportToCSV() {
        val userId = firebaseDataManager.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val calendar = Calendar.getInstance()
            val endTime = calendar.time
            
            calendar.add(Calendar.MONTH, -6)
            val startTime = calendar.time

            val result = statisticRepository.exportStatisticsToCSV(userId, startTime, endTime)
            if (result is Resource.Success) {
                _exportResult.emit(result)
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(exportError = result.message) }
            }
            _uiState.update { it.copy(isExporting = false) }
        }
    }

    fun exportToJSON() {
        val userId = firebaseDataManager.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val calendar = Calendar.getInstance()
            val endTime = calendar.time
            
            calendar.add(Calendar.MONTH, -6)
            val startTime = calendar.time

            val result = statisticRepository.exportStatisticsToJSON(userId, startTime, endTime)
            if (result is Resource.Success) {
                _exportResult.emit(result)
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(exportError = result.message) }
            }
            _uiState.update { it.copy(isExporting = false) }
        }
    }

    private fun monthToName(month: Int): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return if (month in 0..11) months[month] else "Unknown"
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val spendingPercentage: Int = 0,
    val categoryStats: List<Pair<String, Double>> = emptyList(),
    val monthlyStats: List<MonthlyStatData> = emptyList(),
    val spendingTrend: com.example.aureus.domain.model.SpendingTrend? = null,
    val insights: List<com.example.aureus.domain.model.SpendingInsight> = emptyList(),
    val selectedPeriod: com.example.aureus.domain.model.StatisticPeriod,
    val isExporting: Boolean = false,
    val error: String? = null,
    val exportError: String? = null,
    val isOfflineMode: Boolean = false // Phase 7: Offline indicator
)

data class MonthlyStatData(
    val month: String,
    val income: Double,
    val expense: Double,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val monthIndex: Int = 0
)