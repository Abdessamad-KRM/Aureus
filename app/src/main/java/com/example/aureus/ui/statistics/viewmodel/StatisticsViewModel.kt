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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    // ✅ CORRECTION #4: Job pour gérer et annuler les coroutines de statistiques
    private var statisticsJobs: Job? = null

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

            // ✅ CORRECTION #5 ALTERNATIVE: Écouter directement les transactions
            viewModelScope.launch {
                firebaseDataManager.getUserTransactions(userId, 1000).collect { transactions ->
                    // Les FLOWs de statistiques sont déjà en écoute, donc ils se mettront à jour automatiquement
                    // Juste vérifier si isLoading doit être false
                    if (_uiState.value.isLoading) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
        }
    }

    private fun loadStatistics(userId: String) {
        // ✅ CORRECTION #4: Annuler les anciens coroutines avant de créer les nouveaux
        statisticsJobs?.cancel(CancellationException("Cancelling previous statistics observers"))
        _uiState.update { it.copy(isLoading = true) }

        // ✅ CORRECTION #4: Créer un nouveau Job group
        statisticsJobs = viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endTime = calendar.time
            calendar.add(Calendar.MONTH, -6)
            val startTime = calendar.time

            // ✅ CORRECTION #1: totalBalance avec .collect()
            launch {
                statisticRepository.getTotalBalance(userId).collect { balance ->
                    _uiState.update { it.copy(totalBalance = balance) }
                }
            }

            // ✅ CORRECTION #2: totalIncome avec .collect()
            launch {
                statisticRepository.getTotalIncome(userId, startTime, endTime).collect { income ->
                    _uiState.update { it.copy(totalIncome = income) }
                }
            }

            // ✅ CORRECTION #3: totalExpense avec .collect()
            launch {
                statisticRepository.getTotalExpense(userId, startTime, endTime).collect { expense ->
                    _uiState.update { it.copy(totalExpense = expense) }
                }
            }

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

    fun refreshStatistics() {
        val userId = firebaseDataManager.currentUserId() ?: return

        viewModelScope.launch {
            // ✅ CORRECTION #6: Juste vérifier l'état de sync, pas besoin de recharger toute la logique
            // Les FLOWs sont déjà en écoute, donc les stats se mettront à jour automatiquement
            if (offlineSyncManager.getSyncStatus().isOnline) {
                val syncResult = offlineSyncManager.syncNow()
                when (syncResult) {
                    is com.example.aureus.data.offline.SyncResult.Success -> {
                        // Les FLOWs se chargeront automatiquement depuis Firestore
                        // Pas besoin de rappeler loadStatistics()
                        _uiState.update { it.copy(isOfflineMode = false) }
                    }
                    is com.example.aureus.data.offline.SyncResult.Error -> {
                        // Sync failed, les FLOWs chargeront depuis le cache
                        _uiState.update { it.copy(isOfflineMode = true) }
                    }
                }
            } else {
                _uiState.update { it.copy(isOfflineMode = true) }
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

    override fun onCleared() {
        super.onCleared()
        // ✅ CORRECTION #4: Annuler les coroutines quand ViewModel est détruit
        statisticsJobs?.cancel(CancellationException("StatisticsViewModel cleared"))
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