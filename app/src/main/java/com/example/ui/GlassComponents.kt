package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. DYNAMIC NEBULA GRADIENT BACKGROUND (WITH 3D MULTI-LAYER FLOATING DEPTH)
// ==========================================

@Composable
fun GlassAtmosphereBox(
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Elegant infinite lifecycle for breathing gas clouds & rotating light nodes
    val infiniteTransition = rememberInfiniteTransition(label = "NebulaAtmosphere")
    
    val breathingValue by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "atmosphereBreathing"
    )

    val floatingOffsetAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatingOffsetAngle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (darkTheme) GlassBackgroundDark else GlassBackgroundLight)
            .drawBehind {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind

                if (darkTheme) {
                    // Floating offsets in circular pattern to simulate high-depth planetary shift
                    val shiftX1 = cos(floatingOffsetAngle) * (w * 0.05f)
                    val shiftY1 = sin(floatingOffsetAngle) * (h * 0.04f)
                    val shiftX2 = sin(floatingOffsetAngle * 1.5f) * (w * 0.04f)
                    val shiftY2 = cos(floatingOffsetAngle * 1.5f) * (h * 0.05f)

                    // Layer 1: Majestic Deep Neon Cyan gas bubble top-left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.22f * breathingValue), Color.Transparent),
                            center = Offset(w * 0.12f + shiftX1, h * 0.14f + shiftY1),
                            radius = w * 0.80f * breathingValue
                        )
                    )

                    // Layer 2: Radiant Cosmic Violet bubble bottom-right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.20f * (2f - breathingValue)), Color.Transparent),
                            center = Offset(w * 0.88f + shiftX2, h * 0.86f + shiftY2),
                            radius = w * 0.82f * (2f - breathingValue)
                        )
                    )

                    // Layer 3: Warm Solar Amber core in the center left (Gives dramatic multi-layered 3D nebula feeling)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GoldMetallic.copy(alpha = 0.09f), Color.Transparent),
                            center = Offset(w * 0.35f - shiftX1 * 0.5f, h * 0.60f - shiftY2 * 0.5f),
                            radius = w * 0.50f
                        )
                    )

                    // Layer 4: Architectural grid mapping lines (Procedural overlay blueprint nodes)
                    val columns = 8
                    val rows = 16
                    val gridC = Color(0x0E00F2FE)
                    for (i in 0..columns) {
                        val x = w * (i.toFloat() / columns.toFloat())
                        drawLine(color = gridC, start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 0.8f)
                    }
                    for (i in 0..rows) {
                        val y = h * (i.toFloat() / rows.toFloat())
                        drawLine(color = gridC, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 0.8f)
                    }

                    // Floating high-contrast vector sparkle stars (3D blueprint coordinates)
                    val starPositions = listOf(
                        Offset(w * 0.2f, h * 0.25f),
                        Offset(w * 0.75f, h * 0.18f),
                        Offset(w * 0.15f, h * 0.75f),
                        Offset(w * 0.80f, h * 0.65f),
                        Offset(w * 0.45f, h * 0.40f)
                    )
                    starPositions.forEachIndexed { idx, pos ->
                        val starBreathingAlpha = 0.15f + 0.35f * sin(floatingOffsetAngle * 2f + idx * 1.5f)
                        drawCircle(
                            color = NeonCyan.copy(alpha = starBreathingAlpha),
                            radius = 1.5.dp.toPx(),
                            center = pos
                        )
                    }

                } else {
                    // Elevated Light Theme sunrise aurora with deeper 3D depth layered flow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFBAE6FD).copy(alpha = 0.48f), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.12f),
                            radius = w * 0.80f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFBCFE8).copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(w * 0.12f, h * 0.52f),
                            radius = w * 0.80f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFEF08A).copy(alpha = 0.42f), Color.Transparent),
                            center = Offset(w * 0.88f, h * 0.88f),
                            radius = w * 0.75f
                        )
                    )
                }
            },
        content = content
    )
}

