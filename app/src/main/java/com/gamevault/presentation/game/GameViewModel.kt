package com.gamevault.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.data.repository.VaultRepository
import com.gamevault.domain.model.Direction
import com.gamevault.domain.model.GameState
import com.gamevault.domain.usecase.GameEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _isPatternSet = MutableStateFlow(false)
    val isPatternSet: StateFlow<Boolean> = _isPatternSet.asStateFlow()

    // Corner tap detection for vault unlock
    private val cornerTapSequence = mutableListOf<String>()
    private val requiredSequence = listOf("TL", "TR", "BL", "BR")

    init {
        startNewGame()
        checkPatternSet()
    }

    private fun checkPatternSet() {
        _isPatternSet.value = vaultRepository.isPatternSet() || vaultRepository.isPinSet()
    }

    fun startNewGame() {
        _gameState.value = gameEngine.initializeGame()
        loadBestScore()
    }

    private fun loadBestScore() {
        val bestScore = vaultRepository.getBestScore()
        _gameState.value = _gameState.value.copy(bestScore = bestScore)
    }

    fun onSwipe(direction: Direction) {
        val newState = gameEngine.move(_gameState.value, direction)
        _gameState.value = newState

        if (newState.score > newState.bestScore) {
            _gameState.value = newState.copy(bestScore = newState.score)
            viewModelScope.launch {
                vaultRepository.saveBestScore(newState.score)
            }
        }
    }

    fun onTileTap(row: Int, col: Int) {
        val corner = when {
            row == 0 && col == 0 -> "TL"
            row == 0 && col == 3 -> "TR"
            row == 3 && col == 0 -> "BL"
            row == 3 && col == 3 -> "BR"
            else -> null
        }

        if (corner != null) {
            cornerTapSequence.add(corner)

            if (cornerTapSequence.takeLast(4) == requiredSequence) {
                if (vaultRepository.isPatternSet() || vaultRepository.isPinSet()) {
                    _isVaultUnlocked.value = true
                } else {
                    _isVaultUnlocked.value = true
                }
                cornerTapSequence.clear()
            }

            while (cornerTapSequence.size > 4) {
                cornerTapSequence.removeAt(0)
            }
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        cornerTapSequence.clear()
    }

    fun unlockVault() {
        _isVaultUnlocked.value = true
    }

    fun verifyPin(pin: String): Boolean {
        return vaultRepository.verifyPin(pin)
    }

    fun setPin(pin: String) {
        vaultRepository.setPin(pin)
        checkPatternSet()
    }
}