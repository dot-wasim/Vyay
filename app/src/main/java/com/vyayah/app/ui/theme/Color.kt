package com.vyayah.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Editorial / Newsprint Paper Palette
val ParchmentLight = Color(0xFFF7F4EC)
val ParchmentSurface = Color(0xFFEFECE3)
val ParchmentBorder = Color(0xFFDCD7CA)
val InkPrimary = Color(0xFF1B1B19)
val InkSecondary = Color(0xFF6B6960)
val ForestGreen = Color(0xFF1E613B)
val LightForestGreen = Color(0xFF2E8B57)

// Dark Mode Palette (Ink Paper)
val InkDark = Color(0xFF141413)
val InkSurfaceDark = Color(0xFF20201E)
val InkBorderDark = Color(0xFF333330)
val PaperTextDark = Color(0xFFF5F3EC)
val PaperTextSecondaryDark = Color(0xFF9E9B91)

val PaperLightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = InkSecondary,
    tertiary = ForestGreen,
    background = ParchmentLight,
    surface = ParchmentLight,
    surfaceVariant = ParchmentSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = InkPrimary,
    onSurface = InkPrimary,
    onSurfaceVariant = InkSecondary,
    outline = ParchmentBorder
)

val PaperDarkColorScheme = darkColorScheme(
    primary = LightForestGreen,
    secondary = PaperTextSecondaryDark,
    tertiary = LightForestGreen,
    background = InkDark,
    surface = InkDark,
    surfaceVariant = InkSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PaperTextDark,
    onSurface = PaperTextDark,
    onSurfaceVariant = PaperTextSecondaryDark,
    outline = InkBorderDark
)
