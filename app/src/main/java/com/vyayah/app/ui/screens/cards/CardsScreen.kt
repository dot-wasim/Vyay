package com.vyayah.app.ui.screens.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vyayah.app.data.model.Account
import com.vyayah.app.data.model.AccountType
import com.vyayah.app.parser.AmountParser
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    viewModel: CardsViewModel = koinViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Accounts & Cards", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account or Card")
                    }
                }
            )
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No accounts or cards yet.\nThey will appear automatically when SMS arrives, or you can add one above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts, key = { it.id }) { acc ->
                    val isCredit = acc.type == AccountType.CREDIT

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCredit) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = acc.nickname,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCredit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            if (isCredit && acc.creditLimit != null && acc.creditLimit > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val used = acc.outstanding ?: 0L
                                val limit = acc.creditLimit
                                val pct = (used.toFloat() / limit).coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Limit: ${AmountParser.formatPaiseToInr(limit)}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "${(pct * 100).toInt()}% used",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (pct > 0.7f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var bankName by remember { mutableStateOf("") }
        var last4 by remember { mutableStateOf("") }
        var balanceRupees by remember { mutableStateOf("") }
        var isCreditCard by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Account or Card") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name (e.g. HDFC, SBI)") },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val balPaise = AmountParser.parseToMinorUnits(balanceRupees.ifBlank { "0" })
                        viewModel.addOrUpdateAccount(
                            Account(
                                bank = bankName.ifBlank { "Bank" },
                                type = if (isCreditCard) AccountType.CREDIT else AccountType.SAVINGS,
                                last4 = last4.ifBlank { "0000" },
                                nickname = "$bankName $last4",
                                openingBalance = balPaise,
                                currentBalance = balPaise,
                                outstanding = if (isCreditCard) balPaise else null
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
