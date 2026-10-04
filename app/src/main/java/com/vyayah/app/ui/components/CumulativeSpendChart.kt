package com.vyayah.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyayah.app.ui.theme.ForestGreen

@Composable
fun CumulativeSpendChart(
    currentDay: Int = 4,
    daysInMonth: Int = 31,
    currentMonthName: String = "October",
    prevMonthName: String = "September",
    modifier: Modifier = Modifier
) {
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val inkColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hatched box indicator for October
            Canvas(modifier = Modifier.size(16.dp, 10.dp)) {
                drawRect(color = inkColor, style = Stroke(width = 1.dp.toPx()))
                // Diagonal hatch
                drawLine(
                    color = inkColor.copy(alpha = 0.5f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = inkColor.copy(alpha = 0.5f),
                    start = Offset(4f, size.height),
                    end = Offset(size.width, 4f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentMonthName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = inkColor)
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Dotted line indicator for September
            Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                drawLine(
                    color = inkColor.copy(alpha = 0.7f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = prevMonthName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = inkColor.copy(alpha = 0.7f))
            )
        }

        // Chart Area
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val w = size.width
            val h = size.height - 24.dp.toPx() // Reserve space for x-axis labels
            val baseY = h

            // 1. Draw Baseline (X-Axis)
            drawLine(
                color = inkColor,
                start = Offset(0f, baseY),
                end = Offset(w, baseY),
                strokeWidth = 1.2.dp.toPx()
            )

            // 2. Draw Ticks along X-Axis
            val tickDays = listOf(1, 8, 15, 22, 29)
            val dayStepX = w / (daysInMonth - 1)

            tickDays.forEach { day ->
                val tickX = (day - 1) * dayStepX
                drawLine(
                    color = inkColor,
                    start = Offset(tickX, baseY),
                    end = Offset(tickX, baseY + 6.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Simulated Previous Month Pace (Dotted curve)
            val prevMonthPath = Path().apply {
                moveTo(0f, baseY - (h * 0.12f))
                cubicTo(
                    w * 0.2f, baseY - (h * 0.25f),
                    w * 0.4f, baseY - (h * 0.65f),
                    w * 0.7f, baseY - (h * 0.85f)
                )
                cubicTo(
                    w * 0.8f, baseY - (h * 0.90f),
                    w * 0.95f, baseY - (h * 0.96f),
                    w, baseY - (h * 0.98f)
                )
            }

            drawPath(
                path = prevMonthPath,
                color = inkColor.copy(alpha = 0.45f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f), 0f)
                )
            )

            // 4. Current Month Solid Line up to currentDay (e.g. Day 4)
            val currentDayX = (currentDay - 1) * dayStepX
            val currentDayY = baseY - (h * 0.14f)

            // Hatch fill under current month curve
            val fillPath = Path().apply {
                moveTo(0f, baseY)
                lineTo(0f, baseY - (h * 0.12f))
                lineTo(currentDayX, currentDayY)
                lineTo(currentDayX, baseY)
                close()
            }

            // Draw diagonal hatch lines inside the fill
            var hatchX = 0f
            while (hatchX < currentDayX + h) {
                drawLine(
                    color = inkColor.copy(alpha = 0.2f),
                    start = Offset(hatchX, baseY),
                    end = Offset(hatchX - 18.dp.toPx(), baseY - 18.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                hatchX += 8.dp.toPx()
            }

            // Outline of current month
            drawLine(
                color = inkColor,
                start = Offset(0f, baseY - (h * 0.12f)),
                end = Offset(currentDayX, currentDayY),
                strokeWidth = 2.dp.toPx()
            )

            // Vertical boundary line on current day
            drawLine(
                color = inkColor.copy(alpha = 0.5f),
                start = Offset(currentDayX, baseY),
                end = Offset(currentDayX, currentDayY),
                strokeWidth = 1.dp.toPx()
            )

            // Active Green dot on current day
            drawCircle(
                color = primaryColor,
                radius = 3.5.dp.toPx(),
                center = Offset(currentDayX, currentDayY)
            )

            // Dotted horizontal tail after green dot
            drawLine(
                color = primaryColor,
                start = Offset(currentDayX + 4.dp.toPx(), currentDayY),
                end = Offset(currentDayX + 22.dp.toPx(), currentDayY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f), 0f)
            )
        }

        // X-Axis Day Labels below line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1", "8", "15", "22", "29").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = inkColor.copy(alpha = 0.7f),
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    }
}
