package com.vyayah.app.ui.screens.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val accounts by viewModel.accounts.collectAsState()
    val combinedMetrics by viewModel.combinedCardMetrics.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val inkColor = MaterialTheme.colorScheme.onSurface
    val borderColor = inkColor.copy(alpha = 0.2f)

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
            // COMBINED CREDIT LIMIT SUMMARY CARD
            if (combinedMetrics.cardCount > 0 && combinedMetrics.totalLimitMinor > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COMBINED CREDIT LIMIT (${combinedMetrics.cardCount} CARDS)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        letterSpacing = 1.1.sp,
                                        color = inkColor.copy(alpha = 0.65f)
                                    )
                                )
                                Text(
                                    text = "${combinedMetrics.utilizationPercentage.toInt()}% used",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (combinedMetrics.utilizationPercentage > 50) MaterialTheme.colorScheme.error else ForestGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Big Combined Limit
                            Text(
                                text = AmountParser.formatPaiseToInr(combinedMetrics.totalLimitMinor),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = inkColor
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Overall Limit Utilization Bar
                            val progressFraction = (combinedMetrics.utilizationPercentage / 100f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (combinedMetrics.utilizationPercentage > 50) MaterialTheme.colorScheme.error else ForestGreen,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid: Total Outstanding vs Available Credit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Outstanding",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(combinedMetrics.totalOutstandingMinor),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Available Credit",
                                        style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f))
                                    )
                                    Text(
                                        text = AmountParser.formatPaiseToInr(combinedMetrics.totalAvailableLimitMinor),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "INDIVIDUAL ACCOUNTS & CARDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = inkColor.copy(alpha = 0.65f)
                    )
                )
            }

            if (accounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "No accounts or cards logged yet.\nTap '+' above to add your bank accounts and credit cards.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(accounts, key = { it.id }) { acc ->
                    val isCredit = acc.type == AccountType.CREDIT

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCredit) Icons.Default.CreditCard else Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isCredit) inkColor else ForestGreen
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = acc.nickname,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp
                                            )
                                        )
                                        Text(
                                            text = "${acc.bank} · •••• ${acc.last4} (${acc.type.name})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = if (isCredit && acc.outstanding != null) {
                                        "Due: " + AmountParser.formatPaiseToInr(acc.outstanding)
                                    } else {
                                        AmountParser.formatPaiseToInr(acc.currentBalance)
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = if (isCredit) MaterialTheme.colorScheme.error else ForestGreen
                                    )
                                )
                            }

                            if (isCredit && acc.creditLimit != null && acc.creditLimit > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                                val used = acc.outstanding ?: 0L
                                val limit = acc.creditLimit
                                val pct = (used.toFloat() / limit).coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Card Limit: ${AmountParser.formatPaiseToInr(limit)}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "${(pct * 100).toInt()}% used",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = if (pct > 0.7f) MaterialTheme.colorScheme.error else ForestGreen,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
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
                        label = { Text("Bank / Issuer Name (e.g. HDFC, ICICI)") },
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
                        label = { Text(if (isCreditCard) "Current Outstanding (₹)" else "Current Balance (₹)") },
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
                                creditLimit = limitPaise
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
