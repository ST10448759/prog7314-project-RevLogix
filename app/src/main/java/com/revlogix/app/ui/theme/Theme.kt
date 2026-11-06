package com.revlogix.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// RevLogix always uses this dark, orange-accented scheme regardless of
// system light/dark setting, matching the fixed automotive look in Part 1.
private val RevLogixColorScheme = darkColorScheme(
    primary = OrangeAccent,
    onPrimary = TextPrimary,
    secondary = OrangeAccentDark,
    onSecondary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun RevLogixTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = RevLogixColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as android.app.Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}