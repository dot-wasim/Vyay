package com.vyayah.app.ui.screens.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
import com.vyayah.app.parser.AmountParser
import com.vyayah.app.ui.theme.ForestGreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    viewModel: CardsViewModel = koinViewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val creditCards by viewModel.creditCards.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val inkColor = MaterialTheme.colorScheme.onSurface
    val borderColor = inkColor.copy(alpha = 0.18f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Accounts & Cards",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account or Card")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= 2-PART SUMMARY AT TOP =================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PART 1 (TOP): TOTAL CREDIT & REMAINING LIMIT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL CREDIT LIMIT (${summary.creditCardCount} CARDS)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        letterSpacing = 1.1.sp,
                                        color = inkColor.copy(alpha = 0.65f)
                                    )
                                )
                                Text(
                                    text = "${summary.creditUtilizationPct.toInt()}% used",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (summary.creditUtilizationPct > 50) MaterialTheme.colorScheme.error else ForestGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Big Combined Credit Limit
                            Text(
                                text = AmountParser.formatPaiseToInr(summary.totalCreditLimitMinor),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    color = inkColor
                                )
                            )

                            // Utilization Progress Bar
                            val progressFraction = (summary.creditUtilizationPct / 100f).coerceIn(0f, 1f)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp)),
                                color = if (summary.creditUtilizationPct > 50) MaterialTheme.colorScheme.error else ForestGreen,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid: How much remaining left & Current dues
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Remaining Credit Left",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(summary.totalCreditRemainingMinor),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Dues",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(summary.totalCreditDuesMinor),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // PART 2 (JUST BELOW): TOTAL MONEY IN BANK
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL MONEY IN BANK (${summary.bankAccountCount} ACCOUNTS)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        letterSpacing = 1.1.sp,
                                        color = inkColor.copy(alpha = 0.65f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AmountParser.formatPaiseToInr(summary.totalMoneyInBankMinor),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = ForestGreen
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // ================= SECTION 1: BANK ACCOUNTS & BALANCES =================
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BANK ACCOUNTS & BALANCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = inkColor.copy(alpha = 0.7f)
                    )
                )
            }

            if (bankAccounts.isEmpty()) {
                item {
                    Text(
                        text = "No bank accounts added yet. They are detected from SMS or can be added via '+'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(bankAccounts, key = { it.id }) { acc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = acc.nickname,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Text(
                                        text = "${acc.bank} · •••• ${acc.last4} (${acc.type.name})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = AmountParser.formatPaiseToInr(acc.currentBalance),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = ForestGreen
                                    )
                                )
                                Text(
                                    text = "Available Balance",
                                    style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }

            // ================= SECTION 2: CREDIT CARDS, LIMITS, REMAINING & DUES =================
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CREDIT CARDS (LIMIT, REMAINING & DUES)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = inkColor.copy(alpha = 0.7f)
                    )
                )
            }

            if (creditCards.isEmpty()) {
                item {
                    Text(
                        text = "No credit cards added yet. Add your cards via '+' to track limits and dues.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(creditCards, key = { it.id }) { card ->
                    val limit = card.creditLimit ?: 0L
                    val dues = card.outstanding ?: 0L
                    val remaining = (limit - dues).coerceAtLeast(0L)
                    val utilPct = if (limit > 0) (dues.toFloat() / limit).coerceIn(0f, 1f) else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Card Name & Due Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = inkColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = card.nickname,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Text(
                                            text = "${card.bank} · •••• ${card.last4}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (card.dueDay != null) {
                                    Text(
                                        text = "Due: ${card.dueDay}th",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3-Column Metrics: Limit | Remaining | Dues
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Limit",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.55f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(limit),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Remaining Left",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.55f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(remaining),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Current Dues",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.55f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(dues),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                            }

                            // Card Utilization Progress Bar
                            if (limit > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { utilPct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = if (utilPct > 0.5f) MaterialTheme.colorScheme.error else ForestGreen,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "${(utilPct * 100).toInt()}% of limit used",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = inkColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddDialog) {
        var bankName by remember { mutableStateOf("") }
        var last4 by remember { mutableStateOf("") }
        var balanceRupees by remember { mutableStateOf("") }
        var creditLimitRupees by remember { mutableStateOf("") }
        var isCreditCard by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add Account or Card", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank / Issuer (e.g. HDFC, ICICI)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = last4,
                        onValueChange = { if (it.length <= 4) last4 = it },
                        label = { Text("Last 4 digits") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = balanceRupees,
                        onValueChange = { balanceRupees = it },
                        label = { Text(if (isCreditCard) "Current Dues (₹)" else "Available Balance (₹)") },
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isCreditCard, onCheckedChange = { isCreditCard = it })
                        Text("This is a Credit Card")
                    }
                    if (isCreditCard) {
                        OutlinedTextField(
                            value = creditLimitRupees,
                            onValueChange = { creditLimitRupees = it },
                            label = { Text("Total Credit Limit (₹)") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val balPaise = AmountParser.parseToMinorUnits(balanceRupees.ifBlank { "0" })
                        val limitPaise = if (isCreditCard) AmountParser.parseToMinorUnits(creditLimitRupees.ifBlank { "0" }) else null

                        viewModel.addOrUpdateAccount(
                            Account(
                                bank = bankName.ifBlank { "Bank" },
                                type = if (isCreditCard) AccountType.CREDIT else AccountType.SAVINGS,
                                last4 = last4.ifBlank { "0000" },
                                nickname = "$bankName $last4",
                                openingBalance = balPaise,
                                currentBalance = balPaise,
                                outstanding = if (isCreditCard) balPaise else null,
                                creditLimit = limitPaise,
                                lastReconciledAt = System.currentTimeMillis()
                            )
                        )
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
