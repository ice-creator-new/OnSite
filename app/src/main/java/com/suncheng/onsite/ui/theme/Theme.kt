package com.suncheng.onsite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IceScheme = darkColorScheme(
    primary = IceAccent,
    onPrimary = IceBg,
    secondary = IceMuted,
    onSecondary = IceBg,
    background = IceBg,
    onBackground = IceFg,
    surface = IceBgElevated,
    onSurface = IceFg,
    surfaceVariant = Color(0xFF1A1E27),
    onSurfaceVariant = IceMuted,
    outline = IceLine,
    error = IceDanger,
)

@Composable
fun OnSiteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IceScheme,
        typography = Typography,
        content = content,
    )
}
