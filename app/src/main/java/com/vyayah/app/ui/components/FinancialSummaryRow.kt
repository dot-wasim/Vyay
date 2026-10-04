package com.vyayah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyayah.app.ui.theme.ForestGreen

@Composable
fun FinancialSummaryRow(
    incomeText: String,
    netText: String,
    savedPercentageText: String,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val inkColor = MaterialTheme.colorScheme.onSurface
    val greenColor = ForestGreen

    Column(modifier = modifier.fillMaxWidth()) {
        Divider(color = borderColor, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // INCOME
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "INCOME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = inkColor.copy(alpha = 0.65f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = incomeText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = greenColor
                    )
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(borderColor)
            )

            // NET
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "NET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = inkColor.copy(alpha = 0.65f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = netText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = greenColor
                    )
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(borderColor)
            )

            // SAVED
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "SAVED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = inkColor.copy(alpha = 0.65f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = savedPercentageText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = inkColor
                    )
                )
            }
        }

        Divider(color = borderColor, thickness = 1.dp)
    }
}
