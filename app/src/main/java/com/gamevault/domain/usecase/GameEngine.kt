package com.gamevault.domain.usecase

import com.gamevault.domain.model.Direction
import com.gamevault.domain.model.GameState
import javax.inject.Inject
import kotlin.random.Random

class GameEngine @Inject constructor() {

    fun initializeGame(): GameState {
        val grid = createEmptyGrid().toMutableList()
        var state = GameState(grid = grid)
        state = addRandomTile(state)
        state = addRandomTile(state)
        return state
    }

    private fun createEmptyGrid(): List<List<Int>> {
        return List(4) { List(4) { 0 } }
    }

    fun move(state: GameState, direction: Direction): GameState {
        if (state.isGameOver) return state

        var grid = state.grid.map { it.toMutableList() }.toMutableList()
        var score = state.score
        var moved = false

        // Rotate grid based on direction to simplify merging logic
        // We always merge left-to-right by rotating the grid
        when (direction) {
            Direction.LEFT -> {
                for (row in grid.indices) {
                    val merged = mergeRow(grid[row])
                    if (merged != grid[row]) moved = true
                    grid[row] = merged.toMutableList()
                }
            }
            Direction.RIGHT -> {
                for (row in grid.indices) {
                    val reversed = grid[row].reversed().toMutableList()
                    val merged = mergeRow(reversed)
                    if (merged != reversed) moved = true
                    grid[row] = merged.reversed().toMutableList()
                }
            }
            Direction.UP -> {
                val rotated = rotateCounterClockwise(grid)
                for (row in rotated.indices) {
                    val merged = mergeRow(rotated[row])
                    if (merged != rotated[row]) moved = true
                    rotated[row] = merged.toMutableList()
                }
                grid = rotateClockwise(rotated)
            }
            Direction.DOWN -> {
                val rotated = rotateClockwise(grid)
                for (row in rotated.indices) {
                    val merged = mergeRow(rotated[row])
                    if (merged != rotated[row]) moved = true
                    rotated[row] = merged.toMutableList()
                }
                grid = rotateCounterClockwise(rotated)
            }
        }

        var newState = state.copy(grid = grid, score = score)

        if (moved) {
            newState = addRandomTile(newState)
        }

        newState = checkGameOver(newState)

        // Check for win (2048 tile)
        newState = if (newState.grid.flatten().contains(2048) && !newState.hasWon) {
            newState.copy(hasWon = true)
        } else {
            newState
        }

        return newState
    }

    private fun mergeRow(row: MutableList<Int>): List<Int> {
        // Remove zeros
        val filtered = row.filter { it != 0 }.toMutableList()

        // Merge adjacent same values from left to right
        var i = 0
        while (i < filtered.size - 1) {
            if (filtered[i] == filtered[i + 1]) {
                filtered[i] *= 2
                filtered.removeAt(i + 1)
            }
            i++
        }

        // Pad with zeros to maintain length of 4
        while (filtered.size < 4) {
            filtered.add(0)
        }

        return filtered
    }

    private fun rotateClockwise(grid: List<List<Int>>): MutableList<MutableList<Int>> {
        val n = grid.size
        val result = MutableList(n) { MutableList(n) { 0 } }
        for (row in 0 until n) {
            for (col in 0 until n) {
                result[col][n - 1 - row] = grid[row][col]
            }
        }
        return result
    }

    private fun rotateCounterClockwise(grid: List<List<Int>>): MutableList<MutableList<Int>> {
        val n = grid.size
        val result = MutableList(n) { MutableList(n) { 0 } }
        for (row in 0 until n) {
            for (col in 0 until n) {
                result[n - 1 - col][row] = grid[row][col]
            }
        }
        return result
    }

    private fun addRandomTile(state: GameState): GameState {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (row in state.grid.indices) {
            for (col in state.grid[row].indices) {
                if (state.grid[row][col] == 0) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }

        if (emptyCells.isEmpty()) return state

        val (row, col) = emptyCells.random()
        val value = if (Random.nextInt(100) < 90) 2 else 4

        val newGrid = state.grid.map { it.toMutableList() }.toMutableList()
        newGrid[row][col] = value

        return state.copy(grid = newGrid)
    }

    private fun checkGameOver(state: GameState): GameState {
        // Check for empty cells
        for (row in state.grid) {
            if (row.contains(0)) return state.copy(isGameOver = false)
        }

        // Check for possible merges horizontally
        for (row in state.grid.indices) {
            for (col in 0 until 3) {
                if (state.grid[row][col] == state.grid[row][col + 1]) {
                    return state.copy(isGameOver = false)
                }
            }
        }

        // Check for possible merges vertically
        for (col in 0 until 4) {
            for (row in 0 until 3) {
                if (state.grid[row][col] == state.grid[row + 1][col]) {
                    return state.copy(isGameOver = false)
                }
            }
        }

        return state.copy(isGameOver = true)
    }

    fun resetGame(): GameState {
        return initializeGame()
    }
}