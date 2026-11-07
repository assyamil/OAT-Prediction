package com.example.oatprediction.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Emerald, onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EEE5), onPrimaryContainer = Emerald,
    secondary = EmeraldAccent, onSecondary = Color.White,
    secondaryContainer = Color(0xFFA1F3D5), onSecondaryContainer = Color(0xFF00382A),
    tertiary = Color(0xFF006C46), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC9F9E2), onTertiaryContainer = Emerald,
    background = MintBackground, onBackground = Ink, surface = MintBackground, onSurface = Ink,
    surfaceVariant = Color(0xFFDCEFE7), onSurfaceVariant = Color(0xFF43564E),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF5FCF9),
    surfaceContainer = Color(0xFFE1F9EF), surfaceContainerHigh = Color(0xFFD6EDE4),
    surfaceContainerHighest = Color(0xFFD0E8DE),
    outline = Color(0xFF6F7974), outlineVariant = Color(0xFFD3E5DE),
    error = Color(0xFFBA1A1A), onError = Color.White,
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF85D6B9), onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513E), onPrimaryContainer = Color(0xFFA1F3D5),
    secondary = Color(0xFF71E3AD), onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF174B37), onSecondaryContainer = Color(0xFFB0F5D0),
    tertiary = Color(0xFF88DAAE), onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF204C39), onTertiaryContainer = Color(0xFFC9F9E2),
    background = Color(0xFF0B1C16), onBackground = Color(0xFFE0F3E9),
    surface = Color(0xFF0B1C16), onSurface = Color(0xFFE0F3E9),
    surfaceVariant = Color(0xFF2C4238), onSurfaceVariant = Color(0xFFBACDC2),
    surfaceContainerLowest = Color(0xFF142B21), surfaceContainerLow = Color(0xFF183025),
    surfaceContainer = Color(0xFF20382D), surfaceContainerHigh = Color(0xFF284236),
    surfaceContainerHighest = Color(0xFF304B3E),
    outline = Color(0xFF82988B), outlineVariant = Color(0xFF365346),
)

@Composable
fun OATPredictionTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(20.dp)),
        content = content,
    )
}
