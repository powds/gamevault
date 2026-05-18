package com.gamevault.presentation.vault

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.gamevault.domain.model.VaultItem
import com.gamevault.domain.model.VaultItemType
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    itemId: Long,
    viewModel: VaultViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()

    val item = vaultItems.find { it.id == itemId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        item?.name ?: "File Viewer",
                        color = VaultText,
                        maxLines = 1
                    )
                },
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

            if (item == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("File not found", color = VaultText.copy(alpha = 0.5f))
                }
            } else {
                when (item.type) {
                    VaultItemType.PHOTO -> PhotoViewer(path = item.path)
                    VaultItemType.VIDEO -> VideoViewer(path = item.path)
                    VaultItemType.DOCUMENT -> DocumentViewer(path = item.path)
                    VaultItemType.APP -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Android,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = VaultPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "App: ${item.name}",
                                    color = VaultText
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.launchApp(item.path) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                                ) {
                                    Text("Launch App")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoViewer(path: String) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(Uri.parse(path))
            .crossfade(true)
            .build(),
        contentDescription = "Photo",
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun VideoViewer(path: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video player placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(VaultSurface),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = VaultPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap to play video",
                    color = VaultText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = path,
            color = VaultText.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun DocumentViewer(path: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(VaultSurface),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = VaultPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "PDF Viewer",
                    color = VaultText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    path,
                    color = VaultText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Open with external PDF viewer */ },
            colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open with PDF Viewer")
        }
    }
}