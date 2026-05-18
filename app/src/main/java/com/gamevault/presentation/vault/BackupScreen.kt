package com.gamevault.presentation.vault

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.presentation.common.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (String) -> Unit,
    onDeleteBackup: (String) -> Unit,
    onExportBackup: (String) -> Unit
) {
    val context = LocalContext.current
    var backups by remember { mutableStateOf<List<BackupItem>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf<String?>(null) }

    // File picker for restore
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val path = it.path ?: return@let
            onRestoreBackup(path)
        }
    }

    // File picker for export
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val path = it.path ?: return@let
            onExportBackup(path)
        }
    }

    // Load backups
    LaunchedEffect(Unit) {
        // Load backup list - in production this would come from ViewModel
        backups = emptyList()
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
                title = { Text("Backup & Restore", color = VaultText) },
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

            // Encryption Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = VaultPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "AES-256 Encryption",
                            color = VaultText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "All backups are encrypted before storage",
                            color = VaultText.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCreateBackup,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Backup")
                }

                OutlinedButton(
                    onClick = { restoreLauncher.launch(null) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VaultPrimary)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Local Backups Section
            Text(
                "Local Backups",
                color = VaultPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (backups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = VaultText.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No backups yet",
                            color = VaultText.copy(alpha = 0.5f)
                        )
                        Text(
                            "Create your first backup above",
                            color = VaultText.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(backups) { backup ->
                        BackupItemCard(
                            backup = backup,
                            onRestore = { showRestoreDialog = backup.backupId },
                            onDelete = { showDeleteDialog = backup.backupId },
                            onExport = { exportLauncher.launch(null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Drive Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = VaultPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Google Drive",
                            color = VaultText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Cloud backup requires Google Drive API setup. Currently showing local backup only.",
                        color = VaultText.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { /* TODO: Open Google Drive setup */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Configure Google Drive")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Delete confirmation dialog
        showDeleteDialog?.let { backupId ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Backup?", color = VaultText) },
                text = {
                    Text(
                        "This will permanently delete this backup. This action cannot be undone.",
                        color = VaultText.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteBackup(backupId)
                            showDeleteDialog = null
                            backups = backups.filter { it.backupId != backupId }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel", color = VaultText)
                    }
                },
                containerColor = VaultSurface,
                titleContentColor = VaultText,
                textContentColor = VaultText.copy(alpha = 0.7f)
            )
        }

        // Restore confirmation dialog
        showRestoreDialog?.let { backupId ->
            AlertDialog(
                onDismissRequest = { showRestoreDialog = null },
                title = { Text("Restore Backup?", color = VaultText) },
                text = {
                    Text(
                        "This will replace your current vault data with this backup. Continue?",
                        color = VaultText.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onRestoreBackup(backupId)
                            showRestoreDialog = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = VaultPrimary)
                    ) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = null }) {
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
private fun BackupItemCard(
    backup: BackupItem,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Backup,
                contentDescription = null,
                tint = VaultPrimary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dateFormat.format(Date(backup.timestamp)),
                    color = VaultText,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${backup.itemCount} items • ${backup.formattedSize}",
                    color = VaultText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                if (backup.encrypted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = VaultPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Encrypted",
                            color = VaultPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            IconButton(onClick = onExport) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Export",
                    tint = VaultText.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = "Restore",
                    tint = VaultText.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = VaultPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class BackupItem(
    val backupId: String,
    val timestamp: Long,
    val itemCount: Int,
    val totalSize: Long,
    val encrypted: Boolean
) {
    val formattedSize: String
        get() = when {
            totalSize < 1024 -> "$totalSize B"
            totalSize < 1024 * 1024 -> String.format("%.1f KB", totalSize / 1024.0)
            totalSize < 1024 * 1024 * 1024 -> String.format("%.1f MB", totalSize / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0))
        }
}