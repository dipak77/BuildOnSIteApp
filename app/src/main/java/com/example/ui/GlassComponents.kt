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

// ==========================================
// 1. DYNAMIC NEBULA GRADIENT BACKGROUND
// ==========================================

@Composable
fun GlassAtmosphereBox(
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (darkTheme) GlassBackgroundDark else GlassBackgroundLight)
            .drawBehind {
                if (darkTheme) {
                    // Draw a rich Neon Cyan bubble top-left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.1f, size.height * 0.15f),
                            radius = size.width * 0.75f
                        )
                    )
                    // Draw a rich Neon Purple bubble bottom-right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.85f),
                            radius = size.width * 0.75f
                        )
                    )
                } else {
                    // Elegant Luxury Sunrise aurora flow for 100-billion-dollar premium styling
                    // 1. Soft glowing sky blue in the top-right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFBAE6FD).copy(alpha = 0.42f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.12f),
                            radius = size.width * 0.75f
                        )
                    )
                    // 2. Sophisticated blush pink in the middle-left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFBCFE8).copy(alpha = 0.32f), Color.Transparent),
                            center = Offset(size.width * 0.12f, size.height * 0.52f),
                            radius = size.width * 0.75f
                        )
                    )
                    // 3. Luxurious golden amber in the bottom-right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFEF08A).copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(size.width * 0.88f, size.height * 0.88f),
                            radius = size.width * 0.7f
                        )
                    )
                }
            },
        content = content
    )
}

// ==========================================
// 2. FROSTED-GLASS CARD
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
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "clickScale"
    )

    // Frosted colors - Ultra-clean satin white with rich contrast for light theme
    val bg = if (darkTheme) {
        Color(0x2E111827) // Slate 900 tint 18% alpha
    } else {
        Color(0xF0FAFAFC) // Translucent light alabaster/white
    }

    // Pro-level elegant borders
    val defaultBorder = if (darkTheme) GlassBorderDark else Color(0x2E6366F1) // Translucent luxury indigo stroke
    val borderStroke = BorderStroke(1.dp, borderColor ?: defaultBorder)

    val contentModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawBehind {
            if (darkTheme && glowColor != null) {
                // Symmetrical neon atmospheric aura behind card
                drawCircle(
                    color = glowColor.copy(alpha = 0.08f),
                    radius = size.maxDimension * 0.42f,
                    center = center
                )
            } else if (!darkTheme) {
                // Soft luxury ambient studio drop shadow aura for light theme
                val shadowColor = glowColor?.copy(alpha = 0.05f) ?: Color(0xFF6366F1).copy(alpha = 0.04f)
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(-4f, 2f),
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
        border = borderStroke,
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
// 3. TACTILE NEON-GLOW BUTTONS
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

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    // In light theme, the primary action button background should be deep/rich
    val resolvedGlowColor = if (!darkTheme && glowColor == NeonCyan) {
        Color(0xFF0369A1) // Sky 700 for beautiful high-contrast text contrast in light theme
    } else {
        glowColor
    }

    val bgBrush = if (outlineMode) {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    } else {
        Brush.linearGradient(
            colors = listOf(
                resolvedGlowColor.copy(alpha = 0.92f),
                resolvedGlowColor.mix(Color.Black, 0.15f).copy(alpha = 0.92f)
            )
        )
    }

    // High contrast premium text selection over solid button surfaces
    val resolvedTextColor = if (outlineMode) {
        resolvedGlowColor
    } else {
        if (resolvedGlowColor == NeonCyan || resolvedGlowColor == NeonAmber || resolvedGlowColor == Color(0xFFFCD34D) || resolvedGlowColor == Color(0xFFF59E0B)) {
            Color(0xFF0F172A) // Slate 900 for lighter yellow/amber backgrounds
        } else {
            Color.White // Pristine crisp white for solid indigo/purple buttons
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
                    drawCircle(
                        color = resolvedGlowColor.copy(alpha = if (isPressed) 0.4f else 0.22f),
                        radius = size.width * 0.55f,
                        center = center
                    )
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
                Row(content = content)
            }
        }
    }
}

// Multiplies or mixes colors
private fun Color.mix(other: Color, ratio: Float): Color {
    return Color(
        red = this.red * (1 - ratio) + other.red * ratio,
        green = this.green * (1 - ratio) + other.green * ratio,
        blue = this.blue * (1 - ratio) + other.blue * ratio,
        alpha = this.alpha * (1 - ratio) + other.alpha * ratio
    )
}

