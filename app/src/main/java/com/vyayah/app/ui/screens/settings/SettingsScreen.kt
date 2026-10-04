package com.vyayah.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var showOemDialog by remember { mutableStateOf(false) }
    var showBackfillDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    // SAF file picker for offline model file
    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // Handle offline model import
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy Guarantee Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vyayah has ZERO INTERNET permission. No servers, no accounts, no analytics. Your financial data never leaves this phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Reliability & OEM Battery Management
            item {
                Text("Reliability & Permissions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOemDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BatteryAlert, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OEM Battery & Autostart Guide", fontWeight = FontWeight.SemiBold)
                            Text("Essential setup for Xiaomi, Oppo, Vivo & Realme", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Permissions & Restricted Settings", fontWeight = FontWeight.SemiBold)
                            Text("Open system app settings to allow SMS & notifications", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Data & History
            item {
                Text("Data & Sync", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBackfillDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Backfill Past SMS", fontWeight = FontWeight.SemiBold)
                            Text("Scan older messages from your inbox (1, 3, 6, 12 months)", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            modelPickerLauncher.launch(arrayOf("*/*"))
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import Offline AI Model", fontWeight = FontWeight.SemiBold)
                            Text("Select local Gemma 3 1B or Qwen2.5 0.5B model file", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Danger Zone
            item {
                Text("Danger Zone", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWipeConfirmDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wipe All Data", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Permanently erase encrypted database & rules", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // OEM Autostart Guide Dialog
    if (showOemDialog) {
        AlertDialog(
            onDismissRequest = { showOemDialog = false },
            title = { Text("OEM Battery & Background Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Aggressive battery managers on Chinese OEM phones kill background apps. Follow your brand's instructions:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider()
                    Text("• Xiaomi / Redmi / POCO:", fontWeight = FontWeight.Bold)
                    Text("1. Settings → Apps → Manage apps → Vyayah → Enable Autostart\n2. Battery saver → Set to 'No restrictions'\n3. Open Recents and tap the Lock icon on Vyayah.", style = MaterialTheme.typography.bodySmall)
                    Text("• Oppo / Realme:", fontWeight = FontWeight.Bold)
                    Text("Settings → Battery → Vyayah → Allow background activity & Auto launch. Lock in recents.", style = MaterialTheme.typography.bodySmall)
                    Text("• Vivo / iQOO:", fontWeight = FontWeight.Bold)
                    Text("Settings → Battery → Background power consumption → Allow for Vyayah. Enable Autostart.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showOemDialog = false }) { Text("Got it") }
            }
        )
    }

    // Backfill Dialog
    if (showBackfillDialog) {
        AlertDialog(
            onDismissRequest = { showBackfillDialog = false },
            title = { Text("Backfill Inbox Messages") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose how far back to scan SMS messages:")
                    Button(
                        onClick = {
                            viewModel.triggerBackfill(context, 1)
                            showBackfillDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Past 1 Month") }
                    Button(
                        onClick = {
                            viewModel.triggerBackfill(context, 3)
                            showBackfillDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Past 3 Months") }
                    Button(
                        onClick = {
                            viewModel.triggerBackfill(context, 6)
                            showBackfillDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Past 6 Months") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackfillDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Wipe Confirmation Dialog
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = { Text("Erase All Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will permanently delete all transactions, accounts, rules, and savings goals from this phone. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
