package com.example.aureus.ui.statistics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.ui.statistics.viewmodel.StatisticsViewModel
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.util.*

/**
 * Statistics Screen - Firebase-based real-time statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SecondaryGold)
        }
        return
    }

    // Convert categoryStats Map to list for LazyColumn
    val categoryStatsList = uiState.categoryStats.entries.map { (category, amount) ->
        Pair(category, amount)
    }

    Scaffold(
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralLightGray)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Balance Card
            item {
                DynamicBalanceCard(
                    balance = uiState.totalBalance
                )
            }

            // Spending Circle
            item {
                DynamicSpendingCircleCard(
                    percentage = uiState.spendingPercentage,
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense
                )
            }

            // Chart Card
            item {
                DynamicChartCard(monthlyStats = uiState.monthlyStats)
            }

            // Category Statistics
            item {
                Text(
                    text = "Spending by Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavyBlue
                )
            }

            items(categoryStatsList) { (category, amount) ->
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

// ==================== DYNAMIC FIREBASE-BASED COMPONENTS ====================

@Composable
private fun DynamicBalanceCard(balance: Double) {
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
                    brush = Brush.horizontalGradient(
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

                Text(
                    text = formatCurrency(balance),
                    color = NeutralWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

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
private fun DynamicSpendingCircleCard(
    percentage: Int,
    income: Double,
    expense: Double
) {
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
            Text(
                text = "Monthly Spending",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                CircularProgressIndicator(
                    progress = percentage / 100f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = SecondaryGold,
                    trackColor = NeutralLightGray
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavyBlue
                    )
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
                SpendingLegend(
                    color = SemanticGreen,
                    label = "Income",
                    amount = income
                )
                SpendingLegend(
                    color = SemanticRed,
                    label = "Expenses",
                    amount = expense
                )
            }
        }
    }
}

@Composable
private fun DynamicChartCard(monthlyStats: List<com.example.aureus.ui.statistics.viewmodel.MonthlyStatData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last 6 Months",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavyBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wave Chart
            CurvedLineChart(
                data = monthlyStats.map { it.income.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Month labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyStats.forEach { stat ->
                    Text(
                        text = stat.month,
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicCategoryStatItem(
    category: String,
    amount: Double,
    percentage: Int
) {
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
                        .background(getDynamicCategoryColor(category).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIconForCategory(category),
                        contentDescription = null,
                        tint = getDynamicCategoryColor(category),
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
                    text = formatCurrency(amount),
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

private fun getCategoryIconForCategory(category: String): ImageVector {
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

private fun getDynamicCategoryColor(category: String): Color {
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
private fun SpendingLegend(
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

@Composable
private fun CurvedLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val maxValue = data.maxOrNull() ?: 1f
        val spacing = size.width / (data.size - 1)
        val heightScale = size.height / maxValue

        val path = Path()

        data.forEachIndexed { index, value ->
            val x = spacing * index
            val y = size.height - (value * heightScale * 0.8f)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = spacing * (index - 1)
                val prevY = size.height - (data[index - 1] * heightScale * 0.8f)

                val controlX1 = prevX + spacing / 2
                val controlX2 = x - spacing / 2

                path.cubicTo(
                    controlX1, prevY,
                    controlX2, y,
                    x, y
                )
            }
        }

        // Draw gradient under line
        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    SecondaryGold.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

        // Draw line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(SecondaryGold, PrimaryMediumBlue)
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        data.forEachIndexed { index, value ->
            val x = spacing * index
            val y = size.height - (value * heightScale * 0.8f)

            drawCircle(
                color = SecondaryGold,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = NeutralWhite,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("fr", "MA"))
    format.currency = Currency.getInstance("MAD")
    return format.format(amount).replace("MAD", "").trim() + " MAD"
}

private fun maskCardNumber(cardNumber: String): String {
    return "**** **** **** " + cardNumber.takeLast(4)
}