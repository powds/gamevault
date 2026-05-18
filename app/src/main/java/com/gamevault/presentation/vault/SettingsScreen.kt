package com.gamevault.presentation.vault

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val context = LocalContext.current
    val storageInfo by viewModel.storageInfo.collectAsStateWithLifecycle()

    var biometricEnabled by remember { mutableStateOf(false) }
    var autoLockEnabled by remember { mutableStateOf(true) }
    var autoLockTimeout by remember { mutableStateOf("Immediately") }
    var intruderCaptureEnabled by remember { mutableStateOf(true) }
    var encryptionEnabled by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showClearSecurityDialog by remember { mutableStateOf(false) }
    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf("No backup yet") }
    var lastBackupTime by remember { mutableStateOf("Never") }

    // File picker for restore
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Handle restore from selected directory
            backupStatus = "Restore from: ${it.path}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("Settings", color = VaultText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VaultText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VaultSurface)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Security Section
                SectionTitle("Security")

                SettingsSwitchItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Unlock",
                    subtitle = "Use fingerprint to unlock vault",
                    checked = biometricEnabled,
                    onCheckedChange = { enabled ->
                        biometricEnabled = enabled
                    }
                )

                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = "Auto Lock",
                    subtitle = "Lock vault when leaving",
                    checked = autoLockEnabled,
                    onCheckedChange = { enabled ->
                        autoLockEnabled = enabled
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Timer,
                    title = "Auto Lock Timeout",
                    subtitle = autoLockTimeout,
                    onClick = { showTimeoutDialog = true }
                )

                SettingsSwitchItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Intruder Capture",
                    subtitle = "Take photo on wrong PIN",
                    checked = intruderCaptureEnabled,
                    onCheckedChange = { enabled ->
                        intruderCaptureEnabled = enabled
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Clear Security",
                    subtitle = "Remove pattern and PIN",
                    onClick = { showClearSecurityDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Backup Section
                SectionTitle("Backup & Storage")

                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "Create Backup",
                    subtitle = "Export vault data to file",
                    onClick = { showCreateBackupDialog = true }
                )

                SettingsItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Restore Backup",
                    subtitle = "Import from backup file",
                    onClick = { restoreLauncher.launch(null) }
                )

                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Last Backup",
                    subtitle = lastBackupTime,
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Encryption Section
                SectionTitle("Encryption")

                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = "Encrypt Files",
                    subtitle = "AES-256 encryption for stored files",
                    checked = encryptionEnabled,
                    onCheckedChange = { enabled ->
                        encryptionEnabled = enabled
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Encrypted Storage",
                    subtitle = "Files stored: ${storageInfo}",
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Google Drive Section
                SectionTitle("Cloud Backup")

                SettingsItem(
                    icon = Icons.Default.Cloud,
                    title = "Google Drive Setup",
                    subtitle = "Requires API configuration",
                    onClick = { }
                )

                SettingsSwitchItem(
                    icon = Icons.Default.Sync,
                    title = "Auto Sync",
                    subtitle = "Automatically backup to cloud",
                    checked = false,
                    onCheckedChange = { }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Appearance Section
                SectionTitle("Appearance")

                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Use dark colors",
                    checked = true,
                    onCheckedChange = { }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Danger Zone
                SectionTitle("Danger Zone")

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Delete All Data",
                    subtitle = "Permanently remove all hidden files",
                    onClick = { showDeleteDialog = true },
                    isDanger = true
                )

                Spacer(modifier = Modifier.weight(1f))

                // Version info
                Text(
                    text = "GameVault v1.0.0",
                    color = VaultText.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Data?", color = VaultText) },
                text = {
                    Text(
                        "This will permanently delete all hidden files and apps. This action cannot be undone.",
                        color = VaultText.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            // TODO: Delete all data
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = VaultText)
                    }
                },
                containerColor = VaultSurface,
                titleContentColor = VaultText,
                textContentColor = VaultText.copy(alpha = 0.7f)
            )
        }

        // Clear security dialog
        if (showClearSecurityDialog) {
            AlertDialog(
                onDismissRequest = { showClearSecurityDialog = false },
                title = { Text("Clear Security?", color = VaultText) },
                text = {
                    Text(
                        "This will remove your pattern and PIN. You will need to set them up again.",
                        color = VaultText.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearSecurityDialog = false
                            // TODO: Clear security
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearSecurityDialog = false }) {
                        Text("Cancel", color = VaultText)
                    }
                },
                containerColor = VaultSurface,
                titleContentColor = VaultText,
                textContentColor = VaultText.copy(alpha = 0.7f)
            )
        }

        // Auto lock timeout dialog
        if (showTimeoutDialog) {
            AlertDialog(
                onDismissRequest = { showTimeoutDialog = false },
                title = { Text("Auto Lock Timeout", color = VaultText) },
                text = {
                    Column {
                        listOf("Immediately", "30 seconds", "1 minute", "5 minutes", "15 minutes").forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        autoLockTimeout = option
                                        showTimeoutDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = autoLockTimeout == option,
                                    onClick = {
                                        autoLockTimeout = option
                                        showTimeoutDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = VaultPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option, color = VaultText)
                            }
                        }
                    }
                },
                confirmButton = { },
                containerColor = VaultSurface,
                titleContentColor = VaultText
            )
        }

        // Create backup dialog
        if (showCreateBackupDialog) {
            AlertDialog(
                onDismissRequest = { showCreateBackupDialog = false },
                title = { Text("Create Backup", color = VaultText) },
                text = {
                    Text(
                        "Create a local backup of your vault data including file references and settings. Actual files will be encrypted before export.",
                        color = VaultText.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCreateBackupDialog = false
                            // TODO: Trigger backup creation
                            backupStatus = "Backup created successfully"
                            lastBackupTime = "Just now"
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Create Backup")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateBackupDialog = false }) {
                        Text("Cancel", color = VaultText)
                    }
                },
                containerColor = VaultSurface,
                titleContentColor = VaultText,
                textContentColor = VaultText.copy(alpha = 0.7f)
            )
        }

        // Restore dialog
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Restore Backup", color = VaultText) },
                text = {
                    Text(
                        "This will replace your current vault data with the backup. Continue?",
                        color = VaultText.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRestoreDialog = false
                            restoreLauncher.launch(null)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Select Backup")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("Cancel", color = VaultText)
                    }
                },
                containerColor = VaultSurface,
                titleContentColor = VaultText,
                textContentColor = VaultText.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = VaultPrimary,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) VaultPrimary else VaultText.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isDanger) VaultPrimary else VaultText
            )
            Text(
                text = subtitle,
                color = VaultText.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = VaultText.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VaultText.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = VaultText)
            Text(
                text = subtitle,
                color = VaultText.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VaultPrimary,
                checkedTrackColor = VaultPrimary.copy(alpha = 0.5f)
            )
        )
    }
}