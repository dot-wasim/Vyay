package com.vyayah.app.ui.screens.today

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
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
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
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

    var showAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Vyay",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = inkColor
                            )
                        )
                        Surface(
                            color = ForestGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "व्यय",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = ForestGreen
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }


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

            // Total Net Balance Card with Translucent Rupee Watermark
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        // Translucent Rupee Watermark in background
                        Text(
                            text = "₹",
                            fontSize = 110.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = inkColor.copy(alpha = 0.06f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = 12.dp, y = (-8).dp)
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL NET BALANCE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = inkColor.copy(alpha = 0.65f)
                                    )
                                )
                                Text(
                                    text = "REAL-TIME",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = displayAmount(uiState.totalNetBalanceMinor),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = inkColor
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Banks: ${displayAmount(uiState.totalAvailableBalanceMinor)} · Cards Due: -${displayAmount(uiState.totalCreditOutstandingMinor)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.SansSerif,
                                    color = inkColor.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal scroll of bank chips and credit cards + Add Chip
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(uiState.accounts) { acc ->
                                    val isCard = acc.type == AccountType.CREDIT
                                    val bal = if (isCard) (acc.outstanding ?: 0L) else acc.currentBalance
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                        modifier = Modifier.clickable {
                                            editingAccount = acc
                                            showAccountDialog = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCard) Icons.Default.CreditCard else Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = if (isCard) MaterialTheme.colorScheme.error else ForestGreen
                                            )
                                            Column {
                                                Text(
                                                    text = acc.bank,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = inkColor
                                                    )
                                                )
                                                Text(
                                                    text = (if (isCard) "- " else "") + displayAmount(bal),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        color = if (isCard) MaterialTheme.colorScheme.error else inkColor.copy(alpha = 0.7f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ForestGreen.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable {
                                            editingAccount = null
                                            showAccountDialog = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Account",
                                                modifier = Modifier.size(14.dp),
                                                tint = ForestGreen
                                            )
                                            Text(
                                                text = "Add Bank/Card",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = ForestGreen
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                        CategoryLeaderRow("Rent 🏠", displayAmount(0, false), 95f)
                        CategoryLeaderRow("Food & Treats 🍕🍔", displayAmount(0, false), 4f)
                        CategoryLeaderRow("Groceries 🛒", displayAmount(0, false), 1f)
                        CategoryLeaderRow("Entertainment & Fun 🍿🎬", displayAmount(0, false), 0f)
                        CategoryLeaderRow("Other 📦", displayAmount(0, false), 0f)
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

    if (showAccountDialog) {
        var bankName by remember { mutableStateOf(editingAccount?.bank ?: "") }
        var last4 by remember { mutableStateOf(editingAccount?.last4 ?: "") }
        var balanceRupees by remember {
            val amt = editingAccount?.let { acc ->
                if (acc.type == AccountType.CREDIT) (acc.outstanding ?: 0L) else acc.currentBalance
            } ?: 0L
            mutableStateOf(if (amt > 0) (amt / 100).toString() else "")
        }
        var isCreditCard by remember { mutableStateOf(editingAccount?.type == AccountType.CREDIT) }
        var creditLimitRupees by remember {
            val limit = editingAccount?.creditLimit ?: 0L
            mutableStateOf(if (limit > 0) (limit / 100).toString() else "")
        }

        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text(
                    text = if (editingAccount != null) "Edit Account / Card" else "Add Bank or Card",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name (e.g. Federal, HDFC, SBI)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = last4,
                        onValueChange = { if (it.length <= 4) last4 = it },
                        label = { Text("Last 4 digits") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = balanceRupees,
                        onValueChange = { balanceRupees = it },
                        label = { Text(if (isCreditCard) "Current Dues (₹)" else "Available Balance (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isCreditCard = !isCreditCard }
                    ) {
                        Checkbox(checked = isCreditCard, onCheckedChange = { isCreditCard = it })
                        Text("This is a Credit Card")
                    }
                    if (isCreditCard) {
                        OutlinedTextField(
                            value = creditLimitRupees,
                            onValueChange = { creditLimitRupees = it },
                            label = { Text("Credit Limit (₹)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val balPaise = AmountParser.parseToMinorUnits(balanceRupees.ifBlank { "0" })
                        val limitPaise = if (isCreditCard) AmountParser.parseToMinorUnits(creditLimitRupees.ifBlank { "0" }) else null
                        val now = System.currentTimeMillis()

                        val accountToSave = (editingAccount ?: Account(
                            bank = bankName.ifBlank { "Bank" },
                            type = if (isCreditCard) AccountType.CREDIT else AccountType.SAVINGS,
                            last4 = last4.ifBlank { "0000" },
                            nickname = "$bankName $last4"
                        )).copy(
                            bank = bankName.ifBlank { "Bank" },
                            type = if (isCreditCard) AccountType.CREDIT else AccountType.SAVINGS,
                            last4 = last4.ifBlank { "0000" },
                            nickname = "$bankName $last4",
                            openingBalance = balPaise,
                            currentBalance = balPaise,
                            outstanding = if (isCreditCard) balPaise else null,
                            creditLimit = limitPaise,
                            lastReconciledAt = now
                        )

                        viewModel.addOrUpdateAccount(accountToSave)
                        showAccountDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