// ==========================================
// 4. FROSTED TEXT FIELDS WITH NEON OUTLINES
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
    val containerBg = if (darkTheme) Color(0x1C111827) else Color(0x40FFFFFF)
    val textC = if (darkTheme) TextPrimary else TextPrimaryLight
    val labelC = if (darkTheme) TextSecondary else TextSecondaryLight

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        textStyle = LocalTextStyle.current.copy(color = textC, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumeric) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text
        ),
        leadingIcon = if (icon != null) {
            { Icon(imageVector = icon, contentDescription = null, tint = focusedStroke) }
        } else null,
        label = { Text(text = label, color = labelC) },
        placeholder = { Text(text = placeholder, color = labelC.copy(alpha = 0.5f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerBg,
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
    // Resolve vibrant contrast active colors for light theme to meet premium standards
    val resolvedActiveColor = if (!darkTheme) {
        if (activeColor == NeonCyan) {
            Color(0xFF0284C7) // Professional light ocean blue
        } else if (activeColor == NeonPurple) {
            Color(0xFF6D28D9) // Luxury Royal purple
        } else if (activeColor == NeonGreen) {
            Color(0xFF047857) // Deep jade green
        } else if (activeColor == NeonPink) {
            Color(0xFFBE123C) // Rich crimson rose
        } else {
            activeColor
        }
    } else {
        activeColor
    }

    val bg = if (selected) {
        resolvedActiveColor.copy(alpha = if (darkTheme) 0.25f else 0.15f)
    } else {
        if (darkTheme) Color(0x1FFFFFFF) else Color(0x0F111827)
    }

    val borderC = if (selected) {
        resolvedActiveColor
    } else {
        if (darkTheme) Color(0x1F9CA3AF) else Color(0x14111827)
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
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
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

    // Glowing track overlay
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(if (darkTheme) Color(0x1AFFFFFF) else Color(0x1A000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progressAnim)
                .clip(RoundedCornerShape(99.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(glowColor.copy(alpha = 0.6f), glowColor)
                    )
                )
                .drawBehind {
                    if (darkTheme) {
                        drawCircle(
                            color = glowColor.copy(alpha = 0.4f),
                            radius = size.height * 1.5f,
                            center = Offset(size.width, size.height / 2f)
                        )
                    }
                }
        )
    }
}

// ==========================================
// 7. RESPONSIVE FROSTED DIALOG / BOTTOM SHEET
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
                        drawCircle(
                            color = glowColor.copy(alpha = 0.12f),
                            radius = size.maxDimension * 0.55f,
                            center = center
                        )
                    }
                }
                .clip(RoundedCornerShape(24.dp))
                .background(if (darkTheme) Color(0xED0B0F19) else Color(0xEDF9FAFB)) // Dark Slate deep glass
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                glowColor.copy(alpha = 0.5f),
                                if (darkTheme) Color(0x18FFFFFF) else Color(0x18111827)
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
                // Header row with Close button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = if (darkTheme) TextPrimary else TextPrimaryLight,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (darkTheme) TextSecondary else TextSecondaryLight
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
// 8. PROCEDURAL BRANDING LOGO COMPONENT
// ==========================================

