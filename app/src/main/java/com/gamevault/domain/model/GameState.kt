package com.gamevault.domain.model

data class GameState(
    val grid: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val isUnlocked: Boolean = false
)

fun createInitialGameState() = GameState(
    grid = List(4) { List(4) { 0 } }
)