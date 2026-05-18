package com.gamevault.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamevault.domain.model.Direction
import com.gamevault.presentation.common.theme.*
import kotlin.math.abs

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onUnlockVault: () -> Unit,
    onSetupPattern: () -> Unit,
    onSetupPin: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()

    LaunchedEffect(isVaultUnlocked) {
        if (isVaultUnlocked) {
            onUnlockVault()
            viewModel.lockVault()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeader(
                score = gameState.score,
                bestScore = gameState.bestScore,
                onNewGame = { viewModel.startNewGame() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GameBoard(
                grid = gameState.grid,
                onSwipe = { viewModel.onSwipe(it) },
                onTileTap = { row, col -> viewModel.onTileTap(row, col) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Swipe to play 2048 | Tap 4 corners to unlock vault",
                style = MaterialTheme.typography.bodySmall,
                color = TextDark.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap corners: TL -> TR -> BL -> BR",
                style = MaterialTheme.typography.bodySmall,
                color = TextDark.copy(alpha = 0.4f)
            )
        }

        if (gameState.isGameOver) {
            GameOverOverlay(
                score = gameState.score,
                onNewGame = { viewModel.startNewGame() }
            )
        }
    }
}

@Composable
private fun GameHeader(
    score: Int,
    bestScore: Int,
    onNewGame: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "2048",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScoreBox(label = "SCORE", value = score)
            ScoreBox(label = "BEST", value = bestScore)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onNewGame,
        colors = ButtonDefaults.buttonColors(containerColor = GameBoard),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("New Game", color = TextLight, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScoreBox(label: String, value: Int) {
    Column(
        modifier = Modifier
            .background(GameBoard, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextLight.copy(alpha = 0.7f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextLight
        )
    }
}

@Composable
private fun GameBoard(
    grid: List<List<Int>>,
    onSwipe: (Direction) -> Unit,
    onTileTap: (Int, Int) -> Unit
) {
    val boardPadding = 8.dp
    val tileSpacing = 8.dp
    val tileSize = 80.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(GameBoard)
            .padding(boardPadding)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (dx, dy) = dragAmount
                    val absDx = abs(dx)
                    val absDy = abs(dy)

                    if (absDx > absDy) {
                        if (dx > 0) onSwipe(Direction.RIGHT)
                        else onSwipe(Direction.LEFT)
                    } else {
                        if (dy > 0) onSwipe(Direction.DOWN)
                        else onSwipe(Direction.UP)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 4f
                    val col = (offset.x / cellSize).toInt().coerceIn(0, 3)
                    val row = (offset.y / cellSize).toInt().coerceIn(0, 3)
                    onTileTap(row, col)
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(tileSpacing)
        ) {
            for (rowIndex in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tileSpacing)
                ) {
                    for (colIndex in 0 until 4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(TileEmpty),
                            contentAlignment = Alignment.Center
                        ) {
                            val value = grid.getOrNull(rowIndex)?.getOrNull(colIndex) ?: 0
                            if (value > 0) {
                                TileItem(value = value)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileItem(value: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(getTileColor(value)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = when {
                    value >= 1000 -> 20.sp
                    value >= 100 -> 24.sp
                    else -> 32.sp
                }
            ),
            fontWeight = FontWeight.Bold,
            color = getTextColor(value)
        )
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    onNewGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GameBackground)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNewGame,
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                ) {
                    Text("Try Again", color = TextLight)
                }
            }
        }
    }
}