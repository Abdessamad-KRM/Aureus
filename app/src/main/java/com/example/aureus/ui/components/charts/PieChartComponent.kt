package com.example.aureus.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.PrimaryNavyBlue
import java.text.DecimalFormat

/**
 * Pie Chart component pour les catégories de dépenses
 * Affiche une visualisation circulaire des dépenses par catégorie
 */
@Composable
fun PieChartComponent(
    data: Map<String, Double>,
    modifier: Modifier = Modifier,
    onSliceClick: ((String, Double) -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Animation pour l'apparition
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800),
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Spending by Category",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavyBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(-90f) // Start from top
                    ) {
                        if (data.isEmpty()) {
                            // Draw empty state
                            drawCircle(
                                color = Color.Gray.copy(alpha = 0.1f),
                                radius = size.minDimension / 4f
                            )
                        } else {
                            val total = data.values.sum()
                            var startAngle = 0f

                            data.forEach { (category, amount) ->
                                val sweepAngle = ((amount / total * 360.0).toFloat()) * animatedProgress.value
                                val color = getCategoryColor(category)

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    style = androidx.compose.ui.graphics.drawscope.Fill,
                                    topLeft = Offset.Zero,
                                    size = Size(size.width, size.height)
                                )

                                startAngle += sweepAngle
                            }

                            // Draw center circle for donut effect
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2.5f
                            )
                        }
                    }

                    // Center text showing total
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = formatAmount(data.values.sum()),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavyBlue
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data.forEach { (category, amount) ->
                        val isSelected = selectedCategory == category
                        val color = getCategoryColor(category)
                        val total = data.values.sum()
                        val percentage = if (total > 0) {
                            (amount / total * 100)
                        } else 0.0

                        CategoryLegendItem(
                            category = category,
                            amount = amount,
                            percentage = percentage,
                            color = color,
                            isSelected = isSelected,
                            onClick = {
                                selectedCategory = if (isSelected) null else category
                                onSliceClick?.invoke(category, amount)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Category legend item component
 */
@Composable
private fun CategoryLegendItem(
    category: String,
    amount: Double,
    percentage: Double,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape)
                )

                Column {
                    Text(
                        text = category.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) color else Color.Gray
                    )
                    Text(
                        text = "${DecimalFormat("0.0").format(percentage)}%",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = formatAmount(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else PrimaryNavyBlue
            )
        }
    }
}

/**
 * Pie chart component for simplified view (centered only)
 */
@Composable
fun PieChartSimple(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Text(
            text = "No data available",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = modifier
        )
        return
    }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800),
        )
    }

    Canvas(modifier = modifier.size(200.dp).rotate(-90f)) {
        val total = data.values.sum()
        var startAngle = 0f

        data.forEach { (category, amount) ->
            val sweepAngle = ((amount / total * 360.0).toFloat()) * animatedProgress.value
            val color = getCategoryColor(category)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.White,
            radius = size.minDimension / 2.5f
        )
    }
}

/**
 * Helper function to get category color
 */
fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "SHOPPING" -> Color(0xFF9C27B0)
        "FOOD & DRINK", "FOOD", "RESTAURANT" -> Color(0xFFFF9800)
        "TRANSPORT", "CAR", "GAS" -> Color(0xFF2196F3)
        "ENTERTAINMENT", "MOVIE", "GAMES" -> Color(0xFFE91E63)
        "BILLS", "UTILITIES", "INSURANCE" -> Color(0xFFF44336)
        "HEALTH", "MEDICAL", "PHARMACY" -> Color(0xFF4CAF50)
        "EDUCATION", "BOOKS", "COURSES" -> Color(0xFF3F51B5)
        "SALARY", "INCOME" -> Color(0xFF66BB6A)
        "TRAVEL", "VACATION" -> Color(0xFFFF5722)
        "HOBBIES" -> Color(0xFF00BCD4)
        "OTHER" -> Color(0xFF607D8B)
        else -> Color(0xFF9E9E9E)
    }
}

/**
 * Helper function to format amount
 */
private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        "${DecimalFormat("#.#").format(amount / 1000)}k"
    } else {
        DecimalFormat("#").format(amount)
    }
}