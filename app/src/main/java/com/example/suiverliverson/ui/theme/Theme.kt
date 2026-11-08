package com.example.suiverliverson.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LiversonColors = lightColorScheme(
    primary = Red,
    secondary = Purple,
    tertiary = Green,
    background = AppBackground,
    surface = AppSurface,
    onBackground = White,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray
)

@Composable
fun LiversonTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LiversonColors, content = content)
}
