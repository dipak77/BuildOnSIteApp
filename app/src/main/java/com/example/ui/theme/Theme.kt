// ─────────────────────────────────────────────────────────────────────────────
// FILE: com/example/ui/theme/Theme.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  EXTENDED COLOR TOKENS  (beyond Material 3 scheme)
// ══════════════════════════════════════════════════════════════════════════════

@Immutable
data class ExtendedColors(
    // ── Backgrounds ──────────────────────────────────────────────────────────
    val pageBg: Color,
    val cardBg: Color,
    val cardBgRaised: Color,
    val cardBgSunken: Color,
    val modalBg: Color,

    // ── Glass surfaces ────────────────────────────────────────────────────────
    val glassSubtle: Color,
    val glassMedium: Color,
    val glassStrong: Color,

    // ── Borders ───────────────────────────────────────────────────────────────
    val borderSubtle: Color,
    val borderMedium: Color,
    val borderStrong: Color,

    // ── Neon accents ──────────────────────────────────────────────────────────
    val accentPrimary: Color,     // Cyan / Sky Blue
    val accentSecondary: Color,   // Purple / Violet
    val accentSuccess: Color,     // Green
    val accentDanger: Color,      // Pink / Red
    val accentWarning: Color,     // Amber
    val accentGold: Color,        // Gold

    // ── Neon glows ────────────────────────────────────────────────────────────
    val glowPrimary: Color,
    val glowSecondary: Color,
    val glowSuccess: Color,
    val glowDanger: Color,

    // ── Text ──────────────────────────────────────────────────────────────────
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textTertiary: Color,
    val textOnAccent: Color,

    // ── Status semantic ───────────────────────────────────────────────────────
    val statusSuccess: Color,
    val statusError: Color,
    val statusWarning: Color,
    val statusInfo: Color,
    val statusSuccessBg: Color,
    val statusErrorBg: Color,
    val statusWarningBg: Color,
    val statusInfoBg: Color,

    // ── Card tints ────────────────────────────────────────────────────────────
    val tintCyan: Color,
    val tintPurple: Color,
    val tintGreen: Color,
    val tintPink: Color,
    val tintAmber: Color,
    val tintGold: Color,

    // ── Chart colors ──────────────────────────────────────────────────────────
    val chartColors: List<Color>,

    // ── Blueprint ────────────────────────────────────────────────────────────
    val blueprintGrid: Color,

    // ── Flag ─────────────────────────────────────────────────────────────────
    val isDark: Boolean
)

// ── Dark Extended Colors ──────────────────────────────────────────────────────
private val DarkExtended = ExtendedColors(
    // Backgrounds
    pageBg       = DarkBg0,
    cardBg       = DarkBg2,
    cardBgRaised = DarkBg3,
    cardBgSunken = DarkBg1,
    modalBg      = DarkBg4,

    // Glass
    glassSubtle  = GlassSurfaceDark,
    glassMedium  = GlassSurfaceMediumDark,
    glassStrong  = GlassSurfaceStrongDark,

    // Borders
    borderSubtle = GlassBorderDark,
    borderMedium = GlassBorderMediumDark,
    borderStrong = GlassBorderStrongDark,

    // Accents
    accentPrimary   = NeonCyan,
    accentSecondary = NeonPurple,
    accentSuccess   = NeonGreen,
    accentDanger    = NeonPink,
    accentWarning   = NeonAmber,
    accentGold      = GoldBase,

    // Glows
    glowPrimary   = GlowCyan,
    glowSecondary = GlowPurple,
    glowSuccess   = GlowGreen,
    glowDanger    = GlowPink,

    // Text
    textPrimary   = TextPrimary,
    textSecondary = TextSecondary,
    textMuted     = TextMuted,
    textTertiary  = TextTertiary,
    textOnAccent  = Color.Black,

    // Status
    statusSuccess   = SemanticSuccess,
    statusError     = SemanticError,
    statusWarning   = SemanticWarning,
    statusInfo      = SemanticInfo,
    statusSuccessBg = SemanticSuccessBg,
    statusErrorBg   = SemanticErrorBg,
    statusWarningBg = SemanticWarningBg,
    statusInfoBg    = SemanticInfoBg,

    // Tints
    tintCyan   = CardTintCyan,
    tintPurple = CardTintPurple,
    tintGreen  = CardTintGreen,
    tintPink   = CardTintPink,
    tintAmber  = CardTintAmber,
    tintGold   = CardTintGold,

    chartColors  = ChartColorsDark,
    blueprintGrid = BlueprintGridDark,
    isDark        = true
)

