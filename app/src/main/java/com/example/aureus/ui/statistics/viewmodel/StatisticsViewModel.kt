package com.example.aureus.ui.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aureus.data.StaticStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StatisticsViewModel - Version statique avec données fictives pour la démo
 * Utilise StaticData au lieu de Firebase pour une présentation client
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStaticStatistics()
    }

    private fun loadStaticStatistics() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Simuler un petit délai de chargement
            kotlinx.coroutines.delay(500)

            // Utiliser les données statiques
            val stats = StaticStatistics

            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalBalance = stats.totalIncome - stats.totalExpense,
                    totalIncome = stats.totalIncome,
                    totalExpense = stats.totalExpense,
                    spendingPercentage = stats.spendingPercentage,
                    categoryStats = stats.categoryStats.associate { stat ->
                        stat.category.name to stat.amount
                    },
                    monthlyStats = stats.monthlyStats.map { monthly ->
                        MonthlyStatData(
                            month = monthly.month,
                            income = monthly.income,
                            expense = monthly.expense
                        )
                    }
                )
            }
        }
    }

    fun refreshStatistics() {
        loadStaticStatistics()
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val spendingPercentage: Int = 0,
    val categoryStats: Map<String, Double> = emptyMap(),
    val monthlyStats: List<MonthlyStatData> = emptyList(),
    val error: String? = null
)

data class MonthlyStatData(
    val month: String,
    val income: Double,
    val expense: Double
)