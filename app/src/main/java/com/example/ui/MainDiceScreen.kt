package com.example.ui

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.RollHistoryItem
import com.example.sensor.ShakeDetector
import com.example.ui.components.DiceFace
import com.example.ui.components.RollHistoryCard
import com.example.ui.components.SettingsSheet
import com.example.ui.components.StatsSheet
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundEffects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDiceScreen(
    viewModel: DiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val diceCount by viewModel.diceCount.collectAsState()
    val diceValues by viewModel.diceValues.collectAsState()
    val rollingValues by viewModel.rollingValues.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val rollHistory by viewModel.rollHistory.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val sensitivity by viewModel.shakeSensitivity.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showStats by viewModel.showStats.collectAsState()
    val totalRollsCount by viewModel.totalRollsCount.collectAsState()

    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Vibrator Service Setup
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerLandingHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(60L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60L)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails
        }
    }

    // Sound Pool Generator
    val soundEffects = remember { SoundEffects() }
    DisposableEffect(Unit) {
        onDispose {
            soundEffects.release()
        }
    }

    // Shake Detector Sensor Setup
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val shakeDetector = remember(sensitivity) {
        ShakeDetector(
            threshold = sensitivity.threshold,
            minIntervalMs = 800L,
            onShake = {
                viewModel.rollDice(
                    onLandingHaptic = { triggerLandingHaptic() },
                    soundEffects = soundEffects
                )
            }
        )
    }

    DisposableEffect(lifecycleOwner, sensitivity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                shakeDetector.register(sensorManager)
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                shakeDetector.unregister(sensorManager)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            shakeDetector.unregister(sensorManager)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val displayedValues = if (isRolling) rollingValues else diceValues
    val totalSum = displayedValues.sum()
    val isDouble = displayedValues.size > 1 && displayedValues.all { it == displayedValues.first() }

    val statusPadding = WindowInsets.statusBars.asPaddingValues()
    val navPadding = WindowInsets.navigationBars.asPaddingValues()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkNavyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkNavyBackground,
                            DarkNavySurface,
                            DarkNavyBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // TOP BAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkNavySurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "ShakeDice Icon",
                                tint = AccentBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ShakeDice",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Shake to Roll",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setShowStats(true) },
                            modifier = Modifier.testTag("stats_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "View Statistics",
                                tint = AccentCyan
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setShowSettings(true) },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // MAIN INTERACTIVE DICE CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavySurface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.rollDice(
                                onLandingHaptic = { triggerLandingHaptic() },
                                soundEffects = soundEffects
                            )
                        }
                        .padding(20.dp)
                        .testTag("dice_container"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // DICE DISPLAY
                        when (diceCount) {
                            1 -> {
                                DiceFace(
                                    value = displayedValues.getOrElse(0) { 6 },
                                    theme = selectedTheme,
                                    isRolling = isRolling,
                                    size = 170.dp
                                )
                            }
                            2 -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DiceFace(
                                        value = displayedValues.getOrElse(0) { 6 },
                                        theme = selectedTheme,
                                        isRolling = isRolling,
                                        size = 135.dp
                                    )
                                    DiceFace(
                                        value = displayedValues.getOrElse(1) { 6 },
                                        theme = selectedTheme,
                                        isRolling = isRolling,
                                        size = 135.dp
                                    )
                                }
                            }
                            3 -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DiceFace(
                                            value = displayedValues.getOrElse(0) { 6 },
                                            theme = selectedTheme,
                                            isRolling = isRolling,
                                            size = 100.dp
                                        )
                                        DiceFace(
                                            value = displayedValues.getOrElse(1) { 6 },
                                            theme = selectedTheme,
                                            isRolling = isRolling,
                                            size = 100.dp
                                        )
                                    }
                                    DiceFace(
                                        value = displayedValues.getOrElse(2) { 6 },
                                        theme = selectedTheme,
                                        isRolling = isRolling,
                                        size = 100.dp
                                    )
                                }
                            }
                            4 -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DiceFace(
                                            value = displayedValues.getOrElse(0) { 6 },
                                            theme = selectedTheme,
                                            isRolling = isRolling,
                                            size = 100.dp
                                        )
                                        DiceFace(
                                            value = displayedValues.getOrElse(1) { 6 },
                                            theme = selectedTheme,
                                            isRolling = isRolling,
                                            size = 100.dp
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DiceFace(
                                            value = displayedValues.getOrElse(2) { 6 },
                                            theme = selectedTheme,
                                            isRolling = isRolling,
                                            size = 100.dp
                                        )
                                        DiceFace(
                                            value = displayedValues.getOrElse(3) { 6 },
                                            theme = selectedTheme,
                                            isRolling = isRolling,
                                            size = 100.dp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // SUM TOTAL BADGE
                        AnimatedVisibility(
                            visible = !isRolling,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(
                                            if (isDouble) AccentGold.copy(alpha = 0.2f)
                                            else DarkNavySurfaceVariant
                                        )
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Total: $totalSum" + if (displayedValues.size > 1) " (${displayedValues.joinToString(" + ")})" else "",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDouble) AccentGold else TextPrimary
                                    )
                                }

                                if (isDouble && displayedValues.size > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "★ DOUBLE ${displayedValues.first()}s! ★",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SHAKE HINT BANNER & ROLL BUTTON
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_shake")
                    val alphaPulse by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Text(
                        text = "📱 Shake phone or tap button to roll",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary.copy(alpha = alphaPulse),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.rollDice(
                                onLandingHaptic = { triggerLandingHaptic() },
                                soundEffects = soundEffects
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("roll_button"),
                        enabled = !isRolling,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isRolling) "Rolling..." else "ROLL DICE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ROLL HISTORY SECTION
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT ROLLS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            letterSpacing = 1.sp
                        )

                        if (rollHistory.isNotEmpty()) {
                            Text(
                                text = "Clear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted,
                                modifier = Modifier
                                    .clickable { viewModel.clearHistory() }
                                    .padding(4.dp)
                                    .testTag("clear_history_button")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (rollHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkNavyCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No rolls yet. Give it a shake!",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            itemsIndexed(
                                items = rollHistory,
                                key = { _, item -> item.id }
                            ) { idx, item ->
                                RollHistoryCard(
                                    item = item,
                                    theme = selectedTheme,
                                    index = rollHistory.size - idx
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Settings Modal Bottom Sheet
    if (showSettings) {
        SettingsSheet(
            sheetState = settingsSheetState,
            diceCount = diceCount,
            selectedTheme = selectedTheme,
            sensitivity = sensitivity,
            hapticsEnabled = hapticsEnabled,
            soundEnabled = soundEnabled,
            onDiceCountChanged = { viewModel.setDiceCount(it) },
            onThemeChanged = { viewModel.setTheme(it) },
            onSensitivityChanged = { viewModel.setSensitivity(it) },
            onHapticsToggled = { viewModel.toggleHaptics() },
            onSoundToggled = { viewModel.toggleSound() },
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { viewModel.setShowSettings(false) }
        )
    }

    // Stats Modal Bottom Sheet
    if (showStats) {
        StatsSheet(
            sheetState = statsSheetState,
            history = rollHistory,
            totalRollsCount = totalRollsCount,
            onDismiss = { viewModel.setShowStats(false) }
        )
    }
}
