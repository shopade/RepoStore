package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DiceTheme
import com.example.model.RollHistoryItem
import com.example.model.ShakeSensitivity
import com.example.util.SoundEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class DiceViewModel : ViewModel() {

    private val _diceCount = MutableStateFlow(2) // Default 2 dice
    val diceCount: StateFlow<Int> = _diceCount.asStateFlow()

    private val _diceValues = MutableStateFlow(listOf(6, 6))
    val diceValues: StateFlow<List<Int>> = _diceValues.asStateFlow()

    private val _rollingValues = MutableStateFlow(listOf(6, 6))
    val rollingValues: StateFlow<List<Int>> = _rollingValues.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    private val _rollHistory = MutableStateFlow<List<RollHistoryItem>>(emptyList())
    val rollHistory: StateFlow<List<RollHistoryItem>> = _rollHistory.asStateFlow()

    private val _selectedTheme = MutableStateFlow(DiceTheme.CLASSIC_WHITE)
    val selectedTheme: StateFlow<DiceTheme> = _selectedTheme.asStateFlow()

    private val _shakeSensitivity = MutableStateFlow(ShakeSensitivity.MEDIUM)
    val shakeSensitivity: StateFlow<ShakeSensitivity> = _shakeSensitivity.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showStats = MutableStateFlow(false)
    val showStats: StateFlow<Boolean> = _showStats.asStateFlow()

    private val _totalRollsCount = MutableStateFlow(0)
    val totalRollsCount: StateFlow<Int> = _totalRollsCount.asStateFlow()

    fun rollDice(onLandingHaptic: () -> Unit = {}, soundEffects: SoundEffects? = null) {
        if (_isRolling.value) return

        viewModelScope.launch {
            _isRolling.value = true

            val currentCount = _diceCount.value
            val animationTicks = 12
            val tickDelayMs = 40L // Total 480 ~ 500ms duration

            for (i in 0 until animationTicks) {
                // Generate temporary random values during rolling animation
                val tempValues = List(currentCount) { Random.nextInt(1, 7) }
                _rollingValues.value = tempValues

                if (_soundEnabled.value && i % 3 == 0) {
                    soundEffects?.playRollClick()
                }

                delay(tickDelayMs)
            }

            // Reveal final random result
            val finalResult = List(currentCount) { Random.nextInt(1, 7) }
            _diceValues.value = finalResult
            _rollingValues.value = finalResult

            // Record history
            val newItem = RollHistoryItem(diceValues = finalResult)
            val updatedHistory = listOf(newItem) + _rollHistory.value
            _rollHistory.value = updatedHistory.take(25) // Keep last 25 rolls

            _totalRollsCount.value += 1
            _isRolling.value = false

            // Feedback
            if (_hapticsEnabled.value) {
                onLandingHaptic()
            }
            if (_soundEnabled.value) {
                soundEffects?.playLandingSound()
            }
        }
    }

    fun setDiceCount(count: Int) {
        if (count in 1..4 && count != _diceCount.value) {
            _diceCount.value = count
            val initial = List(count) { Random.nextInt(1, 7) }
            _diceValues.value = initial
            _rollingValues.value = initial
        }
    }

    fun setTheme(theme: DiceTheme) {
        _selectedTheme.value = theme
    }

    fun setSensitivity(sensitivity: ShakeSensitivity) {
        _shakeSensitivity.value = sensitivity
    }

    fun toggleHaptics() {
        _hapticsEnabled.value = !_hapticsEnabled.value
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    fun clearHistory() {
        _rollHistory.value = emptyList()
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun setShowStats(show: Boolean) {
        _showStats.value = show
    }
}
