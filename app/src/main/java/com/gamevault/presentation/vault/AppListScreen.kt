package com.gamevault.presentation.vault

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.domain.model.HiddenApp
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    val hiddenPackages = hiddenApps.map { it.packageName }.toSet()

    LaunchedEffect(Unit) {
        viewModel.loadApps()
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

            if (installedApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VaultPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(installedApps) { app ->
                        AppListItem(
                            app = app,
                            isHidden = hiddenPackages.contains(app.packageName),
                            onHide = { viewModel.hideApp(app) },
                            onUnhide = { viewModel.unhideApp(app.packageName) },
                            onLaunch = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: ApplicationInfo,
    isHidden: Boolean,
    onHide: () -> Unit,
    onUnhide: () -> Unit,
    onLaunch: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pm = context.packageManager

    var appIcon by remember { mutableStateOf<Drawable?>(null) }
    
    LaunchedEffect(app) {
        try {
            appIcon = app.loadIcon(pm)
        } catch (e: Exception) {
            appIcon = null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHidden) VaultPrimary.copy(alpha = 0.1f) else VaultSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            Box(modifier = Modifier.size(40.dp)) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon!!.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        tint = VaultPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.loadLabel(pm).toString(),
                    color = VaultText
                )
                Text(
                    text = app.packageName,
                    color = VaultText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isHidden) {
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
            } else {
                IconButton(onClick = onHide) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = "Hide",
                        tint = VaultText.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}