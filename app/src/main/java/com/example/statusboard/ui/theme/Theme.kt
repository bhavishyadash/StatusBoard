package com.example.statusboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// App-level theme mode
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

// Dark mode palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4C7DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1D2B4D),
    onPrimaryContainer = Color(0xFFE0EAFF),

    secondary = Color(0xFF22C55E),
    onSecondary = Color.Black,

    background = Color(0xFF050509),
    onBackground = Color(0xFFE5E7EB),

    surface = Color(0xFF111318),
    onSurface = Color(0xFFE5E7EB),

    error = Color(0xFFFF4B61),
    onError = Color.White
)

// Light mode palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4C7DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EAFF),
    onPrimaryContainer = Color(0xFF00174C),

    secondary = Color(0xFF22C55E),
    onSecondary = Color.White,

    background = Color(0xFFF4F4F6),
    onBackground = Color(0xFF020617),

    surface = Color.White,
    onSurface = Color(0xFF020617),

    error = Color(0xFFB91C1C),
    onError = Color.White
)

@Composable
fun StatusBoardTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}