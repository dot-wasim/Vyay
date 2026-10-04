package com.vyayah.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Rose500 = Color(0xFFF43F5E)
val Amber500 = Color(0xFFF59E0B)
val Indigo500 = Color(0xFF6366F1)

val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    secondary = Indigo500,
    tertiary = Amber500,
    background = Slate900,
    surface = Slate800,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Slate700
)

val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    secondary = Indigo500,
    tertiary = Amber500,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE2E8F0)
)
