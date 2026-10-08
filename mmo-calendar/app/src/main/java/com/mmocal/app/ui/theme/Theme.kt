package com.mmocal.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mmocal.app.data.AccentPalette
import com.mmocal.app.data.AppStyle
import com.mmocal.app.data.ThemeMode

private fun classicScheme(accent: Color, dark: Boolean): androidx.compose.material3.ColorScheme {
    val onAccent = if (accent.luminanceCompat() > 0.5f) Color(0xFF10131A) else Color.White
    return if (dark) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accent.copy(alpha = 0.85f),
            onPrimaryContainer = Color.White,
            secondary = accent,
            onSecondary = onAccent,
            secondaryContainer = accent.copy(alpha = 0.18f),
            onSecondaryContainer = accent,
            tertiary = accent,
            onTertiary = onAccent,
            background = Color(0xFF101014),
            onBackground = Color(0xFFE9E9EF),
            surface = Color(0xFF16161B),
            onSurface = Color(0xFFE9E9EF),
            surfaceVariant = Color(0xFF1E1E25),
            onSurfaceVariant = Color(0xFFB9B9C6),
            outline = Color(0xFF33333D),
            outlineVariant = Color(0xFF26262E),
            error = Color(0xFFF43F5E),
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accent.copy(alpha = 0.12f),
            onPrimaryContainer = accent,
            secondary = accent,
            onSecondary = onAccent,
            secondaryContainer = accent.copy(alpha = 0.12f),
            onSecondaryContainer = accent,
            tertiary = accent,
            onTertiary = onAccent,
            background = Color(0xFFFCFCFF),
            onBackground = Color(0xFF1A1B21),
            surface = Color(0xFFFCFCFF),
            onSurface = Color(0xFF1A1B21),
            surfaceVariant = Color(0xFFF1F1F6),
            onSurfaceVariant = Color(0xFF55565E),
            outline = Color(0xFFC9C9D2),
            outlineVariant = Color(0xFFE4E4EA),
            error = Color(0xFFDC2626),
            onError = Color.White
        )
    }
}

private fun monoScheme(accent: Color, dark: Boolean): androidx.compose.material3.ColorScheme {
    val onAccent = if (accent.luminanceCompat() > 0.5f) Color(0xFF10131A) else Color.White
    return if (dark) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accent.copy(alpha = 0.16f),
            onPrimaryContainer = accent,
            secondary = Color(0xFFC8C8D0),
            onSecondary = Color(0xFF151515),
            secondaryContainer = Color(0xFF2A2A2A),
            onSecondaryContainer = Color(0xFFD8D8D8),
            tertiary = Color(0xFFC8C8D0),
            onTertiary = Color(0xFF151515),
            background = Color(0xFF121212),
            onBackground = Color(0xFFECECEC),
            surface = Color(0xFF171717),
            onSurface = Color(0xFFECECEC),
            surfaceVariant = Color(0xFF1F1F1F),
            onSurfaceVariant = Color(0xFFABABAB),
            outline = Color(0xFF3A3A3A),
            outlineVariant = Color(0xFF2A2A2A),
            error = Color(0xFFEF4444),
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accent.copy(alpha = 0.1f),
            onPrimaryContainer = accent,
            secondary = Color(0xFF616161),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEEEEEE),
            onSecondaryContainer = Color(0xFF3D3D3D),
            tertiary = Color(0xFF616161),
            onTertiary = Color.White,
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF1B1B1B),
            surface = Color(0xFFFAFAFA),
            onSurface = Color(0xFF1B1B1B),
            surfaceVariant = Color(0xFFF2F2F2),
            onSurfaceVariant = Color(0xFF5E5E5E),
            outline = Color(0xFFDDDDDD),
            outlineVariant = Color(0xFFEAEAEA),
            error = Color(0xFFDC2626),
            onError = Color.White
        )
    }
}

private fun Color.luminanceCompat(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)

@Composable
fun MMOCalendarTheme(
    themeMode: ThemeMode,
    style: AppStyle,
    accentIndex: Int,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val accent = androidx.compose.ui.graphics.Color(AccentPalette.at(accentIndex).value)

    val scheme = when (style) {
        AppStyle.MATERIAL_YOU ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                classicScheme(accent, dark)
            }
        AppStyle.CLASSIC -> classicScheme(accent, dark)
        AppStyle.MONO -> monoScheme(accent, dark)
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content
    )
}
