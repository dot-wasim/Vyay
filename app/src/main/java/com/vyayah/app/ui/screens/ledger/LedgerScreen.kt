package com.vyayah.app.ui.screens.ledger

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.vyayah.app.data.model.PaymentInstrument
import com.vyayah.app.data.model.Transaction
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.parser.AmountParser
import com.vyayah.app.ui.theme.ForestGreen
import org.koin.androidx.compose.koinViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel = koinViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val needsReviewCount by viewModel.needsReviewCount.collectAsState()

    var showAddMissedDialog by remember { mutableStateOf(false) }
    var selectedTxnForDetail by remember { mutableStateOf<Transaction?>(null) }
    val inkColor = MaterialTheme.colorScheme.onSurface
    val borderColor = inkColor.copy(alpha = 0.2f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ledger",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showAddMissedDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Missed Transaction")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                placeholder = { Text("Search merchants, VPAs, senders...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = borderColor,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = inkColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { viewModel.selectCategory(cat.id) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = inkColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Needs Review Banner if items pending
            if (needsReviewCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = "$needsReviewCount transaction(s) need your review",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found.\nTap '+' above to add any missed transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.transaction.id }) { item ->
                        val tx = item.transaction
                        val isCredit = tx.direction == TransactionDirection.CREDIT
                        val formattedDate = DateFormat.format("dd MMM, hh:mm a", Date(tx.timestamp)).toString()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTxnForDetail = tx },
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
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.categoryColor))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tx.merchantNorm ?: tx.sender,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Text(
                                            text = "$formattedDate · ${item.categoryName} (${tx.instrument.name})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = (if (isCredit) "+ " else "- ") + AmountParser.formatPaiseToInr(tx.amountMinor, true),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isCredit) ForestGreen else inkColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Missed Transaction
    if (showAddMissedDialog) {
        var amountRupees by remember { mutableStateOf("") }
        var merchantName by remember { mutableStateOf("") }
        var isExpense by remember { mutableStateOf(true) }
        var selectedInstrument by remember { mutableStateOf(PaymentInstrument.UPI) }
        var selectedCategory by remember { mutableStateOf<Long?>(null) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMissedDialog = false },
            title = {
                Text("Add Missed Transaction", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amountRupees,
                        onValueChange = { amountRupees = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Merchant / Description") },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isExpense,
                            onClick = { isExpense = true },
                            label = { Text("Expense (Debit)") }
                        )
                        FilterChip(
                            selected = !isExpense,
                            onClick = { isExpense = false },
                            label = { Text("Income (Credit)") }
                        )
                    }

                    // Instrument Selector
                    Text("Instrument:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(PaymentInstrument.UPI, PaymentInstrument.CARD, PaymentInstrument.CASH).forEach { inst ->
                            FilterChip(
                                selected = selectedInstrument == inst,
                                onClick = { selectedInstrument = inst },
                                label = { Text(inst.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (amountRupees.isNotBlank() && merchantName.isNotBlank()) {
                            viewModel.addMissedTransaction(
                                amountRupees = amountRupees,
                                merchantName = merchantName,
                                direction = if (isExpense) TransactionDirection.DEBIT else TransactionDirection.CREDIT,
                                instrument = selectedInstrument,
                                categoryId = selectedCategory,
                                notes = notes.ifBlank { null }
                            )
                            showAddMissedDialog = false
                        }
                    }
                ) {
                    Text("Add Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMissedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Detail & Rename Transaction Dialog
    selectedTxnForDetail?.let { tx ->
        var editedName by remember { mutableStateOf(tx.merchantNorm ?: "") }
        var isRenaming by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedTxnForDetail = null },
            title = {
                if (isRenaming) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Rename Merchant") },
                        singleLine = true
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            tx.merchantNorm ?: tx.sender,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { isRenaming = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Amount: ${AmountParser.formatPaiseToInr(tx.amountMinor, true)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text("Type: ${tx.type.name} · ${tx.instrument.name}")
                    if (!tx.upiRef.isNullOrBlank()) Text("UPI Ref: ${tx.upiRef}")
                    if (!tx.upiVpa.isNullOrBlank()) Text("VPA: ${tx.upiVpa}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Original SMS / Record:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = tx.rawBody ?: "(No raw body)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (isRenaming) {
                    Button(onClick = {
                        viewModel.renameTransaction(tx, editedName, true)
                        selectedTxnForDetail = null
                    }) {
                        Text("Save Name")
                    }
                } else {
                    TextButton(onClick = { selectedTxnForDetail = null }) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                IconButton(onClick = {
                    viewModel.deleteTransaction(tx)
                    selectedTxnForDetail = null
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}
