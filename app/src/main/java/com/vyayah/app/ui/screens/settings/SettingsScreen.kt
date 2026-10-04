package com.vyayah.app.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val hideAmountsDefault by viewModel.hideAmountsDefault.collectAsState()
    val flagSecureEnabled by viewModel.flagSecureEnabled.collectAsState()

    var showOemDialog by remember { mutableStateOf(false) }
    var showBackfillDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showLedgerKeyDialog by remember { mutableStateOf(false) }
    var showPasteKeyDialog by remember { mutableStateOf(false) }

    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> }

    val inkColor = MaterialTheme.colorScheme.onSurface
    val borderColor = inkColor.copy(alpha = 0.2f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
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
            // Privacy Promise Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vyayah has ZERO INTERNET permission. No servers, no accounts. Your financial ledger never leaves this phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SECTION 1: APP SECURITY & LOCK
            item {
                Text(
                    "APP SECURITY & PRIVACY LOCK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Biometric App Lock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Biometric / PIN App Lock", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Require fingerprint or device PIN to open Vyayah", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = appLockEnabled,
                                onCheckedChange = { viewModel.toggleAppLock(it) }
                            )
                        }

                        Divider(color = borderColor)

                        // Hide App Preview in Recents (FLAG_SECURE)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hide in Recent Apps", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Blurs app in app switcher and blocks screen capture", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = flagSecureEnabled,
                                onCheckedChange = { viewModel.toggleFlagSecure(it) }
                            )
                        }

                        Divider(color = borderColor)

                        // Hide Amounts by Default
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hide Amounts by Default", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Starts the app with amounts masked as ••••", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = hideAmountsDefault,
                                onCheckedChange = { viewModel.toggleHideAmountsDefault(it) }
                            )
                        }
                    }
                }
            }

            // SECTION 2: SOVEREIGN RECOVERY & LEDGER KEY
            item {
                Text(
                    "SOVEREIGN RECOVERY & BACKUPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // View Ledger Key Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLedgerKeyDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Your Ledger Key (Vault Phrase)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("View 12-word master recovery phrase", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Paste & Restore Key Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPasteKeyDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Paste Existing Ledger Key", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Restore your key from a paper note or old phone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Export Encrypted Backup
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                val file = viewModel.exportEncryptedBackup(context)
                                if (file != null) {
                                    Toast.makeText(context, "Encrypted backup created: ${file.name}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Export Encrypted Backup (.vyayah)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("AES-256-GCM database snapshot", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.Download, contentDescription = null)
                    }
                }
            }

            // SECTION 3: SMS PARSER & OTP SHIELD
            item {
                Text(
                    "SMS PARSER & BANK HEADERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("OTP & Authentication Shield Active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Authentic bank headers (like VM-SBIUPI, AD-HDFCBK) frequently send OTPs and verification alerts. Vyayah automatically rejects all OTP messages so they never enter your ledger or alter account balances.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SECTION 4: RELIABILITY & OEM WIZARD
            item {
                Text(
                    "RELIABILITY & OEM SETUP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOemDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BatteryAlert, contentDescription = null)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OEM Autostart & Battery Guide", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Setup steps for Xiaomi, Oppo, Vivo & Realme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("System App Permissions", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Review SMS & notification permissions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // SECTION 5: DATA & DANGER ZONE
            item {
                Text(
                    "DATA & HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBackfillDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Backfill Past SMS", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Scan inbox history (1, 3, or 6 months)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWipeConfirmDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wipe All Data", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                            Text("Permanently erase database and ledger", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Modal: View 12-Word Ledger Key Dialog
    if (showLedgerKeyDialog) {
        val ledgerKey = remember { viewModel.getLedgerKey(context) }
        val words = remember { ledgerKey.split(" ") }

        AlertDialog(
            onDismissRequest = { showLedgerKeyDialog = false },
            title = {
                Text("Your Ledger Key", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This 12-word secret phrase is your sovereign key. Write this down on paper. If you switch phones, you will need this key to restore your database backups.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until words.size step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${i + 1}. ${words[i]}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (i + 1 < words.size) {
                                    Text(
                                        text = "${i + 2}. ${words[i + 1]}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Vyayah Ledger Key", ledgerKey)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Ledger Key copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Ledger Key")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLedgerKeyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Paste Existing Ledger Key Dialog
    if (showPasteKeyDialog) {
        var pastedPhrase by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasteKeyDialog = false },
            title = {
                Text("Paste Ledger Key", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter or paste your 12-word recovery phrase separated by spaces:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = pastedPhrase,
                        onValueChange = { pastedPhrase = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. amber bamboo cedar delta...") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.pasteAndImportLedgerKey(context, pastedPhrase)
                        if (success) {
                            Toast.makeText(context, "Ledger Key restored successfully!", Toast.LENGTH_LONG).show()
                            showPasteKeyDialog = false
                        } else {
                            Toast.makeText(context, "Invalid key: Please enter all 12 words", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Restore Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // OEM Autostart Guide Dialog
    if (showOemDialog) {
        AlertDialog(
            onDismissRequest = { showOemDialog = false },
            title = { Text("OEM Battery & Background Setup", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
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
            title = { Text("Backfill Inbox Messages", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
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
            title = { Text("Erase All Data?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
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
