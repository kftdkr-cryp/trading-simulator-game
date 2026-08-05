package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = SecondarySlate,
    background = DarkNavyBg,
    surface = SecondarySlate,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = DangerRed
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    secondary = SecondarySlate,
    background = DarkNavyBg,
    surface = SecondarySlate,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = DangerRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for premium trading look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