// ==========================================
// 2. FROSTED-GLASS CARD (WITH SHIMMER BORDERS & MICRO-INTERACTIONS)
// ==========================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true,
    borderColor: Color? = null,
    glowColor: Color? = null,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Soft breathing scale on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "clickScale"
    )

    // Animated glow pulse on hover/touch or continuous in dark mode
    val infiniteTransition = rememberInfiniteTransition(label = "CardBorderShimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPercentage"
    )

    // Frosted background - richer depth and high-contrast backdrop
    val bg = if (darkTheme) {
        Color(0x50090D1A) // Deeper crystal grey obsidian aura (31% opacity)
    } else {
        Color(0xA6FFFFFF) // Luxe crystal white backdrop with 65% opacity
    }

    // Dynamic shimmer border brush
    val activeBorderColor = glowColor ?: if (darkTheme) NeonCyan else Color(0xFF6366F1)
    
    val defaultBorderBrush = if (darkTheme) {
        Brush.sweepGradient(
            colors = listOf(
                GlassBorderDark,
                activeBorderColor.copy(alpha = 0.4f),
                GlassBorderDark,
                activeBorderColor.copy(alpha = 0.1f),
                GlassBorderDark
            )
        )
    } else {
        Brush.sweepGradient(
            colors = listOf(
                Color(0x264F46E5),
                activeBorderColor.copy(alpha = 0.35f),
                Color(0x264F46E5),
                Color(0x104F46E5),
                Color(0x264F46E5)
            )
        )
    }

    val contentModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawBehind {
            if (darkTheme) {
                // Layered Neon auroral glow behind card that expands slightly on press
                val actualGlowColor = glowColor ?: NeonCyan
                val radiusFactor = if (isPressed) 0.48f else 0.42f
                val auraOpacity = if (isPressed) 0.12f else 0.08f
                drawCircle(
                    color = actualGlowColor.copy(alpha = auraOpacity),
                    radius = size.maxDimension * radiusFactor,
                    center = center
                )
            } else {
                // Double soft luxury ambient drop shadow aura for light theme
                val shadowColor = glowColor?.copy(alpha = 0.06f) ?: Color(0xFF6366F1).copy(alpha = 0.05f)
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(-4f, 4f),
                    size = Size(size.width + 8f, size.height + 8f),
                    cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                )
            }
        }
        .clip(RoundedCornerShape(20.dp))
        .background(bg)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                )
            } else Modifier
        )

    Card(
        modifier = contentModifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = if (borderColor != null) Brush.linearGradient(listOf(borderColor, borderColor)) else defaultBorderBrush
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

// ==========================================
// 3. TACTILE NEON-GLOW BUTTONS (WITH VELVET GLOW PULSE)
// ==========================================

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true,
    glowColor: Color = NeonCyan,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    outlineMode: Boolean = false,
    horizontalPadding: Dp = 20.dp,
    verticalPadding: Dp = 12.dp,
    minHeight: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth snappy tactile scaling on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    // Pulse animations for neon glow effect
    val pulseTransition = rememberInfiniteTransition(label = "ButtonPulseGlow")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonPulseAlpha"
    )

    // Linear light swipe sweeping over button
    val sweepOffset by pulseTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "buttonSweepOffset"
    )

    val resolvedGlowColor = if (!darkTheme && glowColor == NeonCyan) {
        Color(0xFF0369A1) // High-contrast crisp text contrast color
    } else {
        glowColor
    }

    // Assign premium graduated gradients based on base neon tones
    val gradientColors = when (glowColor) {
        NeonCyan -> GradientCosmicBlue
        NeonPurple -> GradientLuxuryPurple
        NeonAmber -> GradientSunsetAmber
        else -> listOf(resolvedGlowColor, resolvedGlowColor.mix(Color.Black, 0.2f))
    }

    val bgBrush = if (outlineMode) {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    } else {
        Brush.linearGradient(
            colors = gradientColors
        )
    }

    val resolvedTextColor = if (outlineMode) {
        resolvedGlowColor
    } else {
        if (resolvedGlowColor == NeonCyan || resolvedGlowColor == NeonAmber || resolvedGlowColor == Color(0xFFFCD34D) || resolvedGlowColor == Color(0xFFF59E0B) || resolvedGlowColor == GoldLight) {
            Color(0xFF070B14) // Deep Obsidian
        } else {
            Color.White
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                if (darkTheme && !outlineMode && enabled) {
                    // Double halo ring structure
                    drawCircle(
                        color = resolvedGlowColor.copy(alpha = pulseAlpha),
                        radius = size.width * 0.58f,
                        center = center
                    )
                    // Core focal pressure halo
                    if (isPressed) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = size.width * 0.42f,
                            center = center
                        )
                    }
                }
            }
            .clip(RoundedCornerShape(99.dp))
            .background(if (enabled) bgBrush else Brush.linearGradient(listOf(Color(0x339CA3AF), Color(0x339CA3AF))))
            .then(
                if (outlineMode) Modifier.background(
                    if (darkTheme) Color(0x1F111827) else Color(0x1FAFB8C8)
                ) else Modifier
            )
            .then(
                if (outlineMode) Modifier.border(
                    BorderStroke(1.5.dp, if (enabled) resolvedGlowColor.copy(alpha = 0.8f) else Color(0x4D9CA3AF)),
                    RoundedCornerShape(99.dp)
                ) else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .drawBehind {
                // Procedural premium glare shine swept overlay
                if (enabled && !outlineMode) {
                    val brushWidth = size.width * 0.35f
                    val sweepCenter = size.width * sweepOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(sweepCenter - brushWidth, 0f),
                            end = Offset(sweepCenter, size.height)
                        ),
                        size = size
                    )
                }
            }
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .defaultMinSize(minHeight = minHeight),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = resolvedTextColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            CompositionLocalProvider(
                LocalContentColor provides resolvedTextColor
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    content()
                }
            }
        }
    }
}

