// ─────────────────────────────────────────────────────────────────────────────
// FILE: com/example/ui/theme/Color.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  DARK THEME — Deep Space Glass System
// ══════════════════════════════════════════════════════════════════════════════

// ── Core Backgrounds ──────────────────────────────────────────────────────────
val DarkBg0          = Color(0xFF020817)   // Deepest void — page background
val DarkBg1          = Color(0xFF060D1F)   // Layer 1 — elevated surface base
val DarkBg2          = Color(0xFF0A1628)   // Layer 2 — card surface
val DarkBg3          = Color(0xFF0F1E35)   // Layer 3 — raised card
val DarkBg4          = Color(0xFF162035)   // Layer 4 — modal / overlay

// ── Glass / Frosted Surfaces ──────────────────────────────────────────────────
val GlassBackgroundDark      = Color(0xFF020817)
val GlassSurfaceDark         = Color(0x1AFFFFFF)   // 10% white glass
val GlassSurfaceMediumDark   = Color(0x26FFFFFF)   // 15% white glass
val GlassSurfaceStrongDark   = Color(0x33FFFFFF)   // 20% white glass
val GlassSurfaceDeeperDark   = Color(0x60090D1A)

// ── Dark Borders ──────────────────────────────────────────────────────────────
val GlassBorderDark          = Color(0x1FFFFFFF)   // subtle white border
val GlassBorderMediumDark    = Color(0x33FFFFFF)   // medium white border
val GlassBorderStrongDark    = Color(0x55FFFFFF)   // strong white border
val GlassBorderNeonCyan      = Color(0x6606B6D4)
val GlassBorderNeonPurple    = Color(0x668B5CF6)
val GlassBorderNeonGreen     = Color(0x6610B981)
val GlassBorderNeonPink      = Color(0x66F43F5E)
val GlassBorderGold          = Color(0x66EAB308)

// ══════════════════════════════════════════════════════════════════════════════
//  LIGHT THEME — Premium Porcelain / Sky Glass System
// ══════════════════════════════════════════════════════════════════════════════

// ── Core Backgrounds ──────────────────────────────────────────────────────────
val LightBg0         = Color(0xFFF0F4FF)   // Deepest sky — page background
val LightBg1         = Color(0xFFF5F8FF)   // Layer 1
val LightBg2         = Color(0xFFFBFCFF)   // Layer 2 — card
val LightBg3         = Color(0xFFFFFFFF)   // Layer 3 — raised card
val LightBg4         = Color(0xFFFAFBFF)   // Layer 4 — modal

// ── Glass / Frosted Surfaces ──────────────────────────────────────────────────
val GlassBackgroundLight     = Color(0xFFF0F4FF)
val GlassSurfaceLight        = Color(0xCCFFFFFF)   // 80% white
val GlassSurfaceMediumLight  = Color(0xB3FFFFFF)   // 70% white
val GlassSurfaceStrongLight  = Color(0xE6FFFFFF)   // 90% white

// ── Light Borders ─────────────────────────────────────────────────────────────
val GlassBorderLight         = Color(0x1A6366F1)   // subtle indigo tint
val GlassBorderMediumLight   = Color(0x336366F1)   // medium
val GlassBorderStrongLight   = Color(0x556366F1)   // strong
val GlassBorderCyanLight     = Color(0x330284C7)
val GlassBorderPurpleLight   = Color(0x337C3AED)
val GlassBorderGreenLight    = Color(0x33059669)

// ══════════════════════════════════════════════════════════════════════════════
//  NEON ACCENT PALETTE  (shared dark + light, tone-adjusted in usage)
// ══════════════════════════════════════════════════════════════════════════════

// ── Primary Neon Set ──────────────────────────────────────────────────────────
val NeonCyan         = Color(0xFF00D4FF)   // Electric Cyan   — primary action
val NeonCyanDim      = Color(0xFF06B6D4)   // Slightly dimmer variant
val NeonPurple       = Color(0xFFA78BFA)   // Soft Violet     — secondary
val NeonPurpleDim    = Color(0xFF8B5CF6)   // Deeper violet
val NeonPink         = Color(0xFFFF4D7D)   // Hot Pink        — danger / out
val NeonPinkDim      = Color(0xFFF43F5E)   // Dimmer pink
val NeonGreen        = Color(0xFF00FF87)   // Cyber Green     — success / in
val NeonGreenDim     = Color(0xFF10B981)   // Emerald dim
val NeonAmber        = Color(0xFFFFB020)   // Warm Amber      — warning
val NeonAmberDim     = Color(0xFFF59E0B)   // Dim amber
val NeonOrange       = Color(0xFFFF6B35)   // Neon Orange     — accent
val NeonBlue         = Color(0xFF3B82F6)   // Royal Blue      — info

