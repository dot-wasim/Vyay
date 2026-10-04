package com.vyayah.app.ui.screens.today

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyayah.app.parser.AmountParser
import com.vyayah.app.ui.components.CategoryLeaderRow
import com.vyayah.app.ui.components.CumulativeSpendChart
import com.vyayah.app.ui.components.FinancialSummaryRow
import com.vyayah.app.ui.theme.ForestGreen
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val inkColor = MaterialTheme.colorScheme.onSurface
    val borderColor = inkColor.copy(alpha = 0.2f)

    val todayDateString = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(Date()).uppercase()
    }

    fun displayAmount(amountMinor: Long, showRupeePrefix: Boolean = true): String {
        return if (uiState.hideAmounts) {
            if (showRupeePrefix) "₹ ••••" else "••••"
        } else {
            val formatted = AmountParser.formatPaiseToInr(amountMinor)
            if (showRupeePrefix) formatted else formatted.removePrefix("₹")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Top Bar: Brand, Eye, Add, Settings
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vyayah",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = inkColor
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Eye Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleHideAmounts() },
                            modifier = Modifier
                                .size(38.dp)
                                .border(1.dp, borderColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (uiState.hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle amount visibility",
                                modifier = Modifier.size(18.dp),
                                tint = inkColor
                            )
                        }

                        // Add Button
                        IconButton(
                            onClick = { /* Quick actions */ },
                            modifier = Modifier
                                .size(38.dp)
                                .border(1.dp, borderColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(20.dp),
                                tint = inkColor
                            )
                        }

                        // Settings Button with Notification Badge
                        Box {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier
                                    .size(38.dp)
                                    .border(1.dp, borderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(18.dp),
                                    tint = inkColor
                                )
                            }
                            // Small red alert badge
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp),
                                containerColor = Color(0xFFE04F43)
                            ) {
                                Text("1", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Subtitle Line & Double Rule
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = todayDateString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = inkColor.copy(alpha = 0.65f)
                        )
                    )
                    Text(
                        text = "FOR OWNER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = inkColor.copy(alpha = 0.65f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Double horizontal rule
                Divider(color = borderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(2.dp))
                Divider(color = borderColor, thickness = 1.dp)
            }

            // Spent Pacing Section
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "${uiState.monthName.uppercase()} · ${uiState.daysLeftInMonth} DAYS LEFT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = inkColor.copy(alpha = 0.65f)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You've spent",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                            color = inkColor
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Huge Spend Amount (or ••••)
                    Text(
                        text = displayAmount(uiState.monthToDateSpendMinor),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = inkColor
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— ${uiState.paceVersusLastMonthPercentage}% behind September's pace.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            color = ForestGreen,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            // Cumulative Spend Day Chart
            item {
                CumulativeSpendChart(
                    currentDay = 4,
                    daysInMonth = 31,
                    currentMonthName = "October",
                    prevMonthName = "September",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Financial Summary (INCOME | NET | SAVED)
            item {
                FinancialSummaryRow(
                    incomeText = displayAmount(uiState.monthIncomeMinor),
                    netText = displayAmount(uiState.netSavingsMinor),
                    savedPercentageText = "${uiState.savingsPercentage.toInt()}%",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // WHERE IT WENT Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHERE IT WENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = inkColor.copy(alpha = 0.65f)
                        )
                    )
                    TextButton(
                        onClick = onNavigateToBudget,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "BUDGETS →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            if (uiState.categoryBreakdown.isEmpty()) {
                // Show standard sample categories as in the inspiration screenshot
                item {
                    Column {
                        CategoryLeaderRow("Rent", displayAmount(0, false), 95f)
                        CategoryLeaderRow("Food & Dining", displayAmount(0, false), 4f)
                        CategoryLeaderRow("Groceries", displayAmount(0, false), 1f)
                        CategoryLeaderRow("Subscriptions", displayAmount(0, false), 0f)
                        CategoryLeaderRow("Other", displayAmount(0, false), 0f)
                    }
                }
            } else {
                items(uiState.categoryBreakdown) { item ->
                    CategoryLeaderRow(
                        categoryName = item.category.name,
                        amountText = displayAmount(item.amountMinor, false),
                        percentage = item.percentage
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
