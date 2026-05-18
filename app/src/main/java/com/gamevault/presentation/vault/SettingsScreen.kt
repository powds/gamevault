package com.gamevault.presentation.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var biometricEnabled by remember { mutableStateOf(false) }
    var autoLockTime by remember { mutableStateOf("Immediately") }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onBack) {
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
                    .padding(16.dp)
            ) {
                // Security Section
                Text(
                    text = "Security",
                    color = VaultPrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    icon = Icons.Default.Pattern,
                    title = "Change Pattern",
                    subtitle = "Set a new unlock pattern",
                    onClick = { /* TODO */ }
                )

                SettingsItem(
                    icon = Icons.Default.Pin,
                    title = "Change PIN",
                    subtitle = "Update your PIN code",
                    onClick = { /* TODO */ }
                )

                SettingsSwitchItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Unlock",
                    subtitle = "Use fingerprint to unlock",
                    checked = biometricEnabled,
                    onCheckedChange = { biometricEnabled = it }
                )

                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Auto Lock",
                    subtitle = autoLockTime,
                    onClick = { /* TODO: Show time picker */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Storage Section
                Text(
                    text = "Storage",
                    color = VaultPrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "Cloud Backup",
                    subtitle = "Backup to Google Drive",
                    onClick = { /* TODO */ }
                )

                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Vault Storage",
                    subtitle = "2.5 GB used",
                    onClick = { /* TODO */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Appearance Section
                Text(
                    text = "Appearance",
                    color = VaultPrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Use dark colors",
                    checked = true,
                    onCheckedChange = { }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Danger Zone
                Text(
                    text = "Danger Zone",
                    color = VaultPrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

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
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Data?") },
                text = {
                    Text("This will permanently delete all hidden files and apps. This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
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
            Text(
                text = title,
                color = VaultText
            )
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