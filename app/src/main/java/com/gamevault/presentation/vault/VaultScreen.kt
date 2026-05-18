package com.gamevault.presentation.vault

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.domain.model.VaultItem
import com.gamevault.domain.model.VaultItemType
import com.gamevault.presentation.common.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onOpenFile: (Long) -> Unit,
    onOpenAppList: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions result
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionDialog = true
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
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
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Vault",
                        color = VaultText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VaultText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = VaultText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VaultSurface
                )
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VaultSurface,
                contentColor = VaultPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Files", color = if (selectedTab == 0) VaultPrimary else VaultText) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        viewModel.selectTab(1)
                        viewModel.loadApps()
                    },
                    text = { Text("Apps", color = if (selectedTab == 1) VaultPrimary else VaultText) }
                )
            }

            // Content
            when (selectedTab) {
                0 -> FilesTab(
                    items = vaultItems,
                    onOpenFile = onOpenFile,
                    onAddFile = { /* TODO: Implement file picker */ }
                )
                1 -> AppsTab(
                    hiddenApps = hiddenApps,
                    onHideApp = { viewModel.hideApp(it) },
                    onUnhideApp = { viewModel.unhideApp(it.packageName) },
                    onLaunchApp = { viewModel.launchApp(it.packageName) },
                    onBrowseApps = onOpenAppList
                )
            }
        }

        // Permission Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Storage Permission Required") },
                text = {
                    Text("GameVault needs storage permission to hide your files securely.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun FilesTab(
    items: List<VaultItem>,
    onOpenFile: (Long) -> Unit,
    onAddFile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Add button
        Button(
            onClick = onAddFile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Files to Vault")
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = VaultText.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hidden files yet",
                        color = VaultText.copy(alpha = 0.5f)
                    )
                    Text(
                        "Tap above to add files",
                        color = VaultText.copy(alpha = 0.3f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    FileItem(item = item, onClick = { onOpenFile(item.id) })
                }
            }
        }
    }
}

@Composable
private fun FileItem(
    item: VaultItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = VaultSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VaultSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.type) {
                        VaultItemType.PHOTO -> Icons.Default.Image
                        VaultItemType.VIDEO -> Icons.Default.VideoFile
                        VaultItemType.DOCUMENT -> Icons.Default.Description
                        VaultItemType.APP -> Icons.Default.Android
                    },
                    contentDescription = null,
                    tint = VaultPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    color = VaultText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatFileSize(item.size),
                    color = VaultText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Type badge
            Text(
                text = item.type.name,
                color = VaultPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun AppsTab(
    hiddenApps: List<com.gamevault.domain.model.HiddenApp>,
    onHideApp: (android.content.pm.ApplicationInfo) -> Unit,
    onUnhideApp: (com.gamevault.domain.model.HiddenApp) -> Unit,
    onLaunchApp: (com.gamevault.domain.model.HiddenApp) -> Unit,
    onBrowseApps: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hiddenApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = VaultText.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hidden apps",
                        color = VaultText.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onBrowseApps,
                        colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                    ) {
                        Text("Browse Apps")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hiddenApps) { app ->
                    HiddenAppItem(
                        app = app,
                        onLaunch = { onLaunchApp(app) },
                        onUnhide = { onUnhideApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenAppItem(
    app: com.gamevault.domain.model.HiddenApp,
    onLaunch: () -> Unit,
    onUnhide: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VaultSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Android,
                contentDescription = null,
                tint = VaultPrimary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = app.appName,
                color = VaultText,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onLaunch) {
                Icon(
                    Icons.Default.Launch,
                    contentDescription = "Launch",
                    tint = VaultPrimary
                )
            }

            IconButton(onClick = onUnhide) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Unhide",
                    tint = VaultText.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}