@Composable
fun BuildOnSiteLogo(
    modifier: Modifier = Modifier.size(120.dp),
    darkTheme: Boolean = true
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val strokeScale = w / 120f

            // Radial background gradient inside the circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (darkTheme) {
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    } else {
                        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                    }
                ),
                radius = w * 0.5f
            )

            // Outer sweep gradient border
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFCD34D), // Golden Yellow
                        Color(0xFFF59E0B), // Secondary Gold
                        Color(0xFFD97706), // Rich Amber
                        Color(0xFFFCD34D)
                    )
                ),
                radius = w * 0.48f,
                style = Stroke(width = (2.5f * strokeScale).coerceAtLeast(1f))
            )

            // Architectural blueprint grid overlay (subtle)
            val gridAlpha = if (darkTheme) 0.08f else 0.15f
            val gridColor = if (darkTheme) NeonCyan else Color(0xFF0284C7)
            for (i in 1..4) {
                val x = w * (i * 0.2f)
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                val y = h * (i * 0.2f)
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }

            // Rising Skyscraper core layout
            val bLeft = w * 0.44f
            val bRight = w * 0.72f
            val bWidth = (bRight - bLeft).coerceAtLeast(0f)
            val bTop = h * 0.16f
            val bHeight = (h * 0.64f).coerceAtLeast(0f)
            
            drawRect(
                color = if (darkTheme) Color(0xFF334155) else Color(0xFF94A3B8),
                topLeft = Offset(bLeft, bTop),
                size = Size(bWidth, bHeight * 0.8f)
            )
            
            // Alternating floors glowing blueprint highlights
            val numFloors = 4
            val floorHeight = ((bHeight * 0.8f) / numFloors).coerceAtLeast(0f)
            for (f in 0 until numFloors) {
                val fTop = bTop + f * floorHeight
                val isAlt = f % 2 == 0
                val col = if (isAlt) Color(0xFFD97706).copy(alpha = 0.35f) else Color(0xFF0EA5E9).copy(alpha = 0.3f)
                drawRect(
                    color = col,
                    topLeft = Offset(bLeft + 2f, fTop + 2f),
                    size = Size((bWidth - 4f).coerceAtLeast(0f), (floorHeight - 4f).coerceAtLeast(0f))
                )
                
                // Translucent windows slots
                val winW = ((bWidth - 12f) / 3f).coerceAtLeast(0f)
                val winH = ((floorHeight - 8f) / 2f).coerceAtLeast(0f)
                for (wx in 0..2) {
                    for (wy in 0..1) {
                        if (winW > 0f && winH > 0f) {
                            drawRect(
                                color = if (darkTheme) Color(0xFF0F172A) else Color.White,
                                topLeft = Offset(
                                    bLeft + 4f + wx * (winW + 2f),
                                    fTop + 3f + wy * (winH + 2f)
                                ),
                                size = Size(winW, winH)
                            )
                        }
                    }
                }
            }

            // Upper Scaffolding poles under-construction setup
            val sY = bTop - (h * 0.09f)
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(bLeft + bWidth * 0.2f, bTop),
                end = Offset(bLeft + bWidth * 0.2f, sY),
                strokeWidth = 1.5f * strokeScale
            )
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(bLeft + bWidth * 0.8f, bTop),
                end = Offset(bLeft + bWidth * 0.8f, sY),
                strokeWidth = 1.5f * strokeScale
            )
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(bLeft + bWidth * 0.2f, bTop),
                end = Offset(bLeft + bWidth * 0.8f, sY),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(bLeft + bWidth * 0.8f, bTop),
                end = Offset(bLeft + bWidth * 0.2f, sY),
                strokeWidth = 1f
            )

            // High-precision tower crane boom
            val cX = w * 0.24f
            val cTopY = h * 0.10f
            val cLeftArmX = w * 0.08f
            val cRightArmX = w * 0.86f
            
            // Principal vertical mast mast
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(cX, h * 0.72f),
                end = Offset(cX, cTopY),
                strokeWidth = 2.5f * strokeScale
            )
            // Long horizontal boom jib
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(cLeftArmX, cTopY),
                end = Offset(cRightArmX, cTopY),
                strokeWidth = 2f * strokeScale
            )
            // Lattice frame counterweights
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(cX, cTopY + h * 0.12f),
                end = Offset(cX + w * 0.12f, cTopY),
                strokeWidth = 1.2f
            )
            // Wire rope sling lifting line
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(w * 0.58f, cTopY),
                end = Offset(w * 0.58f, bTop + h * 0.04f),
                strokeWidth = 1f
            )
            // Load block pulley crane hook
            drawCircle(
                color = Color(0xFF475569),
                radius = 2f * strokeScale,
                center = Offset(w * 0.58f, bTop + h * 0.04f)
            )

            // Classic safety golden yellow hardhat
            val hatX = w * 0.26f
            val hatY = h * 0.64f
            val hatR = (w * 0.13f).coerceAtLeast(0f)
            if (hatR > 0f) {
                // Hardhat shell
                drawArc(
                    color = Color(0xFFF59E0B),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(hatX - hatR, hatY - hatR),
                    size = Size(hatR * 2f, hatR * 2f)
                )
                // Hardhat protective brim
                drawRoundRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(hatX - hatR * 1.2f, hatY - 1f),
                    size = Size((hatR * 2.4f).coerceAtLeast(0f), (h * 0.025f).coerceAtLeast(0f)),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Center ridge highlight
                drawArc(
                    color = Color.White,
                    startAngle = 220f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(hatX - hatR * 0.35f, hatY - hatR * 0.96f),
                    size = Size((hatR * 0.7f).coerceAtLeast(0f), (hatR * 0.45f).coerceAtLeast(0f))
                )
            }
        }
    }
}

