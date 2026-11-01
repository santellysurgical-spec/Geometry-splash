package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GdColorScheme = darkColorScheme(
    primary = GdCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF004D54),
    onPrimaryContainer = GdCyan,
    secondary = GdLime,
    onSecondary = Color(0xFF00220F),
    secondaryContainer = Color(0xFF004D25),
    onSecondaryContainer = GdLime,
    tertiary = GdMagenta,
    onTertiary = Color(0xFF380018),
    tertiaryContainer = Color(0xFF6B0033),
    onTertiaryContainer = GdMagenta,
    background = GdBackgroundDark,
    onBackground = GdTextPrimary,
    surface = GdSurfaceDark,
    onSurface = GdTextPrimary,
    surfaceVariant = GdSurfaceVariantDark,
    onSurfaceVariant = GdTextSecondary,
    outline = GdBorderDark,
    error = GdDemonRed,
    onError = Color.White
)

@Composable
fun GdAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GdColorScheme,
        typography = Typography,
        content = content
    )
}
