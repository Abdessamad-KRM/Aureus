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
import com.example.aureus.data.*
import com.example.aureus.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val categoryStats = remember { StaticStatistics.categoryStats }
    val spendingPercentage = remember { StaticStatistics.spendingPercentage }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
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
                BalanceCard()
            }

            // Spending Circle
            item {
                SpendingCircleCard(percentage = spendingPercentage)
            }

            // Chart Card
            item {
                ChartCard()
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

            items(categoryStats) { stat ->
                CategoryStatItem(stat)
            }
        }
    }
}

@Composable
private fun BalanceCard() {
    val defaultCard = StaticCards.cards.first()
    
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
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = SecondaryGold
                    )
                }

                Text(
                    text = formatCurrency(defaultCard.balance),
                    color = NeutralWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = maskCardNumber(defaultCard.cardNumber),
                        color = NeutralWhite,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = defaultCard.cardType.name,
                        color = SecondaryGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingCircleCard(percentage: Int) {
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
                    amount = StaticStatistics.monthlyStats.last().income
                )
                SpendingLegend(
                    color = SemanticRed,
                    label = "Expenses",
                    amount = StaticStatistics.monthlyStats.last().expense
                )
            }
        }
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
private fun ChartCard() {
    val monthlyStats = StaticStatistics.monthlyStats
    
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = SemanticGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+12.5%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SemanticGreen
                    )
                }
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

@Composable
private fun CategoryStatItem(stat: CategoryStatistic) {
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
                        .background(getCategoryColor(stat.category).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(stat.category),
                        contentDescription = null,
                        tint = getCategoryColor(stat.category),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = stat.category.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavyBlue
                    )
                    Text(
                        text = "${stat.transactionCount} transactions",
                        fontSize = 12.sp,
                        color = NeutralMediumGray
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(stat.amount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SemanticRed
                )
                Text(
                    text = "${stat.percentage.toInt()}%",
                    fontSize = 12.sp,
                    color = NeutralMediumGray
                )
            }
        }
    }
}

private fun getCategoryIcon(category: TransactionCategory): ImageVector {
    return when (category) {
        TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
        TransactionCategory.FOOD -> Icons.Default.Restaurant
        TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
        TransactionCategory.ENTERTAINMENT -> Icons.Default.Movie
        TransactionCategory.BILLS -> Icons.Default.Receipt
        TransactionCategory.HEALTH -> Icons.Default.LocalHospital
        TransactionCategory.EDUCATION -> Icons.Default.School
        TransactionCategory.SALARY -> Icons.Default.AccountBalance
        TransactionCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

private fun getCategoryColor(category: TransactionCategory): Color {
    return when (category) {
        TransactionCategory.SHOPPING -> Color(0xFF9C27B0)
        TransactionCategory.FOOD -> Color(0xFFFF9800)
        TransactionCategory.TRANSPORT -> Color(0xFF2196F3)
        TransactionCategory.ENTERTAINMENT -> Color(0xFFE91E63)
        TransactionCategory.BILLS -> Color(0xFFF44336)
        TransactionCategory.HEALTH -> Color(0xFF4CAF50)
        TransactionCategory.EDUCATION -> Color(0xFF3F51B5)
        TransactionCategory.SALARY -> SemanticGreen
        TransactionCategory.OTHER -> NeutralMediumGray
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
