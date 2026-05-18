package com.gamevault.presentation.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onPinSet: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Enter a 4-digit PIN") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("Set PIN", color = VaultText) },
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isConfirming) "Confirm your PIN" else message,
                        color = VaultText,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // PIN dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(4) { index ->
                            val currentPin = if (isConfirming) confirmPin else pin
                            PinDot(filled = index < currentPin.length)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Number pad
                    NumberPad(
                        onNumberClick = { number ->
                            if (isConfirming) {
                                if (confirmPin.length < 4) {
                                    confirmPin += number
                                    if (confirmPin.length == 4) {
                                        if (pin == confirmPin) {
                                            onPinSet()
                                        } else {
                                            message = "PINs don't match. Try again."
                                            pin = ""
                                            confirmPin = ""
                                            isConfirming = false
                                        }
                                    }
                                }
                            } else {
                                if (pin.length < 4) {
                                    pin += number
                                    if (pin.length == 4) {
                                        message = "Now confirm your PIN"
                                        isConfirming = true
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (isConfirming && confirmPin.isNotEmpty()) {
                                confirmPin = confirmPin.dropLast(1)
                            } else if (!isConfirming && pin.isNotEmpty()) {
                                pin = pin.dropLast(1)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDot(filled: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(if (filled) VaultPrimary else VaultText.copy(alpha = 0.3f))
    )
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        ).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else {
                        NumberKey(
                            key = key,
                            onClick = {
                                if (key == "⌫") {
                                    onDeleteClick()
                                } else {
                                    onNumberClick(key)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberKey(
    key: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(VaultSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            color = VaultText,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}