// ── Light Theme Equivalents (deeper, saturated for readability) ───────────────
val LightCyan        = Color(0xFF0284C7)   // Deep Sky Blue
val LightPurple      = Color(0xFF7C3AED)   // Deep Violet
val LightPink        = Color(0xFFB91C1C)   // Dark Red
val LightGreen       = Color(0xFF15803D)   // Dark Green
val LightAmber       = Color(0xFFD97706)   // Deep Amber
val LightOrange      = Color(0xFFEA580C)   // Deep Orange
val LightBlue        = Color(0xFF1D4ED8)   // Deep Blue

// ══════════════════════════════════════════════════════════════════════════════
//  GOLD / METALLIC SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

val GoldPlatinum     = Color(0xFFE8E8E8)
val GoldPlatinumDim  = Color(0xFFB0B8C8)
val GoldLight        = Color(0xFFFDE047)   // Bright lemon gold
val GoldBase         = Color(0xFFFACC15)   // Standard gold
val GoldWarm         = Color(0xFFEAB308)   // Warm gold
val GoldDark         = Color(0xFFCA8A04)   // Deep gold
val GoldBronze       = Color(0xFFA16207)   // Bronze
val GoldCopper       = Color(0xFF92400E)   // Copper dark
val GoldMetallic     = Color(0xFFEAB308)

// ══════════════════════════════════════════════════════════════════════════════
//  TEXT SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

// Dark Theme Text
val TextPrimary        = Color(0xFFF8FAFC)   // Near-white primary
val TextPrimaryDim     = Color(0xFFE2E8F0)   // Slightly dimmed
val TextSecondary      = Color(0xFF94A3B8)   // Slate secondary
val TextSecondaryDim   = Color(0xFF64748B)   // Dimmer secondary
val TextMuted          = Color(0xFF475569)   // Muted / disabled
val TextTertiary       = Color(0xFF334155)   // Very muted

// Light Theme Text
val TextPrimaryLight      = Color(0xFF0F172A)   // Near-black primary
val TextPrimaryLightDim   = Color(0xFF1E293B)   // Slightly lighter
val TextSecondaryLight    = Color(0xFF475569)   // Slate secondary
val TextSecondaryLightDim = Color(0xFF64748B)   // Lighter secondary
val TextMutedLight        = Color(0xFF94A3B8)   // Muted / disabled
val TextTertiaryLight     = Color(0xFFCBD5E1)   // Very muted

// ══════════════════════════════════════════════════════════════════════════════
//  SEMANTIC STATUS COLORS
// ══════════════════════════════════════════════════════════════════════════════

// Success
val SemanticSuccess      = Color(0xFF00FF87)
val SemanticSuccessDim   = Color(0xFF10B981)
val SemanticSuccessLight = Color(0xFF15803D)
val SemanticSuccessBg    = Color(0x1500FF87)     // dark bg tint
val SemanticSuccessBgL   = Color(0xFFECFDF5)     // light bg tint

// Error / Danger
val SemanticError        = Color(0xFFFF4D7D)
val SemanticErrorDim     = Color(0xFFF43F5E)
val SemanticErrorLight   = Color(0xFFB91C1C)
val SemanticErrorBg      = Color(0x15FF4D7D)
val SemanticErrorBgL     = Color(0xFFFFF1F2)

// Warning
val SemanticWarning      = Color(0xFFFFB020)
val SemanticWarningDim   = Color(0xFFF59E0B)
val SemanticWarningLight = Color(0xFFD97706)
val SemanticWarningBg    = Color(0x15FFB020)
val SemanticWarningBgL   = Color(0xFFFFFBEB)

// Info
val SemanticInfo         = Color(0xFF00D4FF)
val SemanticInfoDim      = Color(0xFF0EA5E9)
val SemanticInfoLight    = Color(0xFF0284C7)
val SemanticInfoBg       = Color(0x1500D4FF)
val SemanticInfoBgL      = Color(0xFFEFF6FF)

// ══════════════════════════════════════════════════════════════════════════════
//  PREMIUM GRADIENT PRESETS
// ══════════════════════════════════════════════════════════════════════════════