// Multiplies or mixes colors safely
private fun Color.mix(other: Color, ratio: Float): Color {
    return Color(
        red = this.red * (1 - ratio) + other.red * ratio,
        green = this.green * (1 - ratio) + other.green * ratio,
        blue = this.blue * (1 - ratio) + other.blue * ratio,
        alpha = this.alpha * (1 - ratio) + other.alpha * ratio
    )
}

// ==========================================
// 4. FROSTED TEXT FIELDS WITH NEON OUTLINES (ELEVATED HIERARCHY)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true,
    placeholder: String = "",
    icon: ImageVector? = null,
    isNumeric: Boolean = false,
    focusedStroke: Color = NeonCyan
) {
    val containerBg = if (darkTheme) Color(0x280B0F19) else Color(0x60FFFFFF)
    val textC = if (darkTheme) TextPrimary else TextPrimaryLight
    val labelC = if (darkTheme) TextSecondary else TextSecondaryLight

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        textStyle = LocalTextStyle.current.copy(
            color = textC, 
            fontSize = 15.sp, 
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumeric) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text
        ),
        leadingIcon = if (icon != null) {
            { Icon(imageVector = icon, contentDescription = null, tint = focusedStroke) }
        } else null,
        label = { Text(text = label, color = labelC, fontWeight = FontWeight.SemiBold) },
        placeholder = { Text(text = placeholder, color = labelC.copy(alpha = 0.45f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (darkTheme) Color(0x3B070A13) else Color(0x8DFFFFFF),
            unfocusedContainerColor = containerBg,
            focusedBorderColor = focusedStroke,
            unfocusedBorderColor = if (darkTheme) GlassBorderDark else GlassBorderLight,
            cursorColor = focusedStroke,
            focusedLabelColor = focusedStroke,
            unfocusedLabelColor = labelC
        )
    )
}

// ==========================================
// 5. TRANSLUCENT STATUS CHIPS
// ==========================================

