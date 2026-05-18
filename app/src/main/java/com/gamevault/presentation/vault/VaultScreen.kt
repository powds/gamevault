package com.gamevault.presentation.vault

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.domain.model.VaultItem
import com.gamevault.domain.model.VaultItemType
import com.gamevault.presentation.common.theme.*
import java.io.File
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
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val storageInfo by viewModel.storageInfo.collectAsStateWithLifecycle()

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<VaultItem?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions result
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            val fileName = it.lastPathSegment ?: "file"
            val type = when {
                fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif") -> VaultItemType.PHOTO
                fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".webm") -> VaultItemType.VIDEO
                else -> VaultItemType.DOCUMENT
            }
            // Get file size
            var size = 0L
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst() && sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex)
                }
            }
            viewModel.addFileToVault(it.toString(), fileName, type, size)
        }
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
                    Column {
                        Text("Vault", color = VaultText)
                        Text(
                            text = storageInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = VaultText.copy(alpha = 0.5f)
                        )
                    }
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
                    items = filteredItems,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.search(it) },
                    onClearSearch = { viewModel.clearSearch() },
                    onOpenFile = onOpenFile,
                    onAddFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                    onDeleteFile = { showDeleteDialog = it }
                )
                1 -> AppsTab(
                    hiddenApps = hiddenApps,
                    onUnhideApp = { viewModel.unhideApp(it) },
                    onLaunchApp = { viewModel.launchApp(it) },
                    onBrowseApps = onOpenAppList
                )
            }
        }

        // Permission Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Storage Permission Required") }
                , text = {
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
                },
                containerColor = VaultSurface,
                titleContentColor = VaultText,
                textContentColor = VaultText.copy(alpha = 0.7f)
            )
        }

        // Delete Confirmation Dialog
        showDeleteDialog?.let { item ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete File?") },
                text = { Text("Are you sure you want to remove \"${item.name}\" from your vault?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteItem(item)
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = VaultPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
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
private fun FilesTab(
    items: List<VaultItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onOpenFile: (Long) -> Unit,
    onAddFile: () -> Unit,
    onDeleteFile: (VaultItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search files...", color = VaultText.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = VaultText.copy(alpha = 0.5f))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = VaultText)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = VaultText,
                unfocusedTextColor = VaultText,
                focusedBorderColor = VaultPrimary,
                unfocusedBorderColor = VaultText.copy(alpha = 0.3f),
                cursorColor = VaultPrimary,
                focusedContainerColor = VaultSurface,
                unfocusedContainerColor = VaultSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Add button
        Button(
            onClick = onAddFile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        text = if (searchQuery.isNotEmpty()) "No files found" else "No hidden files yet",
                        color = VaultText.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search" else "Tap above to add files",
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
                    FileItem(
                        item = item,
                        onClick = { onOpenFile(item.id) },
                        onDelete = { onDeleteFile(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileItem(
    item: VaultItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = VaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on type
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
                        VaultItemType.APP, VaultItemType.AUDIO, VaultItemType.OTHER -> Icons.Default.Android
                    },
                    contentDescription = null,
                    tint = VaultPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = VaultText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = formatFileSize(item.size),
                        color = VaultText.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = " • ",
                        color = VaultText.copy(alpha = 0.3f)
                    )
                    Text(
                        text = formatDate(item.dateAdded),
                        color = VaultText.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = VaultText)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = VaultPrimary) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = VaultPrimary) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppsTab(
    hiddenApps: List<com.gamevault.domain.model.HiddenApp>,
    onUnhideApp: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onBrowseApps: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Browse Apps Button at top
        Button(
            onClick = onBrowseApps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
        ) {
            Icon(Icons.Default.Apps, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse & Hide Apps")
        }

        if (hiddenApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                    Text(
                        "Tap button above to browse apps",
                        color = VaultText.copy(alpha = 0.3f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hiddenApps) { app ->
                    HiddenAppItem(
                        app = app,
                        onLaunch = { onLaunchApp(app.packageName) },
                        onUnhide = { onUnhideApp(app.packageName) }
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
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLaunch),
        colors = CardDefaults.cardColors(containerColor = VaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VaultSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    tint = VaultPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    color = VaultText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    color = VaultText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = VaultText)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Launch") },
                        onClick = {
                            showMenu = false
                            onLaunch()
                        },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Unhide", color = VaultPrimary) },
                        onClick = {
                            showMenu = false
                            onUnhide()
                        },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = VaultPrimary) }
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}