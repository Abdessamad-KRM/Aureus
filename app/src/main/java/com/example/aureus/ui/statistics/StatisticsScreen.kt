package com.example.aureus.ui.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.StatisticPeriod
import com.example.aureus.ui.statistics.viewmodel.StatisticsViewModel
import com.example.aureus.ui.theme.*
import com.example.aureus.ui.components.charts.LineChartComponent
import com.example.aureus.ui.components.charts.PieChartComponent
import com.example.aureus.ui.components.charts.getCategoryColor
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * Statistics Screen - Firebase-based real-time statistics avec filtres et export
 * Phase 6: Enhanced animations and transitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    // Export result handling
    LaunchedEffect(Unit) {
        viewModel.exportResult.collect { result ->
            scope.launch {
                when (result) {
                    is com.example.aureus.domain.model.Resource.Success -> {
                        snackbarHostState.showSnackbar(
                            message = "Statistics exported successfully!",
                            duration = SnackbarDuration.Short
                        )
                    }
                    is com.example.aureus.domain.model.Resource.Error -> {
                        snackbarHostState.showSnackbar(
                            message = "Export failed: ${result.message}",
                            duration = SnackbarDuration.Short
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Statistics", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Period Filter
                    Box {
                        IconButton(onClick = { showPeriodMenu = true }) {
                            Icon(Icons.Default.CalendarMonth, "Filter by period")
                        }
                        DropdownMenu(
                            expanded = showPeriodMenu,
                            onDismissRequest = { showPeriodMenu = false }
                        ) {
                            StatisticPeriod.entries.forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period.name) },
                                    onClick = {
                                        viewModel.changePeriod(period)
                                        showPeriodMenu = false
                                    },
                                    trailingIcon = if (uiState.selectedPeriod == period) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }
                    
                    // Export Menu
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Export options")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export to CSV") },
                                leadingIcon = { Icon(Icons.Default.Download, null) },
                                onClick = {
                                    viewModel.exportToCSV()
                                    showExportMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export to JSON") },
                                leadingIcon = { Icon(Icons.Default.Description, null) },
                                onClick = {
                                    viewModel.exportToJSON()
                                    showExportMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeutralLightGray),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = SecondaryGold)
                    Text("Loading statistics...", color = NeutralMediumGray)
                }
            }
        } else {
            // Phase 6: AnimatedContent for smooth transitions when data changes
            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(tween(durationMillis = 300)) + expandVertically(tween(durationMillis = 300)),
                exit = fadeOut(tween(durationMillis = 200)) + shrinkVertically(tween(durationMillis = 200))
            ) {
                val density = LocalDensity.current
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralLightGray)
                        .padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                // Phase 15: Performance Optimization - Items with stable keys
                item(key = "period_badge") {
                    PeriodBadge(period = uiState.selectedPeriod)
                }

                item(key = "balance_card") {
                    DynamicBalanceCard(
                        balance = uiState.totalBalance
                    )
                }

                item(key = "spending_circle") {
                    DynamicSpendingCircleCard(
                        percentage = uiState.spendingPercentage,
                        income = uiState.totalIncome,
                        expense = uiState.totalExpense,
                        trend = uiState.spendingTrend
                    )
                }

                // Insights Section - with keys
                if (uiState.insights.isNotEmpty()) {
                    item(key = "insights_header") {
                        Text(
                            text = "Insights",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavyBlue
                        )
                    }

                    items(
                        items = uiState.insights,
                        key = { insight -> "${insight.title}-${insight.type}" } // Phase 15: Stable key using composite
                    ) { insight ->
                        InsightCard(insight = insight)
                    }
                }

                // Professional Line Chart
                item(key = "line_chart") {
                    // Phase 6: Enhanced animation for chart
                    val chartEnterTransition = fadeIn(tween(600, delayMillis = 100)) +
                        slideInHorizontally(animationSpec = tween(600, easing = EaseInOutCubic), initialOffsetX = { it / 2 })

                    AnimatedVisibility(
                        visible = uiState.monthlyStats.isNotEmpty(),
                        enter = chartEnterTransition,
                        exit = fadeOut(tween(300))
                    ) {
                        LineChartComponent(
                            incomeData = uiState.monthlyStats.map { it.income },
                            expenseData = uiState.monthlyStats.map { it.expense },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                        )
                    }
                }

                // Professional Pie Chart for Categories
                if (uiState.categoryStats.isNotEmpty()) {
                    item(key = "pie_chart") {
                        // Phase 6: Animated entry for pie chart
                        val pieEnterTransition = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialScale = 0.8f
                        ) + fadeIn(tween(600, delayMillis = 200))

                        AnimatedVisibility(
                            visible = uiState.categoryStats.isNotEmpty(),
                            enter = pieEnterTransition,
                            exit = scaleOut() + fadeOut(tween(200))
                        ) {
                            PieChartComponent(
                                data = uiState.categoryStats.toMap(),
                                modifier = Modifier.fillMaxWidth(),
                                onSliceClick = { category, amount ->
                                    // Handle slice click if needed
                                }
                            )
                        }
                    }
                }

                // Category Statistics List Header
                item(key = "category_stats_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spending by Category",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavyBlue
                        )
                    }
                }

                // Phase 15: Performance Optimization - Category items with keys
                items(
                    items = uiState.categoryStats,
                    key = { it.first }
                ) { (category, amount) ->
                    val percentage = if (uiState.totalExpense > 0) {
                        (amount / uiState.totalExpense * 100).toInt()
                    } else 0
                    DynamicCategoryStatItem(
                        category = category,
                        amount = amount,
                        percentage = percentage
                    )
                }
            }
        }
    }
    }
}