@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true,
    activeColor: Color = NeonCyan
) {
    val resolvedActiveColor = if (!darkTheme) {
        when (activeColor) {
            NeonCyan -> Color(0xFF0284C7)
            NeonPurple -> Color(0xFF6D28D9)
            NeonGreen -> Color(0xFF047857)
            NeonPink -> Color(0xFFBE123C)
            else -> activeColor
        }
    } else {
        activeColor
    }

    val bg = if (selected) {
        resolvedActiveColor.copy(alpha = if (darkTheme) 0.28f else 0.18f)
    } else {
        if (darkTheme) Color(0x1AFFFFFF) else Color(0x0C111827)
    }

    val borderC = if (selected) {
        resolvedActiveColor
    } else {
        if (darkTheme) Color(0x1F9CA3AF) else Color(0x12111827)
    }

    val textC = if (selected) {
        resolvedActiveColor
    } else {
        if (darkTheme) TextSecondary else TextSecondaryLight
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .border(BorderStroke(1.dp, borderC), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .defaultMinSize(minWidth = 52.dp, minHeight = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textC,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// 6. SHIMMER PROGRESS BAR
// ==========================================

@Composable
fun GlassProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true,
    glowColor: Color = NeonCyan
) {
    val progressAnim by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progressPercentage"
    )

    val shimmerTransition = rememberInfiniteTransition(label = "ProgressShimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // Glowing track overlay
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(if (darkTheme) Color(0x1EFFFFFF) else Color(0x15000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progressAnim)
                .clip(RoundedCornerShape(99.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(glowColor.copy(alpha = 0.5f), glowColor, glowColor.copy(alpha = 0.8f))
                    )
                )
                .drawBehind {
                    if (darkTheme) {
                        drawCircle(
                            color = glowColor.copy(alpha = 0.45f),
                            radius = size.height * 1.6f,
                            center = Offset(size.width, size.height / 2f)
                        )
                    }
                }
        ) {
            // Internal sliding sheen highlights
            val brushWidth = 40.dp
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dx = size.width * shimmerOffset
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0f)
                        ),
                        startX = dx,
                        endX = dx + brushWidth.toPx()
                    )
                )
            }
        }
    }
}

// ==========================================
// 7. RESPONSIVE FROSTED DIALOG / BOTTOM SHEET (WITH SOFT BLUR SCATTERBackstop)
// ==========================================

@Composable
fun GlassModalDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    darkTheme: Boolean = true,
    glowColor: Color = NeonCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .drawBehind {
                    if (darkTheme) {
                        // Dramatic ambient light scattering backdrop circle representing "real focus glass layers"
                        drawCircle(
                            color = glowColor.copy(alpha = 0.15f),
                            radius = size.maxDimension * 0.58f,
                            center = center
                        )
                    }
                }
                .clip(RoundedCornerShape(24.dp))
                .background(if (darkTheme) Color(0xF2090D1A) else Color(0xF5F8FAFC)) // Extra frosted high opacity
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                glowColor.copy(alpha = 0.65f),
                                if (darkTheme) Color(0x30FFFFFF) else Color(0x30111827)
                            )
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Architectural crown accent handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (darkTheme) Color(0x3DFFFFFF) else Color(0x24000000))
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                // Header row with Close button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(glowColor)
                        )
                        Text(
                            text = title,
                            color = if (darkTheme) TextPrimary else TextPrimaryLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (darkTheme) Color(0x1AFFFFFF) else Color(0x0D000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (darkTheme) TextSecondary else TextSecondaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Inner content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp) // Maintain safety heights
                ) {
                    content()
                }
            }
        }
    }
}

// ==========================================
// 8. PROCEDURAL BRANDING LOGO COMPONENT (ULTIMATE GOLD-ACCENTED VECTOR ART)
// ==========================================

