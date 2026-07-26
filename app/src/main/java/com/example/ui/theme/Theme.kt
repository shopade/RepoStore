package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ShakeDiceColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextPrimary,
    primaryContainer = DarkNavySurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = AccentCyan,
    onSecondary = TextPrimary,
    secondaryContainer = DarkNavyCard,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGold,
    onTertiary = DarkNavyBackground,
    background = DarkNavyBackground,
    onBackground = TextPrimary,
    surface = DarkNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    error = AccentRed
)

@Composable
fun ShakeDiceTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavyBackground.toArgb()
            window.navigationBarColor = DarkNavyBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ShakeDiceColorScheme,
        typography = Typography,
        content = content
    )
}