// ==================== NEW ENHANCED COMPONENTS ====================

@Composable
fun PeriodBadge(period: StatisticPeriod) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SecondaryGold.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = SecondaryGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Period: ${period.name}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )
        }
    }
}

@Composable
fun InsightCard(insight: com.example.aureus.domain.model.SpendingInsight) {
    val bgColor = when (insight.type) {
        com.example.aureus.domain.model.InsightType.SUCCESS -> SemanticGreen.copy(alpha = 0.1f)
        com.example.aureus.domain.model.InsightType.WARNING -> SemanticRed.copy(alpha = 0.1f)
        com.example.aureus.domain.model.InsightType.INFO -> PrimaryMediumBlue.copy(alpha = 0.1f)
        com.example.aureus.domain.model.InsightType.PREDICTION -> Color(0xFF9C27B0).copy(alpha = 0.1f)
        com.example.aureus.domain.model.InsightType.SUGGESTION -> Color(0xFFFF9800).copy(alpha = 0.1f)
    }

    val iconColor = when (insight.type) {
        com.example.aureus.domain.model.InsightType.SUCCESS -> SemanticGreen
        com.example.aureus.domain.model.InsightType.WARNING -> SemanticRed
        com.example.aureus.domain.model.InsightType.INFO -> PrimaryMediumBlue
        com.example.aureus.domain.model.InsightType.PREDICTION -> Color(0xFF9C27B0)
        com.example.aureus.domain.model.InsightType.SUGGESTION -> Color(0xFFFF9800)
    }

    val icon = when (insight.type) {
        com.example.aureus.domain.model.InsightType.SUCCESS -> Icons.Default.CheckCircle
        com.example.aureus.domain.model.InsightType.WARNING -> Icons.Default.Warning
        com.example.aureus.domain.model.InsightType.INFO -> Icons.Default.Info
        com.example.aureus.domain.model.InsightType.PREDICTION -> Icons.Default.TrendingUp
        com.example.aureus.domain.model.InsightType.SUGGESTION -> Icons.Default.Lightbulb
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
                Text(
                    text = insight.description,
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
        }
    }
}

// ==================== EXISTING DYNAMIC COMPONENTS ====================

