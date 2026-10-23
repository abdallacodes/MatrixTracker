package com.tracker.matrixtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeMode = staticCompositionLocalOf { "DARK" }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003915),
    primaryContainer = Color(0xFF1B4D24),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFF90A4AE),
    onSecondary = Color(0xFF1E2836),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFC4C4C4),
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF546E7A),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFF8F9FA),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEEF1F4),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF00FF87),
    onPrimary = Color(0xFF00381B),
    primaryContainer = Color(0xFF00522A),
    onPrimaryContainer = Color(0xFF66FFB2),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color(0xFF00363F),
    tertiary = Color(0xFFFF007F),
    onTertiary = Color(0xFF3F001D),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF00FF87).copy(alpha = 0.5f),
    outlineVariant = Color(0xFF222222)
)

@Composable
fun MatrixTrackerTheme(
    themeMode: String = "DARK",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "LIGHT" -> LightColorScheme
        "AMOLED" -> AmoledColorScheme
        else -> DarkColorScheme
    }

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
