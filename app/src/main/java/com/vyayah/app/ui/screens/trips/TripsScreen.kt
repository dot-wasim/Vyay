package com.vyayah.app.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyayah.app.parser.AmountParser
import com.vyayah.app.ui.theme.ForestGreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    viewModel: TripsViewModel = koinViewModel()
) {
    val trips by viewModel.trips.collectAsState()
    var showNewTripDialog by remember { mutableStateOf(false) }

    val inkColor = MaterialTheme.colorScheme.onSurface
    val borderColor = inkColor.copy(alpha = 0.2f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trips & Travel",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showNewTripDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Trip")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Intro explanation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Trips isolate vacation expenses so travel surges don't distort your regular monthly home budget.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // List Trips
            items(trips, key = { it.id }) { trip ->
                val remaining = (trip.budgetMinor - trip.spentMinor).coerceAtLeast(0L)
                val utilPct = if (trip.budgetMinor > 0) (trip.spentMinor.toFloat() / trip.budgetMinor).coerceIn(0f, 1f) else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(trip.emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = trip.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Text(
                                        text = trip.dateRangeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (trip.isActive) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { utilPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (utilPct > 0.85f) MaterialTheme.colorScheme.error else ForestGreen,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3-Column Split: Budget | Spent | Remaining
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Trip Budget", style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f)))
                                Text(
                                    AmountParser.formatPaiseToInr(trip.budgetMinor),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column {
                                Text("Spent So Far", style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f)))
                                Text(
                                    AmountParser.formatPaiseToInr(trip.spentMinor),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining Left", style = MaterialTheme.typography.labelSmall.copy(color = inkColor.copy(alpha = 0.6f)))
                                Text(
                                    AmountParser.formatPaiseToInr(remaining),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                )
                            }
                        }

                        // Tagged Transactions
                        if (trip.transactions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = borderColor)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "TAGGED TRIP SPENDS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = inkColor.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            trip.transactions.forEach { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${tx.dateText} · ${tx.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "- ${AmountParser.formatPaiseToInr(tx.amountMinor, true)}",
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
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

    if (showNewTripDialog) {
        var tripName by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("🏖️") }
        var dateRange by remember { mutableStateOf("") }
        var budgetRupees by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewTripDialog = false },
            title = {
                Text("Create New Trip", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Trip Emoji") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tripName,
                        onValueChange = { tripName = it },
                        label = { Text("Trip Name (e.g. Goa Vacation)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dateRange,
                        onValueChange = { dateRange = it },
                        label = { Text("Dates (e.g. 10 Oct – 15 Oct)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = budgetRupees,
                        onValueChange = { budgetRupees = it },
                        label = { Text("Trip Budget (₹)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tripName.isNotBlank() && budgetRupees.isNotBlank()) {
                            viewModel.createTrip(tripName, emoji, dateRange, budgetRupees)
                            showNewTripDialog = false
                        }
                    }
                ) {
                    Text("Create Trip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTripDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
