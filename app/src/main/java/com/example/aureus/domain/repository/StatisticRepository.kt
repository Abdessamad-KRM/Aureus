package com.example.aureus.domain.repository

import com.example.aureus.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Statistic Repository Interface
 * Fournit toutes les méthodes pour calculer et récupérer les statistiques
 */
interface StatisticRepository {

    // ==================== REVENUS & DÉPENSES MOISNELLES ====================

    /**
     * Récupère les statistiques mensuelles (revenus et dépenses)
     * @param userId ID de l'utilisateur
     * @param months Nombre de mois à récupérer (défaut: 6)
     */
    suspend fun getMonthlyIncomeExpense(
        userId: String,
        months: Int = 6
    ): Flow<List<MonthlyStatistic>>

    /**
     * Récupère les statistiques pour un mois spécifique
     */
    suspend fun getMonthlyStatistic(
        userId: String,
        year: Int,
        month: Int
    ): Flow<MonthlyStatistic>

    // ==================== RÉPARTITION PAR CATÉGORIE ====================

    /**
     * Récupère la répartition des dépenses par catégorie
     * @param userId ID de l'utilisateur
     * @param startDate Date de début
     * @param endDate Date de fin
     */
    fun getCategoryBreakdown(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<CategoryStatistic>>

    /**
     * Récupère les dépenses pour une catégorie spécifique
     */
    fun getCategoryExpenses(
        userId: String,
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<Double>

    // ==================== TENDANCES DE DÉPENSES ====================

    /**
     * Récupère les tendances de dépenses (comparaison période actuelle vs précédente)
     * @param userId ID de l'utilisateur
     * @param period Période d'analyse
     */
    suspend fun getSpendingTrends(
        userId: String,
        period: StatisticPeriod = StatisticPeriod.MONTHLY
    ): Flow<SpendingTrend>

    /**
     * Récupère l'historique des tendances sur plusieurs périodes
     */
    suspend fun getSpendingTrendHistory(
        userId: String,
        period: StatisticPeriod,
        periodsCount: Int
    ): Flow<List<SpendingTrend>>

    // ==================== STATISTIQUES PAR PÉRIODE ====================

    /**
     * Récupère des statistiques complètes pour une période donnée
     */
    suspend fun getPeriodStatistics(
        userId: String,
        period: StatisticPeriod,
        startDate: Date,
        endDate: Date
    ): Flow<PeriodStatistic>

    /**
     * Compare deux périodes (actuelle vs précédente)
     */
    suspend fun getPeriodComparison(
        userId: String,
        currentStartDate: Date,
        currentEndDate: Date,
        previousStartDate: Date,
        previousEndDate: Date
    ): Flow<PeriodComparison>

    // ==================== STATISTIQUES DE BUDGET ====================

    /**
     * Récupère les statistiques de budget par catégorie
     */
    suspend fun getBudgetStatistics(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<BudgetStatistic>>

    /**
     * Vérifie si le budget est dépassé pour une catégorie
     */
    suspend fun isBudgetExceeded(
        userId: String,
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<Boolean>

    // ==================== INSIGHTS IA ====================

    /**
     * Génère des insights basés sur les dépenses
     */
    suspend fun getSpendingInsights(
        userId: String,
        period: StatisticPeriod = StatisticPeriod.MONTHLY
    ): Flow<List<SpendingInsight>>

    /**
     * Prédit les dépenses futures basées sur l'historique
     */
    suspend fun predictFutureSpending(
        userId: String,
        period: StatisticPeriod,
        predictionPeriods: Int
    ): Flow<Double>

    // ==================== OBJECTIFS D'ÉPARGNE ====================

    /**
     * Récupère tous les objectifs d'épargne de l'utilisateur
     */
    fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>>

    /**
     * Crée un nouvel objectif d'épargne
     */
    suspend fun createSavingsGoal(goal: SavingsGoal): Resource<Unit>

    /**
     * Met à jour un objectif d'épargne
     */
    suspend fun updateSavingsGoal(goalId: String, updates: Map<String, Any>): Resource<Unit>

    /**
     * Supprime un objectif d'épargne
     */
    suspend fun deleteSavingsGoal(goalId: String): Resource<Unit>

    // ==================== STATISTIQUES GLOBALES ====================

    /**
     * Récupère le solde total de l'utilisateur
     */
    fun getTotalBalance(userId: String): Flow<Double>

    /**
     * Récupère le total des revenus sur une période
     */
    fun getTotalIncome(userId: String, startDate: Date, endDate: Date): Flow<Double>

    /**
     * Récupère le total des dépenses sur une période
     */
    fun getTotalExpense(userId: String, startDate: Date, endDate: Date): Flow<Double>

    /**
     * Récupère le pourcentage de dépenses par rapport aux revenus
     */
    suspend fun getSpendingPercentage(userId: String, startDate: Date, endDate: Date): Flow<Int>

    // ==================== EXPORT ====================

    /**
     * Exporte les statistiques en format CSV
     */
    suspend fun exportStatisticsToCSV(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Resource<String>

    /**
     * Exporte les statistiques en format JSON
     */
    suspend fun exportStatisticsToJSON(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Resource<String>
}