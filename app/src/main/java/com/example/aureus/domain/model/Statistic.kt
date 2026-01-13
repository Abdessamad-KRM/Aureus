package com.example.aureus.domain.model

/**
 * Models pour les statistiques de l'application Aureus
 */

/**
 * Enum pour les périodes de statistiques
 */
enum class StatisticPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Statistique mensuelle avec revenus et dépenses
 */
data class MonthlyStatistic(
    val month: Int, // 0-11 (Janvier-Décembre)
    val year: Int,
    val income: Double,
    val expense: Double,
    val transactionsCount: Int
)

/**
 * Statistique par catégorie
 */
data class CategoryStatistic(
    val category: String,
    val amount: Double,
    val transactionCount: Int,
    val percentage: Double // Pourcentage du total des dépenses
)

/**
 * Tendance des dépenses
 */
data class SpendingTrend(
    val period: StatisticPeriod,
    val currentValue: Double,
    val previousValue: Double,
    val changePercentage: Double, // Positif = augmentation, Négatif = diminution
    val changeAmount: Double,
    val trend: TrendDirection
)

/**
 * Direction de la tendance
 */
enum class TrendDirection {
    UP,      // Augmentation
    DOWN,    // Diminution
    STABLE   // Stable
}

/**
 * Statistique de budget (Budget vs Dépenses)
 */
data class BudgetStatistic(
    val category: String,
    val budgetAmount: Double,
    val actualAmount: Double,
    val remainingAmount: Double,
    val percentageUsed: Double,
    val isOverBudget: Boolean
)

/**
 * Insight IA sur les dépenses
 */
data class SpendingInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val value: Double?,
    val date: String?
)

/**
 * Type d'insight
 */
enum class InsightType {
    WARNING,       // Alerte budget dépassé
    SUCCESS,       // Objectif atteint
    INFO,          // Information générale
    PREDICTION,    // Prédiction future
    SUGGESTION     // Suggestion d'économie
}

/**
 * Statistique complète pour une période donnée
 */
data class PeriodStatistic(
    val period: StatisticPeriod,
    val startDate: String,
    val endDate: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val transactionCount: Int,
    val categoryBreakdown: List<CategoryStatistic>,
    val spendingTrend: SpendingTrend?,
    val insights: List<SpendingInsight>
)

/**
 * Comparaison de deux périodes
 */
data class PeriodComparison(
    val currentPeriod: PeriodStatistic,
    val previousPeriod: PeriodStatistic,
    val incomeChange: Double,
    val expenseChange: Double,
    val balanceChange: Double,
    val incomeChangePercentage: Double,
    val expenseChangePercentage: Double
)

/**
 * Objectif d'épargne (Savings Goal)
 */
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val categoryName: String,
    val isCompleted: Boolean,
    val progressPercentage: Double
)