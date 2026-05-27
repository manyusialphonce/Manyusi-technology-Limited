package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFFFB74D),
    tertiary = Color(0xFF64B5F6),
    background = CropDarkBg,
    surface = CropDarkSurface,
    onPrimary = Color(0xFF0D2510),
    onSecondary = Color(0xFF3E1200),
    onBackground = Color(0xFFE3EDE2),
    onSurface = Color(0xFFE3EDE2),
    outline = CropDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AgriGreen,
    secondary = AgriOrange,
    tertiary = AgriBlue,
    background = CropLightBg,
    surface = CropLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1B241A),
    onSurface = Color(0xFF1B241A),
    outline = CropLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
