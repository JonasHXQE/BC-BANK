package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = PrimaryVioletDark,
    onPrimaryContainer = PrimaryVioletLight,
    secondary = AccentGold,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCyan,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = BorderSubtle,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRedDark,
    onErrorContainer = ExpenseRedLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

