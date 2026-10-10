package com.nour.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Emerald = Color(0xFF0B4F3C)
val EmeraldLight = Color(0xFF1F7A5C)
val Gold = Color(0xFFC9A24A)
val Cream = Color(0xFFF8F1E4)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EBDF),
    onPrimaryContainer = Color(0xFF00281C),
    secondary = Gold,
    onSecondary = Color(0xFF2B2000),
    secondaryContainer = Color(0xFFF5E3B5),
    onSecondaryContainer = Color(0xFF3A2C00),
    background = Cream,
    onBackground = Color(0xFF1C1B17),
    surface = Cream,
    onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFEDE3D0),
    surfaceContainer = Color(0xFFF1E8D6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD6B3),
    onPrimary = Color(0xFF003827),
    primaryContainer = Emerald,
    onPrimaryContainer = Color(0xFFD3EBDF),
    secondary = Color(0xFFE8C26A),
    onSecondary = Color(0xFF3A2C00),
    secondaryContainer = Color(0xFF574400),
    onSecondaryContainer = Color(0xFFF5E3B5),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF0F1513),
    onSurface = Color(0xFFE1E3DF),
)

@Composable
fun NourTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
