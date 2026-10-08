package com.mmocalendar.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Bg = Color(0xFF0B0E1A)
private val Surface = Color(0xFF141927)
private val Surface2 = Color(0xFF1D2440)
private val Accent = Color(0xFF8B5CF6)
private val Accent2 = Color(0xFF22D3EE)
private val Gold = Color(0xFFFBBF24)

private val Scheme = darkColorScheme(
    primary = Accent,
    secondary = Accent2,
    tertiary = Gold,
    background = Bg,
    surface = Surface,
    surfaceVariant = Surface2,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MMOTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
