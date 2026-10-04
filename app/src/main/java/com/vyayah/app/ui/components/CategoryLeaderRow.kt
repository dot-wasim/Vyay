package com.vyayah.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryLeaderRow(
    categoryName: String,
    amountText: String,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    val inkColor = MaterialTheme.colorScheme.onSurface
    val dotColor = inkColor.copy(alpha = 0.25f)
    val pctInt = percentage.toInt()

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Name
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = inkColor
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Dotted Leader Line
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .padding(horizontal = 4.dp)
            ) {
                drawLine(
                    color = dotColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f), 0f)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Amount (or ••••)
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = inkColor
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Percentage
            Text(
                text = "$pctInt%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = inkColor.copy(alpha = 0.75f)
                ),
                modifier = Modifier.widthIn(min = 28.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Clean Solid Underline Progress Bar
        val barFraction = (percentage / 100f).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(inkColor.copy(alpha = 0.12f))
        ) {
            if (barFraction > 0.005f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barFraction)
                        .height(2.dp)
                        .background(inkColor)
                )
            }
        }
    }
}