// ── Light Extended Colors ─────────────────────────────────────────────────────
private val LightExtended = ExtendedColors(
    // Backgrounds
    pageBg       = LightBg0,
    cardBg       = LightBg2,
    cardBgRaised = LightBg3,
    cardBgSunken = LightBg1,
    modalBg      = LightBg4,

    // Glass
    glassSubtle  = GlassSurfaceLight,
    glassMedium  = GlassSurfaceMediumLight,
    glassStrong  = GlassSurfaceStrongLight,

    // Borders
    borderSubtle = GlassBorderLight,
    borderMedium = GlassBorderMediumLight,
    borderStrong = GlassBorderStrongLight,

    // Accents (deeper for light theme legibility)
    accentPrimary   = LightCyan,
    accentSecondary = LightPurple,
    accentSuccess   = LightGreen,
    accentDanger    = LightPink,
    accentWarning   = LightAmber,
    accentGold      = GoldDark,

    // Glows (subtle for light)
    glowPrimary   = Color(0x220284C7),
    glowSecondary = Color(0x227C3AED),
    glowSuccess   = Color(0x22059669),
    glowDanger    = Color(0x22E11D48),

    // Text
    textPrimary   = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted     = TextMutedLight,
    textTertiary  = TextTertiaryLight,
    textOnAccent  = Color.White,

    // Status
    statusSuccess   = SemanticSuccessLight,
    statusError     = SemanticErrorLight,
    statusWarning   = SemanticWarningLight,
    statusInfo      = SemanticInfoLight,
    statusSuccessBg = SemanticSuccessBgL,
    statusErrorBg   = SemanticErrorBgL,
    statusWarningBg = SemanticWarningBgL,
    statusInfoBg    = SemanticInfoBg,

    // Tints
    tintCyan   = CardTintCyanL,
    tintPurple = CardTintPurpleL,
    tintGreen  = CardTintGreenL,
    tintPink   = CardTintPinkL,
    tintAmber  = CardTintAmberL,
    tintGold   = CardTintGoldL,

    chartColors   = ChartColorsLight,
    blueprintGrid = BlueprintGridLight,
    isDark        = false
)

// ══════════════════════════════════════════════════════════════════════════════
//  MATERIAL 3 COLOR SCHEMES
// ══════════════════════════════════════════════════════════════════════════════

private val M3DarkColorScheme = darkColorScheme(
    // Primary (Cyan)
    primary          = NeonCyan,
    onPrimary        = Color(0xFF001F26),
    primaryContainer = Color(0xFF003640),
    onPrimaryContainer = Color(0xFF9EEFFD),

    // Secondary (Purple)
    secondary          = NeonPurple,
    onSecondary        = Color(0xFF1E0064),
    secondaryContainer = Color(0xFF38006B),
    onSecondaryContainer = Color(0xFFE8DDFF),

    // Tertiary (Green)
    tertiary          = NeonGreen,
    onTertiary        = Color(0xFF002114),
    tertiaryContainer = Color(0xFF003822),
    onTertiaryContainer = Color(0xFF72FDB5),

    // Error
    error          = NeonPink,
    onError        = Color(0xFF370010),
    errorContainer = Color(0xFF5C001F),
    onErrorContainer = Color(0xFFFFB3C1),

    // Backgrounds & Surfaces
    background    = DarkBg0,
    onBackground  = TextPrimary,
    surface       = DarkBg2,
    onSurface     = TextPrimary,
    surfaceVariant    = DarkBg3,
    onSurfaceVariant  = TextSecondary,
    surfaceTint       = NeonCyan,

    // Utility
    outline         = GlassBorderDark,
    outlineVariant  = GlassBorderMediumDark,
    scrim           = Color(0xFF000000),
    inverseSurface  = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF1E293B),
    inversePrimary  = LightCyan
)

private val M3LightColorScheme = lightColorScheme(
    // Primary (Sky Blue — readable on white)
    primary          = LightCyan,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFCEEAFF),
    onPrimaryContainer = Color(0xFF001E2E),

    // Secondary (Deep Violet)
    secondary          = LightPurple,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF2E0087),

    // Tertiary (Emerald)
    tertiary          = LightGreen,
    onTertiary        = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF002113),

    // Error
    error          = SemanticErrorLight,
    onError        = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Backgrounds & Surfaces
    background    = LightBg0,
    onBackground  = TextPrimaryLight,
    surface       = LightBg3,
    onSurface     = TextPrimaryLight,
    surfaceVariant    = LightBg1,
    onSurfaceVariant  = TextSecondaryLight,
    surfaceTint       = LightCyan,

    // Utility
    outline         = GlassBorderLight,
    outlineVariant  = GlassBorderMediumLight,
    scrim           = Color(0xFF000000),
    inverseSurface  = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF1F5F9),
    inversePrimary  = NeonCyan
)

// ══════════════════════════════════════════════════════════════════════════════
//  COMPOSITION LOCAL
// ══════════════════════════════════════════════════════════════════════════════

val LocalExtendedColors = compositionLocalOf { LightExtended }

// Convenience accessor — use anywhere in Composable tree
val androidx.compose.material3.ColorScheme.ext: ExtendedColors
    @Composable get() = LocalExtendedColors.current

// ══════════════════════════════════════════════════════════════════════════════
//  THEME ENTRY POINT
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val m3Scheme   = if (darkTheme) M3DarkColorScheme  else M3LightColorScheme
    val extScheme  = if (darkTheme) DarkExtended        else LightExtended

    CompositionLocalProvider(LocalExtendedColors provides extScheme) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography  = Typography,
            content     = content
        )
    }
}