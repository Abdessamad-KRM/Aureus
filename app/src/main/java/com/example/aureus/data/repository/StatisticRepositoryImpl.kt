package com.example.aureus.data.repository

import android.util.Log
import com.example.aureus.data.local.dao.StatisticsCacheDao
import com.example.aureus.data.local.entity.StatisticsCacheEntity
import com.example.aureus.domain.model.*
import com.example.aureus.domain.repository.StatisticRepository
import com.example.aureus.domain.repository.TransactionRepositoryFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Statistic Repository Implementation
 * Utilise TransactionRepositoryFirebase pour calculer les statistiques
 * Phase 3, 5, 6: Added performance optimizations and caching
 */
@Singleton
class StatisticRepositoryImpl @Inject constructor(
    private val transactionRepository: TransactionRepositoryFirebase,
    private val firebaseDataManager: com.example.aureus.data.remote.firebase.FirebaseDataManager,
    private val cacheDao: StatisticsCacheDao
) : StatisticRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    override suspend fun getMonthlyIncomeExpense(userId: String, months: Int): Flow<List<MonthlyStatistic>> {
        return transactionRepository.getMonthlyStatistics(userId, months).map { statsMap ->
            statsMap.map { (key, value) ->
                // key est format "YYYY-MM", value est Pair(income, expense)
                val parts = key.split("-")
                val year = if (parts.size >= 2) parts[0].toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR) else Calendar.getInstance().get(Calendar.YEAR)
                val month = if (parts.size >= 2) (parts[1].toIntOrNull() ?: 0) - 1 else Calendar.getInstance().get(Calendar.MONTH)
                
                MonthlyStatistic(
                    month = month,
                    year = year,
                    income = value.first,
                    expense = value.second,
                    transactionsCount = 0 // À implémenter si nécessaire
                )
            }.sortedBy { it.year * 100 + it.month }
        }
    }

    override suspend fun getMonthlyStatistic(userId: String, year: Int, month: Int): Flow<MonthlyStatistic> {
        return getMonthlyIncomeExpense(userId, 12).map { monthlyStats ->
            monthlyStats.find { it.year == year && it.month == month }
                ?: MonthlyStatistic(month, year, 0.0, 0.0, 0)
        }
    }

    override fun getCategoryBreakdown(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<CategoryStatistic>> {
        return transactionRepository.getCategoryExpenses(userId, startDate, endDate).map { categoryExpensesMap ->
            val totalExpense = categoryExpensesMap.values.sum()
            
            categoryExpensesMap.map { (category, amount) ->
                CategoryStatistic(
                    category = category,
                    amount = amount,
                    transactionCount = 0, // À améliorer en comptant les transactions
                    percentage = if (totalExpense > 0) (amount / totalExpense) * 100 else 0.0
                )
            }.sortedByDescending { it.amount }
        }
    }

    override fun getCategoryExpenses(
        userId: String,
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<Double> {
        return getCategoryBreakdown(userId, startDate, endDate).map { categories ->
            categories.find { it.category.equals(category, ignoreCase = true) }?.amount ?: 0.0
        }
    }

    override suspend fun getSpendingTrends(userId: String, period: StatisticPeriod): Flow<SpendingTrend> {
        return flow {
            val (currentStart, currentEnd) = getPeriodRange(period, 0)
            val (previousStart, previousEnd) = getPeriodRange(period, 1)

            val incomeFlow = transactionRepository.getTotalIncome(userId, currentStart, currentEnd)
            val expenseFlow = transactionRepository.getTotalExpense(userId, currentStart, currentEnd)
            val prevIncomeFlow = transactionRepository.getTotalIncome(userId, previousStart, previousEnd)
            val prevExpenseFlow = transactionRepository.getTotalExpense(userId, previousStart, previousEnd)

            var currentExpense = 0.0
            var previousExpense = 0.0

            expenseFlow.collect { currentExpense = it }
            prevExpenseFlow.collect { previousExpense = it }

            val changeAmount = currentExpense - previousExpense
            val changePercentage = if (previousExpense > 0) {
                ((changeAmount / previousExpense) * 100)
            } else {
                if (currentExpense > 0) 100.0 else 0.0
            }

            val trend = when {
                changePercentage > 5 -> TrendDirection.UP
                changePercentage < -5 -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }

            emit(
                SpendingTrend(
                    period = period,
                    currentValue = currentExpense,
                    previousValue = previousExpense,
                    changePercentage = changePercentage,
                    changeAmount = changeAmount,
                    trend = trend
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSpendingTrendHistory(
        userId: String,
        period: StatisticPeriod,
        periodsCount: Int
    ): Flow<List<SpendingTrend>> {
        return flow {
            val trends = mutableListOf<SpendingTrend>()

            for (i in 0 until periodsCount) {
                val (currentStart, currentEnd) = getPeriodRange(period, i)
                val (previousStart, previousEnd) = getPeriodRange(period, i + 1)

                val currentExpenseFlow = transactionRepository.getTotalExpense(userId, currentStart, currentEnd)
                val previousExpenseFlow = transactionRepository.getTotalExpense(userId, previousStart, previousEnd)

                var currentExpense = 0.0
                var previousExpense = 0.0

                currentExpenseFlow.collect { currentExpense = it }
                previousExpenseFlow.collect { previousExpense = it }

                val changeAmount = currentExpense - previousExpense
                val changePercentage = if (previousExpense > 0) {
                    ((changeAmount / previousExpense) * 100)
                } else {
                    if (currentExpense > 0) 100.0 else 0.0
                }

                val trend = when {
                    changePercentage > 5 -> TrendDirection.UP
                    changePercentage < -5 -> TrendDirection.DOWN
                    else -> TrendDirection.STABLE
                }

                trends.add(
                    SpendingTrend(
                        period = period,
                        currentValue = currentExpense,
                        previousValue = previousExpense,
                        changePercentage = changePercentage,
                        changeAmount = changeAmount,
                        trend = trend
                    )
                )
            }

            emit(trends.reversed())
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getPeriodStatistics(
        userId: String,
        period: StatisticPeriod,
        startDate: Date,
        endDate: Date
    ): Flow<PeriodStatistic> {
        return flow {
            val incomeFlow = transactionRepository.getTotalIncome(userId, startDate, endDate)
            val expenseFlow = transactionRepository.getTotalExpense(userId, startDate, endDate)
            
            var totalIncome = 0.0
            var totalExpense = 0.0
            
            incomeFlow.collect { totalIncome = it }
            expenseFlow.collect { totalExpense = it }
            
            val categoryBreakdown = getCategoryBreakdown(userId, startDate, endDate).first()
            val transactions = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate, 1000).first()
            
            val insights = generateInsights(totalIncome, totalExpense, categoryBreakdown, period)

            val stat = PeriodStatistic(
                period = period,
                startDate = dateFormat.format(startDate),
                endDate = dateFormat.format(endDate),
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netBalance = totalIncome - totalExpense,
                transactionCount = transactions.size,
                categoryBreakdown = categoryBreakdown,
                spendingTrend = null, // À calculer si nécessaire
                insights = insights
            )

            emit(stat)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getPeriodComparison(
        userId: String,
        currentStartDate: Date,
        currentEndDate: Date,
        previousStartDate: Date,
        previousEndDate: Date
    ): Flow<PeriodComparison> {
        return flow {
            val currentStatsFlow = getPeriodStatistics(userId, StatisticPeriod.MONTHLY, currentStartDate, currentEndDate)
            val previousStatsFlow = getPeriodStatistics(userId, StatisticPeriod.MONTHLY, previousStartDate, previousEndDate)

            var currentStats: PeriodStatistic? = null
            var previousStats: PeriodStatistic? = null

            currentStatsFlow.collect { currentStats = it }
            previousStatsFlow.collect { previousStats = it }

            if (currentStats != null && previousStats != null) {
                val incomeChange = currentStats.totalIncome - previousStats.totalIncome
                val expenseChange = currentStats.totalExpense - previousStats.totalExpense
                val balanceChange = currentStats.netBalance - previousStats.netBalance

                val incomeChangePercentage = if (previousStats.totalIncome > 0) {
                    ((incomeChange / previousStats.totalIncome) * 100)
                } else 0.0

                val expenseChangePercentage = if (previousStats.totalExpense > 0) {
                    ((expenseChange / previousStats.totalExpense) * 100)
                } else 0.0

                emit(
                    PeriodComparison(
                        currentPeriod = currentStats,
                        previousPeriod = previousStats,
                        incomeChange = incomeChange,
                        expenseChange = expenseChange,
                        balanceChange = balanceChange,
                        incomeChangePercentage = incomeChangePercentage,
                        expenseChangePercentage = expenseChangePercentage
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getBudgetStatistics(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<BudgetStatistic>> {
        // Budgets simulés - À implémenter avec Firebase pour les budgets utilisateur
        return getCategoryBreakdown(userId, startDate, endDate).map { categories ->
            val defaultBudgets = mapOf(
                "FOOD" to 3000.0,
                "TRANSPORT" to 1000.0,
                "ENTERTAINMENT" to 1500.0,
                "SHOPPING" to 2000.0,
                "BILLS" to 2500.0,
                "HEALTH" to 1000.0,
                "EDUCATION" to 1500.0
            )

            categories.map { category ->
                val budgetAmount = defaultBudgets[category.category.uppercase()] ?: 1000.0
                val actualAmount = category.amount
                val remainingAmount = budgetAmount - actualAmount
                val percentageUsed = if (budgetAmount > 0) (actualAmount / budgetAmount) * 100 else 0.0

                BudgetStatistic(
                    category = category.category,
                    budgetAmount = budgetAmount,
                    actualAmount = actualAmount,
                    remainingAmount = remainingAmount,
                    percentageUsed = percentageUsed,
                    isOverBudget = remainingAmount < 0
                )
            }
        }
    }

    override suspend fun isBudgetExceeded(
        userId: String,
        category: String,
        startDate: Date,
        endDate: Date
    ): Flow<Boolean> {
        return getBudgetStatistics(userId, startDate, endDate).map { budgets ->
            budgets.any { it.category.equals(category, ignoreCase = true) && it.isOverBudget }
        }
    }

    override suspend fun getSpendingInsights(userId: String, period: StatisticPeriod): Flow<List<SpendingInsight>> {
        return flow {
            val (startDate, endDate) = getPeriodRange(period, 0)

            val incomeFlow = transactionRepository.getTotalIncome(userId, startDate, endDate)
            val expenseFlow = transactionRepository.getTotalExpense(userId, startDate, endDate)
            val categoriesFlow = getCategoryBreakdown(userId, startDate, endDate)

            var totalIncome = 0.0
            var totalExpense = 0.0
            val categories = mutableListOf<CategoryStatistic>()

            incomeFlow.collect { totalIncome = it }
            expenseFlow.collect { totalExpense = it }
            categoriesFlow.collect { categories.clear(); categories.addAll(it) }

            val insights = generateInsights(totalIncome, totalExpense, categories, period)
            emit(insights)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun predictFutureSpending(
        userId: String,
        period: StatisticPeriod,
        predictionPeriods: Int
    ): Flow<Double> {
        return flow {
            // Prédiction basée sur la moyenne des 3 dernières périodes
            val expenseHistoryFlow = getSpendingTrendHistory(userId, period, 4)
            val expenseHistory = mutableListOf<Double>()

            expenseHistoryFlow.collect { trends ->
                trends.take(3).forEach { expenseHistory.add(it.currentValue) }
            }

            val predictedSpending = if (expenseHistory.isNotEmpty()) {
                expenseHistory.average()
            } else {
                0.0
            }

            emit(predictedSpending * predictionPeriods)
        }.flowOn(Dispatchers.IO)
    }

    override fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>> {
        return firebaseDataManager.getSavingsGoals(userId).map { goalsList ->
            goalsList.mapNotNull { mapToSavingsGoal(it) }
        }
    }

    override suspend fun createSavingsGoal(goal: SavingsGoal): Resource<Unit> {
        return try {
            val userId = firebaseDataManager.currentUserId() ?: return Resource.Error("User not logged in")
            val goalData = savingsGoalToMap(goal)
            val result = firebaseDataManager.createSavingsGoal(userId, goalData)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to create savings goal")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun updateSavingsGoal(goalId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            val userId = firebaseDataManager.currentUserId() ?: return Resource.Error("User not logged in")
            val result = firebaseDataManager.updateSavingsGoal(userId, goalId, updates)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to update savings goal")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override suspend fun deleteSavingsGoal(goalId: String): Resource<Unit> {
        return try {
            val userId = firebaseDataManager.currentUserId() ?: return Resource.Error("User not logged in")
            val result = firebaseDataManager.deleteSavingsGoal(userId, goalId)
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to delete savings goal")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}", e)
        }
    }

    override fun getTotalBalance(userId: String): Flow<Double> {
        return transactionRepository.getTransactions(userId, 1000).map { transactions ->
            transactions.sumOf { if (it.type.name == "CREDIT") it.amount else -it.amount }
        }
    }

    override fun getTotalIncome(userId: String, startDate: Date, endDate: Date): Flow<Double> {
        return transactionRepository.getTotalIncome(userId, startDate, endDate)
    }

    override fun getTotalExpense(userId: String, startDate: Date, endDate: Date): Flow<Double> {
        return transactionRepository.getTotalExpense(userId, startDate, endDate)
    }

    override suspend fun getSpendingPercentage(userId: String, startDate: Date, endDate: Date): Flow<Int> {
        return flow {
            val incomeFlow = transactionRepository.getTotalIncome(userId, startDate, endDate)
            val expenseFlow = transactionRepository.getTotalExpense(userId, startDate, endDate)

            var income = 0.0
            var expense = 0.0

            incomeFlow.collect { income = it }
            expenseFlow.collect { expense = it }

            val percentage = if (income > 0) {
                ((expense / income) * 100).toInt()
            } else if (expense > 0) {
                100
            } else {
                0
            }

            emit(percentage)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun exportStatisticsToCSV(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Resource<String> {
        return try {
            val csvBuilder = StringBuilder()
            csvBuilder.append("Date,Category,Description,Amount,Type\n")

            val transactionsFlow = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate, 1000)
            var transactions: List<Transaction> = emptyList()

            transactionsFlow.collect { transactions = it }

            transactions.forEach { transaction ->
                csvBuilder.append("${transaction.date},${transaction.category ?: "N/A"},${transaction.description},${transaction.amount},${transaction.type.name}\n")
            }

            Resource.Success(csvBuilder.toString())
        } catch (e: Exception) {
            Resource.Error("Failed to export statistics: ${e.message}", e)
        }
    }

    override suspend fun exportStatisticsToJSON(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Resource<String> {
        return try {
            val incomeFlow = transactionRepository.getTotalIncome(userId, startDate, endDate)
            val expenseFlow = transactionRepository.getTotalExpense(userId, startDate, endDate)
            val categoriesFlow = getCategoryBreakdown(userId, startDate, endDate)

            var totalIncome = 0.0
            var totalExpense = 0.0
            val categories = mutableListOf<CategoryStatistic>()

            incomeFlow.collect { totalIncome = it }
            expenseFlow.collect { totalExpense = it }
            categoriesFlow.collect { categories.clear(); categories.addAll(it) }

            val json = """
                {
                  "period": {
                    "startDate": "${dateFormat.format(startDate)}",
                    "endDate": "${dateFormat.format(endDate)}"
                  },
                  "summary": {
                    "totalIncome": $totalIncome,
                    "totalExpense": $totalExpense,
                    "netBalance": ${totalIncome - totalExpense}
                  },
                  "categoryBreakdown": [
                    ${categories.joinToString(",\n    ") { cat ->
                        """
                        {
                          "category": "${cat.category}",
                          "amount": ${cat.amount},
                          "percentage": ${cat.percentage}
                        }
                    """.trimIndent()
                    }}
                  ]
                }
            """.trimIndent()

            Resource.Success(json)
        } catch (e: Exception) {
            Resource.Error("Failed to export statistics: ${e.message}", e)
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun getPeriodRange(period: StatisticPeriod, periodsAgo: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        when (period) {
            StatisticPeriod.DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, -periodsAgo)
                val end = calendar.time
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                return Pair(calendar.time, end)
            }
            StatisticPeriod.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -periodsAgo)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = calendar.time
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                return Pair(start, calendar.time)
            }
            StatisticPeriod.MONTHLY -> {
                calendar.add(Calendar.MONTH, -periodsAgo)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                return Pair(start, calendar.time)
            }
            StatisticPeriod.YEARLY -> {
                calendar.add(Calendar.YEAR, -periodsAgo)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = calendar.time
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                return Pair(start, calendar.time)
            }
        }
    }

    private fun generateInsights(
        income: Double,
        expense: Double,
        categories: List<CategoryStatistic>,
        period: StatisticPeriod
    ): List<SpendingInsight> {
        val insights = mutableListOf<SpendingInsight>()
        val spendingPercentage = if (income > 0) (expense / income) * 100 else 0.0

        // Insight 1: Dépenses vs Revenus
        if (spendingPercentage > 100) {
            insights.add(
                SpendingInsight(
                    type = InsightType.WARNING,
                    title = "Dépenses dépassent les revenus",
                    description = "Vos dépenses dépassent vos revenus de ${String.format("%.1f", spendingPercentage - 100)}% pour cette période.",
                    value = spendingPercentage,
                    date = null
                )
            )
        } else if (spendingPercentage > 80) {
            insights.add(
                SpendingInsight(
                    type = InsightType.INFO,
                    title = "Proche de la limite de budget",
                    description = "Vos dépenses représentent ${String.format("%.1f", spendingPercentage)}% de vos revenus.",
                    value = spendingPercentage,
                    date = null
                )
            )
        } else {
            insights.add(
                SpendingInsight(
                    type = InsightType.SUCCESS,
                    title = "Bonne gestion budgétaire",
                    description = "Vos dépenses sont dans les limites raisonnables (${String.format("%.1f", spendingPercentage)}% des revenus).",
                    value = spendingPercentage,
                    date = null
                )
            )
        }

        // Insight 2: Catégorie la plus élevée
        if (categories.isNotEmpty()) {
            val topCategory = categories.maxByOrNull { it.amount }
            topCategory?.let {
                insights.add(
                    SpendingInsight(
                        type = InsightType.INFO,
                        title = "Plus grosse dépense: ${it.category}",
                        description = "La catégorie ${it.category} représente ${String.format("%.1f", it.percentage)}% de vos dépenses totales.",
                        value = it.amount,
                        date = null
                    )
                )
            }
        }

        // Insight 3: Épargne potentielle
        val savings = income - expense
        if (savings > 0) {
            insights.add(
                SpendingInsight(
                    type = InsightType.SUCCESS,
                    title = "Épargne générée",
                    description = "Vous avez épargné ${String.format("%.0f", savings)} MAD cette période.",
                    value = savings,
                    date = null
                )
            )
        }

        return insights
    }

    // ==================== SAVINGS GOAL MAPPING FUNCTIONS ====================

    /**
     * Convert SavingsGoal domain model to Firestore Map
     */
    private fun savingsGoalToMap(goal: SavingsGoal): Map<String, Any> {
        return mapOf(
            "id" to goal.id,
            "name" to goal.name,
            "targetAmount" to goal.targetAmount,
            "currentAmount" to goal.currentAmount,
            "deadline" to goal.deadline,
            "categoryName" to goal.categoryName,
            "isCompleted" to goal.isCompleted,
            "progressPercentage" to goal.progressPercentage
        )
    }

    /**
     * Convert Firestore Map to SavingsGoal domain model
     */
    private fun mapToSavingsGoal(data: Map<String, Any>): SavingsGoal? {
        return try {
            SavingsGoal(
                id = data["id"] as? String ?: return null,
                name = data["name"] as? String ?: return null,
                targetAmount = (data["targetAmount"] as? Number)?.toDouble() ?: 0.0,
                currentAmount = (data["currentAmount"] as? Number)?.toDouble() ?: 0.0,
                deadline = data["deadline"] as? String ?: "",
                categoryName = data["categoryName"] as? String ?: "",
                isCompleted = data["isCompleted"] as? Boolean ?: false,
                progressPercentage = (data["progressPercentage"] as? Number)?.toDouble() ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to map SavingsGoal: ${e.message}", e)
            null
        }
    }

    // ==================== CACHE MANAGEMENT (Phase 5) ====================

    override suspend fun <T> getCachedStatistic(
        userId: String,
        statType: String,
        period: String,
        clazz: Class<T>
    ): T? {
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = StatisticsCacheEntity.generateCacheKey(userId, statType, period)
                val cacheEntry = cacheDao.getValidCache(cacheKey)
                cacheEntry?.parseData()
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to get cached statistic: ${e.message}", e)
            null
        }
    }

    override suspend fun <T> cacheStatistic(
        userId: String,
        statType: String,
        data: T,
        period: String,
        ttlMs: Long
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val cacheEntry = StatisticsCacheEntity.fromData(
                    userId = userId,
                    statType = statType,
                    data = data,
                    period = period,
                    ttlMs = ttlMs
                )
                cacheDao.upsertWithCleanup(cacheEntry)
                true
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to cache statistic: ${e.message}", e)
            false
        }
    }

    override suspend fun invalidateCache(userId: String, statType: String) {
        try {
            withContext(Dispatchers.IO) {
                cacheDao.invalidateCachesByType(userId, statType)
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to invalidate cache: ${e.message}", e)
        }
    }

    override suspend fun invalidateAllCache(userId: String) {
        try {
            withContext(Dispatchers.IO) {
                cacheDao.deleteAllUserCaches(userId)
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to invalidate all cache: ${e.message}", e)
        }
    }

    override suspend fun clearExpiredCache() {
        try {
            withContext(Dispatchers.IO) {
                cacheDao.deleteExpiredCaches()
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to clear expired cache: ${e.message}", e)
        }
    }

    override fun <T> getCachedStatisticFlow(
        userId: String,
        statType: String,
        period: String,
        clazz: Class<T>
    ): Flow<T?> {
        return try {
            val cacheKey = StatisticsCacheEntity.generateCacheKey(userId, statType, period)
            cacheDao.getCacheByKeyFlow(cacheKey).map { cacheEntry ->
                if (cacheEntry != null && !cacheEntry.isExpired()) {
                    cacheEntry.parseData()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to get cached statistic flow: ${e.message}", e)
            flow { emit(null) }
        }
    }

    override suspend fun isCacheValid(
        userId: String,
        statType: String,
        period: String
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = StatisticsCacheEntity.generateCacheKey(userId, statType, period)
                val cacheEntry = cacheDao.getValidCache(cacheKey)
                cacheEntry != null && !cacheEntry.isExpired() && cacheEntry.isFresh
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to check cache validity: ${e.message}", e)
            false
        }
    }

    override suspend fun precacheStatistics(userId: String) {
        try {
            withContext(Dispatchers.IO) {
                Log.d("StatisticRepositoryImpl", "Precaching statistics for user: $userId")
                clearExpiredCache()

                val calendar = Calendar.getInstance()
                val endTime = calendar.time
                calendar.add(Calendar.MONTH, -6)
                val startTime = calendar.time

                // Precompute common statistics
                val totalBalance = getTotalBalance(userId).first()
                cacheStatistic(userId, "totalBalance", totalBalance, "default")

                val totalIncome = getTotalIncome(userId, startTime, endTime).first()
                cacheStatistic(userId, "totalIncome", totalIncome, "monthly")

                val totalExpense = getTotalExpense(userId, startTime, endTime).first()
                cacheStatistic(userId, "totalExpense", totalExpense, "monthly")

                val monthlyStats = getMonthlyIncomeExpense(userId, 6).first()
                cacheStatistic(userId, "monthlyStats", monthlyStats, "6months")

                val categoryBreakdown = getCategoryBreakdown(userId, startTime, endTime).first()
                cacheStatistic(userId, "categoryBreakdown", categoryBreakdown, "monthly")

                Log.d("StatisticRepositoryImpl", "Precache completed successfully")
            }
        } catch (e: Exception) {
            Log.e("StatisticRepositoryImpl", "Failed to precache statistics: ${e.message}", e)
        }
    }

    // ==================== PERFORMANCE OPTIMIZATION (Phase 3) ====================
}