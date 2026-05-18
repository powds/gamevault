package com.gamevault.presentation.vault

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gamevault.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternSetupScreen(
    onPatternSet: () -> Unit,
    onBack: () -> Unit
) {
    var pattern by remember { mutableStateOf(listOf<Offset>()) }
    var message by remember { mutableStateOf("Draw your secret pattern to unlock vault") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("Set Unlock Pattern", color = VaultText) },
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
                        text = message,
                        color = VaultText,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PatternGrid(
                        pattern = pattern,
                        onPatternChange = { newPattern ->
                            pattern = newPattern
                            if (newPattern.size >= 4) {
                                message = "Pattern set! Tap confirm to save."
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pattern = emptyList() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VaultText)
                        ) {
                            Text("Clear")
                        }

                        Button(
                            onClick = onPatternSet,
                            enabled = pattern.size >= 4,
                            colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternGrid(
    pattern: List<Offset>,
    onPatternChange: (List<Offset>) -> Unit
) {
    val dotPositions = remember {
        val positions = mutableListOf<Offset>()
        for (row in 0..2) {
            for (col in 0..2) {
                positions.add(Offset(col.toFloat(), row.toFloat()))
            }
        }
        positions
    }

    var selectedDots by remember { mutableStateOf(setOf<Int>()) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .size(280.dp)
            .background(VaultSurface, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cellSize = size.width / 3f
                            val col = (offset.x / cellSize).toInt().coerceIn(0, 2)
                            val row = (offset.y / cellSize).toInt().coerceIn(0, 2)
                            val index = row * 3 + col
                            selectedDots = setOf(index)
                        },
                        onDrag = { change, _ ->
                            val cellSize = size.width / 3f
                            val col = (change.position.x / cellSize).toInt().coerceIn(0, 2)
                            val row = (change.position.y / cellSize).toInt().coerceIn(0, 2)
                            val index = row * 3 + col
                            if (!selectedDots.contains(index)) {
                                selectedDots = selectedDots + index
                            }
                        },
                        onDragEnd = {
                            val path = selectedDots.map { dotPositions[it] }
                            onPatternChange(path)
                            selectedDots = emptySet()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / 3f
                
                // Draw dots
                dotPositions.forEachIndexed { index, pos ->
                    val isSelected = selectedDots.contains(index)
                    drawCircle(
                        color = if (isSelected) VaultPrimary else VaultText.copy(alpha = 0.3f),
                        radius = 12.dp.toPx(),
                        center = Offset(pos.x * cellSize + cellSize / 2, pos.y * cellSize + cellSize / 2)
                    )
                }

                // Draw lines
                if (selectedDots.size >= 2) {
                    val path = Path()
                    val sorted = selectedDots.sorted().map { dotPositions[it] }
                    sorted.forEachIndexed { index, pos ->
                        val center = Offset(pos.x * cellSize + cellSize / 2, pos.y * cellSize + cellSize / 2)
                        if (index == 0) {
                            path.moveTo(center.x, center.y)
                        } else {
                            path.lineTo(center.x, center.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = VaultPrimary,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}