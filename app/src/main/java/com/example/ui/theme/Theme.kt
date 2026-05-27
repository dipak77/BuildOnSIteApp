package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonPurple,
    onSecondary = Color.White,
    tertiary = NeonGreen,
    onTertiary = Color.Black,
    background = GlassBackgroundDark,
    onBackground = TextPrimary,
    surface = Color(0xFF0F172A), // Slate 900 base for modal fallback
    onSurface = TextPrimary,
    surfaceVariant = Color(0x3B1F2937),
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderDark
)

private val GlassLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7), // Light cyan-ish blue
    onPrimary = Color.White,
    secondary = Color(0xFF7C3AED), // Light purple
    onSecondary = Color.White,
    tertiary = Color(0xFF059669), // Green
    onTertiary = Color.White,
    background = GlassBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9), // Luxurious light slate gray
    onSurfaceVariant = TextSecondaryLight,
    outline = GlassBorderLight // Elegant translucent indigo track outline trace
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We default to dark theme for premium neon look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        GlassDarkColorScheme
    } else {
        GlassLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
