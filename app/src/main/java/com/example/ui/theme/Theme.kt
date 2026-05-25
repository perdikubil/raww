package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ArtisticOrange,
    secondary = ArtisticGold,
    tertiary = ForestSage,
    background = DarkForestNight,
    surface = Color(0xFF192C1D),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = ArtisticBg,
    onSurface = ArtisticBg,
    surfaceVariant = Color(0xFF223627),
    onSurfaceVariant = ArtisticBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ArtisticGreen,
    secondary = ArtisticOrange,
    tertiary = ArtisticGold,
    background = ArtisticBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ArtisticText,
    onSurface = ArtisticText,
    surfaceVariant = Color(0xFFF6F4E8),
    onSurfaceVariant = ArtisticGold
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamicColor by default to guarantee our campbuddy scout themes remain consistent
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