@Composable
fun DynamicBalanceCard(balance: Double) {
    // Phase 6: Animated balance counter
    val animatedBalance by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "balance"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(PrimaryNavyBlue, PrimaryMediumBlue)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Balance",
                        color = NeutralWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Phase 6: Animated balance text
                AnimatedContent(
                    targetState = animatedBalance,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "balanceText"
                ) { currentBalance ->
                    Text(
                        text = formatCurrency(currentBalance.toDouble()),
                        color = NeutralWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Text(
                    text = "Available Balance",
                    color = NeutralWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DynamicSpendingCircleCard(
    percentage: Int,
    income: Double,
    expense: Double,
    trend: com.example.aureus.domain.model.SpendingTrend? = null
) {
    // Phase 6: Animate progress indicator
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "percentage"
    )

    // Animate income and expense values
    val animatedIncome by animateFloatAsState(
        targetValue = income.toFloat(),
        animationSpec = tween(durationMillis = 800, delayMillis = 200),
        label = "income"
    )

    val animatedExpense by animateFloatAsState(
        targetValue = expense.toFloat(),
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "expense"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavyBlue
                )

                // Phase 6: Animated Trend Indicator
                trend?.let { t ->
                    val trendIcon = when (t.trend) {
                        com.example.aureus.domain.model.TrendDirection.UP -> Icons.Default.TrendingUp
                        com.example.aureus.domain.model.TrendDirection.DOWN -> Icons.Default.TrendingDown
                        com.example.aureus.domain.model.TrendDirection.STABLE -> Icons.Default.TrendingFlat
                    }
                    val trendColor = when (t.trend) {
                        com.example.aureus.domain.model.TrendDirection.UP -> SemanticRed
                        com.example.aureus.domain.model.TrendDirection.DOWN -> SemanticGreen
                        com.example.aureus.domain.model.TrendDirection.STABLE -> NeutralMediumGray
                    }

                    AnimatedVisibility(
                        visible = trend != null,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = trendIcon,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", t.changePercentage)}%",
                                fontSize = 12.sp,
                                color = trendColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Phase 6: Animated CircularProgressIndicator
                CircularProgressIndicator(
                    progress = { animatedPercentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                    color = SecondaryGold,
                    trackColor = NeutralLightGray
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Phase 6: Animated percentage text
                    AnimatedContent(
                        targetState = animatedPercentage.toInt(),
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        label = "percentageText"
                    ) { currentPercentage ->
                        Text(
                            text = "$currentPercentage%",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavyBlue
                        )
                    }
                    Text(
                        text = "of income",
                        fontSize = 14.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Phase 6: Animated values in legends
                SpendingLegend(
                    color = SemanticGreen,
                    label = "Income",
                    amount = animatedIncome.toDouble()
                )
                SpendingLegend(
                    color = SemanticRed,
                    label = "Expenses",
                    amount = animatedExpense.toDouble()
                )
            }
        }
    }
}

@Composable
fun DynamicCategoryStatItem(
    category: String,
    amount: Double,
    percentage: Int
) {
    // Phase 15: Performance Optimization - Remember expensive computations
    val categoryColor by remember(category) {
        mutableStateOf(getDynamicCategoryColor(category))
    }
    val categoryIcon by remember(category) {
        mutableStateOf(getCategoryIconForCategory(category))
    }
    val formattedAmount by remember(amount) {
        mutableStateOf(formatCurrency(amount))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = categoryColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = "Statistics",
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SemanticRed
                )
                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

fun getCategoryIconForCategory(category: String): ImageVector {
    return when (category.uppercase()) {
        "SHOPPING" -> Icons.Default.ShoppingBag
        "FOOD & DRINK", "FOOD" -> Icons.Default.Restaurant
        "TRANSPORT" -> Icons.Default.DirectionsCar
        "ENTERTAINMENT" -> Icons.Default.Movie
        "BILLS" -> Icons.Default.Receipt
        "HEALTH" -> Icons.Default.LocalHospital
        "EDUCATION" -> Icons.Default.School
        "SALARY" -> Icons.Default.AccountBalance
        else -> Icons.Default.MoreHoriz
    }
}

fun getDynamicCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "SHOPPING" -> Color(0xFF9C27B0)
        "FOOD & DRINK", "FOOD" -> Color(0xFFFF9800)
        "TRANSPORT" -> Color(0xFF2196F3)
        "ENTERTAINMENT" -> Color(0xFFE91E63)
        "BILLS" -> Color(0xFFF44336)
        "HEALTH" -> Color(0xFF4CAF50)
        "EDUCATION" -> Color(0xFF3F51B5)
        "SALARY" -> SemanticGreen
        else -> NeutralMediumGray
    }
}

@Composable
fun SpendingLegend(
    color: Color,
    label: String,
    amount: Double
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = NeutralMediumGray
            )
        }
        Text(
            text = formatCurrency(amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavyBlue
        )
    }
}

/**
 * Point précalculé pour le chart ✅
 */
data class ChartPoint(
    val x: Float,
    val y: Float,
    val controlX1: Float,
    val controlX2: Float,
    val prevY: Float
)

/**
 * Précalcule les points du chart en dehors du Canvas ✅
 */
fun preCalculateChartPoints(
    data: List<Float>,
    width: Float,
    height: Float
): List<ChartPoint>? {
    if (data.isEmpty()) return null

    val maxValue = data.maxOrNull() ?: 1f
    val spacing = width / (data.size - 1)
    val heightScale = height / maxValue

    return data.mapIndexed { index, value ->
        val x = spacing * index
        val y = height - (value * heightScale * 0.8f)

        if (index == 0) {
            ChartPoint(x, y, 0f, 0f, 0f)
        } else {
            val prevX = spacing * (index - 1)
            val prevY = height - (data[index - 1] * heightScale * 0.8f)
            val controlX1 = prevX + spacing / 2
            val controlX2 = x - spacing / 2

            ChartPoint(x, y, controlX1, controlX2, prevY)
        }
    }
}

@Composable
fun CurvedLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // ✅ Précalculer les points hors du Canvas
    val chartPoints by remember(data, density) {
        derivedStateOf {
            // Valeurs par défaut pour le calcul préliminaire
            val width = 400f
            val height = 200f
            preCalculateChartPoints(data, width, height)
        }
    }

    Canvas(modifier = modifier) {
        val points = chartPoints ?: return@Canvas

        val path = androidx.compose.ui.graphics.Path()

        points.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.cubicTo(
                    point.controlX1, point.prevY,
                    point.controlX2, point.y,
                    point.x, point.y
                )
            }
        }

        // Draw gradient under line
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    SecondaryGold.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

        // Draw line
        drawPath(
            path = path,
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(SecondaryGold, PrimaryMediumBlue)
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = SecondaryGold,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(point.x, point.y)
            )
            drawCircle(
                color = NeutralWhite,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(point.x, point.y)
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}