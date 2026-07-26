package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DiceClassicDots
import com.example.ui.theme.DiceClassicWhite
import com.example.ui.theme.DiceCrimsonBg
import com.example.ui.theme.DiceCyberpunkBg
import com.example.ui.theme.DiceCyanDots
import com.example.ui.theme.DiceGoldDots
import com.example.ui.theme.DiceObsidianBg
import com.example.ui.theme.DiceRedDots
import com.example.ui.theme.DiceWhiteDots

enum class DiceTheme(
    val displayName: String,
    val backgroundColor: Color,
    val pipColor: Color,
    val centerPipColor: Color,
    val borderColor: Color
) {
    CLASSIC_WHITE(
        displayName = "Classic White",
        backgroundColor = DiceClassicWhite,
        pipColor = DiceClassicDots,
        centerPipColor = DiceRedDots, // Red pip for single dot or center dot (Vegas style)
        borderColor = Color(0xFFE2E8F0)
    ),
    CRIMSON_GOLD(
        displayName = "Crimson Gold",
        backgroundColor = DiceCrimsonBg,
        pipColor = DiceGoldDots,
        centerPipColor = DiceGoldDots,
        borderColor = Color(0xFF7F1D1D)
    ),
    OBSIDIAN_NIGHT(
        displayName = "Obsidian Dark",
        backgroundColor = DiceObsidianBg,
        pipColor = DiceWhiteDots,
        centerPipColor = AccentBlue,
        borderColor = Color(0xFF27272A)
    ),
    CYBER_NEON(
        displayName = "Cyberpunk Cyan",
        backgroundColor = DiceCyberpunkBg,
        pipColor = DiceCyanDots,
        centerPipColor = AccentCyan,
        borderColor = Color(0xFF0284C7)
    ),
    VEGAS_EMERALD(
        displayName = "Vegas Emerald",
        backgroundColor = Color(0xFF064E3B),
        pipColor = Color(0xFFFDE047),
        centerPipColor = Color(0xFFF59E0B),
        borderColor = Color(0xFF022C22)
    )
}

enum class ShakeSensitivity(
    val displayName: String,
    val threshold: Float
) {
    HIGH("Gentle (Sensitive)", 11.0f),
    MEDIUM("Standard (Recommended)", 13.5f),
    LOW("Firm Shake", 16.5f)
}

data class RollHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val diceValues: List<Int>,
    val total: Int = diceValues.sum(),
    val isDouble: Boolean = diceValues.size > 1 && diceValues.all { it == diceValues.first() },
    val isMaxRoll: Boolean = diceValues.all { it == 6 }
)
