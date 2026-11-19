package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DailyPathLightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = AntiqueGold,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = GoldOnContainer,
    tertiary = EmeraldMedium,
    onTertiary = Color.White,
    background = CreamCanvas,
    onBackground = InkPrimary,
    surface = CreamElevated,
    onSurface = InkPrimary,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = InkSecondary,
    outline = CreamOutline,
    outlineVariant = Color(0xFFE7E1D4),
    error = AlertRust,
    onError = Color.White,
    errorContainer = AlertRustSoft,
    onErrorContainer = AlertRust
)

private val DailyPathDarkColorScheme = darkColorScheme(
    primary = DarkEmeraldAccent,
    onPrimary = DarkEmeraldOnPrimary,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = DarkInkPrimary,
    secondary = DarkGoldAccent,
    onSecondary = Color(0xFF281D05),
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = DarkGoldAccent,
    tertiary = EmeraldMedium,
    onTertiary = Color.White,
    background = DarkForestCanvas,
    onBackground = DarkInkPrimary,
    surface = DarkForestSurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = DarkForestElevated,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkForestOutline,
    outlineVariant = Color(0xFF1F362C),
    error = Color(0xFFF28B82),
    onError = Color(0xFF490D0D),
    errorContainer = Color(0xFF5E1B1B),
    onErrorContainer = Color(0xFFFFDAD6)
)

val DailyPathShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun DailyPathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DailyPathDarkColorScheme else DailyPathLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DailyPathShapes,
        content = content
    )
}
