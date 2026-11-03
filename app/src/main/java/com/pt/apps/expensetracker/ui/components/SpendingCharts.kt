package com.pt.apps.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt.apps.expensetracker.data.model.Categories
import com.pt.apps.expensetracker.domain.CategorySpending
import com.pt.apps.expensetracker.domain.MonthlyTrendPoint

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.atan2


private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

@Composable
fun SpendingDonutChart(
    breakdown: List<CategorySpending>,
    modifier: Modifier = Modifier,
    onCategorySelected: (CategorySpending?) -> Unit = {}
) {
    if (breakdown.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses in this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val totalAmount = remember(breakdown) { breakdown.sumOf { it.totalAmount } }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(breakdown) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .testTag("donut_chart_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(breakdown) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchVec = tapOffset - center
                            val dist = kotlin.math.sqrt(touchVec.x * touchVec.x + touchVec.y * touchVec.y)
                            val outerRadius = size.width / 2f
                            val innerRadius = outerRadius * 0.6f

                            if (dist in innerRadius..outerRadius) {
                                var angle = Math.toDegrees(atan2(touchVec.y.toDouble(), touchVec.x.toDouble())).toFloat()
                                if (angle < 0) angle += 360f

                                var currentStart = -90f
                                var tappedIdx: Int? = null
                                for ((idx, item) in breakdown.withIndex()) {
                                    val sweep = (item.percentage / 100f) * 360f
                                    val normStart = (currentStart % 360 + 360) % 360
                                    val normEnd = (normStart + sweep)

                                    if (normEnd > 360f) {
                                        if (angle >= normStart || angle <= (normEnd % 360)) {
                                            tappedIdx = idx
                                            break
                                        }
                                    } else {
                                        if (angle in normStart..normEnd) {
                                            tappedIdx = idx
                                            break
                                        }
                                    }
                                    currentStart += sweep
                                }
                                selectedIndex = if (selectedIndex == tappedIdx) null else tappedIdx
                                onCategorySelected(selectedIndex?.let { breakdown.getOrNull(it) })
                            } else {
                                selectedIndex = null
                                onCategorySelected(null)
                            }
                        }
                    }
            ) {
                val strokeWidth = size.width * 0.20f
                val chartRadius = (size.width - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                var startAngle = -90f
                for ((idx, item) in breakdown.withIndex()) {
                    val sweepAngle = ((item.percentage / 100f) * 360f) * animationProgress.value
                    val isSelected = selectedIndex == idx
                    val cat = Categories.getCategory(item.category)

                    val effectiveStroke = if (isSelected) strokeWidth * 1.15f else strokeWidth
                    val effectiveRadius = if (isSelected) chartRadius * 1.02f else chartRadius

                    drawArc(
                        color = cat.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.coerceAtLeast(1.5f),
                        useCenter = false,
                        topLeft = Offset(center.x - effectiveRadius, center.y - effectiveRadius),
                        size = Size(effectiveRadius * 2, effectiveRadius * 2),
                        style = Stroke(width = effectiveStroke, cap = StrokeCap.Round)
                    )

                    startAngle += sweepAngle
                }
            }

            // Center Callout Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                val activeItem = selectedIndex?.let { breakdown.getOrNull(it) }
                if (activeItem != null) {
                    Text(
                        text = activeItem.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currencyFormat.format(activeItem.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", activeItem.percentage),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Categories.getCategory(activeItem.category).color
                    )
                } else {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${breakdown.size} categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendBarChart(
    points: List<MonthlyTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxVal = remember(points) {
        val highest = points.maxOfOrNull { it.total } ?: 100.0
        if (highest <= 0.0) 100.0 else highest * 1.15
    }
    val averageVal = remember(points) {
        val nonZero = points.filter { it.total > 0 }
        if (nonZero.isNotEmpty()) nonZero.sumOf { it.total } / nonZero.size else 0.0
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Spending Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (averageVal > 0) {
                Text(
                    text = "Avg: ${currencyFormat.format(averageVal)}/mo",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 12.dp, bottom = 4.dp)
                .testTag("monthly_bar_chart")
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 24.dp.toPx()
            val numBars = points.size
            val barSpacing = chartWidth / numBars
            val barWidth = (barSpacing * 0.45f).coerceIn(16.dp.toPx(), 44.dp.toPx())

            // Draw baseline
            drawLine(
                color = outlineVariant,
                start = Offset(0f, chartHeight),
                end = Offset(chartWidth, chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            // Draw average dashed line if > 0
            if (averageVal > 0) {
                val avgY = (chartHeight - (averageVal / maxVal * chartHeight).toFloat()).coerceAtLeast(0f)
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(0f, avgY),
                    end = Offset(chartWidth, avgY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            points.forEachIndexed { index, point ->
                val barCenterX = (index * barSpacing) + (barSpacing / 2f)
                val barHeight = ((point.total / maxVal) * chartHeight * animationProgress.value).toFloat()
                val barTop = chartHeight - barHeight

                // Draw Bar
                val barColor = if (index == points.size - 1) primaryColor else primaryColor.copy(alpha = 0.65f)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barCenterX - (barWidth / 2f), barTop),
                    size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // Month Labels Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.forEach { point ->
                Text(
                    text = point.monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}
