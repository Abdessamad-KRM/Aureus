package com.example.aureus.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aureus.ui.theme.PrimaryMediumBlue
import com.example.aureus.ui.theme.SecondaryGold

/**
 * Line Chart professionnel pour les statistiques
 * Affiche les tendances de revenus et dépenses sur plusieurs mois
 */
@Composable
fun LineChartComponent(
    incomeData: List<Double>,
    expenseData: List<Double>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
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
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(
                    color = Color(0xFF4CAF50),
                    label = "Income"
                )
                Spacer(modifier = Modifier.width(16.dp))
                ChartLegendItem(
                    color = Color(0xFFFF5252),
                    label = "Expenses"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Income Chart
            if (incomeData.isNotEmpty()) {
                Text(
                    text = "Income Trend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProfessionalLineChart(
                    data = incomeData.map { it.toFloat() },
                    lineColor = Color(0xFF4CAF50),
                    areaTopColor = Color(0x804CAF50),
                    areaBottomColor = Color(0x004CAF50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expense Chart
            if (expenseData.isNotEmpty()) {
                Text(
                    text = "Expense Trend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProfessionalLineChart(
                    data = expenseData.map { it.toFloat() },
                    lineColor = Color(0xFFFF5252),
                    areaTopColor = Color(0x80FF5252),
                    areaBottomColor = Color(0x00FF5252),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

/**
 * Professional Line Chart Component with smooth curves and gradient
 */
@Composable
private fun ProfessionalLineChart(
    data: List<Float>,
    lineColor: Color,
    areaTopColor: Color,
    areaBottomColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) {
            // Draw empty state
            drawCircle(
                color = Color.Gray.copy(alpha = 0.1f),
                radius = size.minDimension / 4f
            )
        }

        val maxValue = data.maxOrNull() ?: 1f
        val minValue = data.minOrNull() ?: 0f
        val valueRange = maxValue - minValue
        val safeValueRange = if (valueRange == 0f) 1f else valueRange

        val spacing = size.width / (data.size - 1)

        // Create smooth cubic bezier curve
        val linePath = Path()
        data.forEachIndexed { index, value ->
            val normalizedValue = (value - minValue) / safeValueRange
            val x = spacing * index
            val y = size.height - (normalizedValue * size.height * 0.8f + size.height * 0.1f)

            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                val prevX = spacing * (index - 1)
                val prevValue = data[index - 1]
                val prevNormalizedValue = (prevValue - minValue) / safeValueRange
                val prevY = size.height - (prevNormalizedValue * size.height * 0.8f + size.height * 0.1f)

                // Control points for smooth curve
                val controlX1 = prevX + spacing / 3
                val controlX2 = x - spacing / 3

                linePath.cubicTo(
                    controlX1, prevY,
                    controlX2, y,
                    x, y
                )
            }
        }

        // Create fill path for gradient
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    areaTopColor,
                    areaBottomColor
                )
            )
        )

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw data points
        data.forEachIndexed { index, value ->
            val normalizedValue = (value - minValue) / safeValueRange
            val x = spacing * index
            val y = size.height - (normalizedValue * size.height * 0.8f + size.height * 0.1f)

            // Outer circle
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
            // Inner circle
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

/**
 * Simple legend item component
 */
@Composable
private fun ChartLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}