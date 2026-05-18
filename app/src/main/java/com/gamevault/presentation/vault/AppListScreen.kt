package com.gamevault.presentation.vault

import android.content.pm.ApplicationInfo
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.domain.model.HiddenApp
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val lockedApps by viewModel.lockedApps.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showLockedOnly by remember { mutableStateOf(false) }

    val filteredApps = remember(installedApps, showLockedOnly, searchQuery) {
        installedApps.filter { app ->
            val pm = context.packageManager
            val appName = app.loadLabel(pm).toString()
            val matchesSearch = searchQuery.isEmpty() || appName.lowercase().contains(searchQuery.lowercase())
            val matchesLocked = !showLockedOnly || lockedApps.contains(app.packageName)
            matchesSearch && matchesLocked
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
                title = { Text("All Apps", color = VaultText) },
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
                    IconButton(onClick = { showLockedOnly = !showLockedOnly }) {
                        Icon(
                            if (showLockedOnly) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (showLockedOnly) "Show all" else "Show locked",
                            tint = if (showLockedOnly) VaultPrimary else VaultText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VaultSurface
                )
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.search(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps...", color = VaultText.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = VaultText.copy(alpha = 0.5f))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
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

            // Stats bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(
                    icon = Icons.Default.VisibilityOff,
                    text = "${hiddenApps.size} Hidden",
                    isActive = false
                )
                StatChip(
                    icon = Icons.Default.Lock,
                    text = "${lockedApps.size} Locked",
                    isActive = true
                )
                StatChip(
                    icon = Icons.Default.Apps,
                    text = "${installedApps.size} Total",
                    isActive = false
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VaultPrimary)
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = VaultText.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No apps found" else "No apps available",
                            color = VaultText.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        AppListItem(
                            app = app,
                            isHidden = hiddenApps.any { it.packageName == app.packageName },
                            isLocked = lockedApps.contains(app.packageName),
                            onHide = { viewModel.hideApp(app) },
                            onUnhide = { viewModel.unhideApp(app.packageName) },
                            onLock = { viewModel.lockApp(app.packageName) },
                            onUnlock = { viewModel.unlockApp(app.packageName) },
                            onLaunch = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isActive: Boolean
) {
    Surface(
        color = if (isActive) VaultPrimary.copy(alpha = 0.2f) else VaultSurface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isActive) VaultPrimary else VaultText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) VaultPrimary else VaultText.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AppListItem(
    app: ApplicationInfo,
    isHidden: Boolean,
    isLocked: Boolean,
    onHide: () -> Unit,
    onUnhide: () -> Unit,
    onLock: () -> Unit,
    onUnlock: () -> Unit,
    onLaunch: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val pm = context.packageManager
    val appName = app.loadLabel(pm).toString()

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
            // App icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isHidden) VaultPrimary.copy(alpha = 0.3f) else VaultSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isHidden) VaultPrimary else VaultText
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appName,
                        color = VaultText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isHidden) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(text = "Hidden", color = VaultPrimary)
                    }
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(text = "Locked", color = VaultPrimary)
                    }
                }
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
                    if (isHidden) {
                        DropdownMenuItem(
                            text = { Text("Unhide") },
                            onClick = {
                                showMenu = false
                                onUnhide()
                            },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Hide") },
                            onClick = {
                                showMenu = false
                                onHide()
                            },
                            leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) }
                        )
                    }
                    if (isLocked) {
                        DropdownMenuItem(
                            text = { Text("Unlock App") },
                            onClick = {
                                showMenu = false
                                onUnlock()
                            },
                            leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Lock App") },
                            onClick = {
                                showMenu = false
                                onLock()
                            },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}