@Composable
fun BuildOnSiteLogo(
    modifier: Modifier = Modifier.size(120.dp),
    darkTheme: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoMachinery")
    val rotAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logoCompassRotate"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val strokeScale = w / 120f

            // Radial base metallic gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (darkTheme) {
                        listOf(Color(0xFF1E293B), Color(0xFF030712))
                    } else {
                        listOf(Color(0xFFFEF08A).copy(alpha = 0.2f), Color(0xFFE2E8F0))
                    }
                ),
                radius = w * 0.5f
            )

            // Double swept gilded golden ring edge border with 3D bevel effect
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GoldLight,
                        GoldDark,
                        GoldMetallic,
                        GoldLight
                    )
                ),
                radius = w * 0.48f,
                style = Stroke(width = (2.5f * strokeScale).coerceAtLeast(1f))
            )

            // Inner high-fidelity safety outline
            drawCircle(
                color = if (darkTheme) NeonCyan.copy(alpha = 0.15f) else Color(0x330284C7),
                radius = w * 0.44f,
                style = Stroke(width = 0.8f * strokeScale)
            )

            // Rotating structural coordinate compass layout grid
            val gridAlpha = if (darkTheme) 0.09f else 0.16f
            val gridColor = if (darkTheme) NeonCyan else Color(0xFF0284C7)
            
            // Draw compass degree markers around inner rim
            for (degree in 0 until 360 step 45) {
                val rad = Math.toRadians((degree + rotAngle).toDouble())
                val startX = w * 0.5f + cos(rad).toFloat() * (w * 0.38f)
                val startY = h * 0.5f + sin(rad).toFloat() * (h * 0.38f)
                val endX = w * 0.5f + cos(rad).toFloat() * (w * 0.42f)
                val endY = h * 0.5f + sin(rad).toFloat() * (h * 0.42f)
                drawLine(
                    color = gridColor.copy(alpha = 0.28f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1f
                )
            }

            // Blueprint grid mesh background
            for (i in 1..4) {
                val x = w * (i * 0.2f)
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(x, 0.05f * h), Offset(x, 0.95f * h), strokeWidth = 0.8f)
                val y = h * (i * 0.2f)
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(0.05f * w, y), Offset(0.95f * w, y), strokeWidth = 0.8f)
            }

            // Rising Skyscraper core structural towers (3 Pillars representing Builders, Finance, & Assets)
            val baseLineY = h * 0.76f
            val tw = w * 0.08f
            
            val t1Left = w * 0.34f
            val t2Left = w * 0.46f
            val t3Left = w * 0.58f

            // Tower 1 (Tech Assets - Steel blue)
            drawRect(
                color = if (darkTheme) Color(0xFF475569) else Color(0xFF64748B),
                topLeft = Offset(t1Left, h * 0.35f),
                size = Size(tw, baseLineY - h * 0.35f)
            )
            // Tower 2 (The Central Apex - Golden GoldMetallic)
            drawRect(
                color = if (darkTheme) GoldDark else GoldMetallic,
                topLeft = Offset(t2Left, h * 0.24f),
                size = Size(tw, baseLineY - h * 0.24f)
            )
            // Tower 3 (Labor Strength - Rich Navy Indigo)
            drawRect(
                color = if (darkTheme) Color(0xFF334155) else Color(0xFF1E293B),
                topLeft = Offset(t3Left, h * 0.42f),
                size = Size(tw, baseLineY - h * 0.42f)
            )

            // Tower window arrays with luminous neon green and warm solar amber
            val towers = listOf(
                Triple(t1Left, h * 0.35f, 4),
                Triple(t2Left, h * 0.24f, 6),
                Triple(t3Left, h * 0.42f, 3)
            )
            towers.forEach { (tx, ty, floors) ->
                val flH = (baseLineY - ty) / floors
                for (fl in 0 until floors) {
                    val currY = ty + fl * flH
                    // Glowing laser telemetry outline for each floor slab
                    drawLine(
                        color = if (darkTheme) NeonCyan.copy(alpha = 0.6f) else Color(0xFF0EA5E9),
                        start = Offset(tx - 1f, currY),
                        end = Offset(tx + tw + 1f, currY),
                        strokeWidth = 0.8f
                    )
                    // Windows
                    drawRect(
                        color = if (fl % 2 == 0) NeonGreen.copy(alpha = 0.7f) else NeonAmber.copy(alpha = 0.7f),
                        topLeft = Offset(tx + tw * 0.2f, currY + flH * 0.25f),
                        size = Size(tw * 0.6f, flH * 0.5f)
                    )
                }
            }

            // Gilded heavy construction crane structure
            val cX = w * 0.18f
            val cTopY = h * 0.18f
            val cRightArmX = w * 0.90f
            
            // Tower Mast column truss
            drawLine(
                color = GoldMetallic,
                start = Offset(cX, baseLineY),
                end = Offset(cX, cTopY),
                strokeWidth = 1.8f * strokeScale
            )
            // Long jib horizontal arm boom
            drawLine(
                color = GoldMetallic,
                start = Offset(w * 0.04f, cTopY),
                end = Offset(cRightArmX, cTopY),
                strokeWidth = 1.5f * strokeScale
            )
            // Diagonal structural trusses for maximum engineering authenticity
            for (cxShift in 0..6) {
                val step = (cRightArmX - cX) / 7
                val currPivot = cX + cxShift * step
                drawLine(
                    color = GoldDark,
                    start = Offset(currPivot, cTopY),
                    end = Offset(currPivot + step * 0.5f, cTopY + h * 0.05f),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = GoldDark,
                    start = Offset(currPivot + step * 0.5f, cTopY + h * 0.05f),
                    end = Offset(currPivot + step, cTopY),
                    strokeWidth = 0.8f
                )
            }

            // Heavy duty steel wire sling crane rope carrying golden asset weight
            val slingX = w * 0.52f
            drawLine(
                color = if (darkTheme) TextSecondary else Color.Gray,
                start = Offset(slingX, cTopY),
                end = Offset(slingX, h * 0.24f),
                strokeWidth = 1f
            )
            // Drawing hooks
            drawCircle(
                color = Color.White,
                radius = 1.5f * strokeScale,
                center = Offset(slingX, h * 0.24f)
            )

            // Heavy equipment soil/concrete texture base block
            drawRoundRect(
                color = if (darkTheme) Color(0x3B334155) else Color(0x2864748B),
                topLeft = Offset(w * 0.12f, baseLineY),
                size = Size(w * 0.76f, h * 0.06f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

// ==========================================
// 9. NEW COMPONENT: PREMIUM STATUS BADGE (CHROME-GLOW DESIGN STRIP)
// ==========================================

@Composable
fun PremiumStatusBadge(
    label: String,
    statusType: String, // "Success", "Pending", "Warning", "Danger", "Premium"
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeStatusGlow")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "statusDotAlpha"
    )

    val (badgeBgColor, badgeBorderColor, badgeTextColor, badgeDotColor) = when (statusType) {
        "Success" -> Quadruple(Color(0x1E10B981), Color(0x4D10B981), Color(0xFF10B981), Color(0xFF10B981))
        "Pending" -> Quadruple(Color(0x1EF59E0B), Color(0x4DF59E0B), Color(0xFFF59E0B), Color(0xFFF59E0B))
        "Warning" -> Quadruple(Color(0x1EFCD34D), Color(0x4DFCD34D), Color(0xFFEAB308), Color(0xFFFFD700))
        "Danger" -> Quadruple(Color(0x1EF43F5E), Color(0x4DF43F5E), Color(0xFFF43F5E), Color(0xFFF43F5E))
        "Premium" -> Quadruple(Color(0x1F8B5CF6), Color(0x4D8B5CF6), Color(0xFFA78BFA), Color(0xFFEC4899))
        else -> Quadruple(Color(0x1F06B6D4), Color(0x4D06B6D4), Color(0xFF06B6D4), Color(0xFF06B6D4))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBgColor)
            .border(BorderStroke(1.dp, badgeBorderColor), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Living pulsing dot indicator
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawCircle(color = badgeDotColor.copy(alpha = dotAlpha))
                }
        )
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = badgeTextColor,
            letterSpacing = 0.5.sp
        )
    }
}

// Helper tuple matching style layout
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