// Dark Multi-Color Gradients
val GradientLuxuryPurple     = listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95), Color(0xFFEC4899))
val GradientCosmicBlue       = listOf(Color(0xFF00D4FF), Color(0xFF2563EB), Color(0xFF4F46E5))
val GradientSunsetAmber      = listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFFFCD34D))
val GradientMetallicGold     = listOf(Color(0xFFFEF08A), Color(0xFFEAB308), Color(0xFFCA8A04), Color(0xFFFEF08A))
val GradientCarbonDark       = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020817))
val GradientElectricCyan     = listOf(Color(0xFF00D4FF), Color(0xFF0EA5E9), Color(0xFF2563EB))
val GradientCyberGreen       = listOf(Color(0xFF00FF87), Color(0xFF10B981), Color(0xFF059669))
val GradientNeonFire         = listOf(Color(0xFFFF6B35), Color(0xFFFF4D7D), Color(0xFF8B5CF6))
val GradientDeepSpace        = listOf(Color(0xFF020817), Color(0xFF0D1B2A), Color(0xFF0A0A1A))
val GradientPrismatic        = listOf(
    Color(0xFF00D4FF), Color(0xFF7C3AED), Color(0xFFFF4D7D),
    Color(0xFFFFB020), Color(0xFF00FF87), Color(0xFF00D4FF)
)

// Light Multi-Color Gradients
val GradientSkyBlueLight     = listOf(Color(0xFF0284C7), Color(0xFF4338CA), Color(0xFF7C3AED))
val GradientFreshGreenLight  = listOf(Color(0xFF059669), Color(0xFF0284C7), Color(0xFF4338CA))
val GradientSunriseLight     = listOf(Color(0xFFEA580C), Color(0xFFD97706), Color(0xFFF59E0B))
val GradientRoseLight        = listOf(Color(0xFFE11D48), Color(0xFF9333EA), Color(0xFF4F46E5))
val GradientOceanLight       = listOf(Color(0xFF0284C7), Color(0xFF0891B2), Color(0xFF059669))

// ══════════════════════════════════════════════════════════════════════════════
//  BLUEPRINT / TEXTURE HELPERS
// ══════════════════════════════════════════════════════════════════════════════

val BlueprintGridDark        = Color(0x0800D4FF)   // very faint cyan grid
val BlueprintGridLight       = Color(0x080284C7)   // very faint blue grid
val TextureBlueAccent        = Color(0x1F00F2FE)
val TexturePurpleAccent      = Color(0x1F4FACFE)
val TextureGoldAccent        = Color(0x1FEAB308)
val TextureGreenAccent       = Color(0x1F10B981)

// ══════════════════════════════════════════════════════════════════════════════
//  CHART / DATA-VIZ PALETTE  (8-color cycle)
// ══════════════════════════════════════════════════════════════════════════════

val ChartColor1  = Color(0xFF00D4FF)   // Cyan
val ChartColor2  = Color(0xFF00FF87)   // Green
val ChartColor3  = Color(0xFFA78BFA)   // Purple
val ChartColor4  = Color(0xFFFF4D7D)   // Pink
val ChartColor5  = Color(0xFFFFB020)   // Amber
val ChartColor6  = Color(0xFF3B82F6)   // Blue
val ChartColor7  = Color(0xFFFF6B35)   // Orange
val ChartColor8  = Color(0xFFFACC15)   // Gold

val ChartColorsDark  = listOf(ChartColor1, ChartColor2, ChartColor3, ChartColor4,
                               ChartColor5, ChartColor6, ChartColor7, ChartColor8)
val ChartColorsLight = listOf(LightCyan, LightGreen, LightPurple, LightPink,
                               LightAmber, LightBlue, LightOrange, GoldDark)

// ══════════════════════════════════════════════════════════════════════════════
//  SHADOW / ELEVATION GLOW (for drawBehind usage)
// ══════════════════════════════════════════════════════════════════════════════

val GlowCyan     = Color(0x3300D4FF)
val GlowPurple   = Color(0x33A78BFA)
val GlowPink     = Color(0x33FF4D7D)
val GlowGreen    = Color(0x3300FF87)
val GlowAmber    = Color(0x33FFB020)
val GlowGold     = Color(0x33EAB308)
val GlowBlue     = Color(0x333B82F6)
val GlowWhite    = Color(0x22FFFFFF)

// ══════════════════════════════════════════════════════════════════════════════
//  SPECIAL SURFACE TINTS  (category-specific card tints)
// ══════════════════════════════════════════════════════════════════════════════

// Dark card tints (very subtle)
val CardTintCyan    = Color(0x0A00D4FF)
val CardTintPurple  = Color(0x0AA78BFA)
val CardTintGreen   = Color(0x0A00FF87)
val CardTintPink    = Color(0x0AFF4D7D)
val CardTintAmber   = Color(0x0AFFB020)
val CardTintGold    = Color(0x0AEAB308)

// Light card tints
val CardTintCyanL   = Color(0xFFEFF6FF)
val CardTintPurpleL = Color(0xFFF5F3FF)
val CardTintGreenL  = Color(0xFFECFDF5)
val CardTintPinkL   = Color(0xFFFFF1F2)
val CardTintAmberL  = Color(0xFFFFFBEB)
val CardTintGoldL   = Color(0xFFFEF9C3)