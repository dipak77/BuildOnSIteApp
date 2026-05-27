package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
                    // Soft sunny light ambient gradients
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.1f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.1f),
                            radius = size.width * 0.6f
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

    // Frosted colors
    val bg = if (darkTheme) {
        Color(0x2E111827) // Slate 900 tint 18% alpha
    } else {
        Color(0xBFFFFFFF) // Translucent light white
    }

    val defaultBorder = if (darkTheme) GlassBorderDark else GlassBorderLight
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

    val bgBrush = if (outlineMode) {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    } else {
        Brush.linearGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.85f),
                glowColor.mix(Color.Black, 0.2f).copy(alpha = 0.85f)
            )
        )
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
                        color = glowColor.copy(alpha = if (isPressed) 0.4f else 0.22f),
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
                    BorderStroke(1.5.dp, if (enabled) glowColor.copy(alpha = 0.8f) else Color(0x4D9CA3AF)),
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
                    tint = if (outlineMode) glowColor else Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            CompositionLocalProvider(
                LocalContentColor provides if (outlineMode) glowColor else Color.Black
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
    val bg = if (selected) {
        activeColor.copy(alpha = 0.25f)
    } else {
        if (darkTheme) Color(0x1FFFFFFF) else Color(0x1C111827)
    }

    val borderC = if (selected) {
        activeColor
    } else {
        if (darkTheme) Color(0x1F9CA3AF) else Color(0x1F4B5563)
    }

    val textC = if (selected) {
        activeColor
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
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(minWidth = 50.dp, minHeight = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        val rawWidth = maxWidth.value
        val safeWidth = if (rawWidth.isNaN() || !rawWidth.isFinite() || rawWidth <= 0f) 120f else rawWidth
        val scale = (safeWidth / 120f).coerceIn(0.1f, 10f)
        
        // Circular emblem badge background
        Box(
            modifier = Modifier
                .size(maxWidth)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (darkTheme) {
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                        }
                    )
                )
                .border(
                    (3 * scale).dp.coerceAtLeast(1.dp),
                    Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFCD34D), // Golden Yellow
                            Color(0xFFF59E0B), // Secondary Gold
                            Color(0xFFD97706), // Rich Amber
                            Color(0xFFFCD34D)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Procedural drawings of the active skyline crane machinery & safety gear
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                if (w <= 0f || h <= 0f) return@Canvas
                
                // Architectural grid overlay
                val gridAlpha = if (darkTheme) 0.08f else 0.15f
                val gridColor = if (darkTheme) NeonCyan else Color(0xFF0284C7)
                for (i in 1..4) {
                    val x = w * (i * 0.2f)
                    drawLine(gridColor.copy(alpha = gridAlpha), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    val y = h * (i * 0.2f)
                    drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }

                // Rising Skyscraper skeleton layout
                val bLeft = w * 0.44f
                val bRight = w * 0.72f
                val bWidth = (bRight - bLeft).coerceAtLeast(0f)
                val bTop = h * 0.16f
                val bHeight = (h * 0.64f).coerceAtLeast(0f)
                
                // Skyscraper structural blocks
                drawRect(
                    color = if (darkTheme) Color(0xFF334155) else Color(0xFF94A3B8),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(bWidth, bHeight * 0.8f)
                )
                
                // Horizontal construction floors highlights
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
                    
                    // Windows details
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

                // Scaffolding poles on high floors
                val sY = bTop - (h * 0.09f)
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(bLeft + bWidth * 0.2f, bTop),
                    end = Offset(bLeft + bWidth * 0.2f, sY),
                    strokeWidth = 1.5f * scale
                )
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(bLeft + bWidth * 0.8f, bTop),
                    end = Offset(bLeft + bWidth * 0.8f, sY),
                    strokeWidth = 1.5f * scale
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

                // High-strength Tower Crane (Yellow)
                val cX = w * 0.24f
                val cTopY = h * 0.10f
                val cLeftArmX = w * 0.08f
                val cRightArmX = w * 0.86f
                
                // Crane core support mast
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(cX, h * 0.72f),
                    end = Offset(cX, cTopY),
                    strokeWidth = 2.5f * scale
                )
                // Horizontal work jib
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(cLeftArmX, cTopY),
                    end = Offset(cRightArmX, cTopY),
                    strokeWidth = 2f * scale
                )
                // Lattice tension link
                drawLine(
                    color = Color(0xFFD97706),
                    start = Offset(cX, cTopY + h * 0.12f),
                    end = Offset(cX + w * 0.12f, cTopY),
                    strokeWidth = 1.2f
                )
                // Steel hoist line extending down to the building
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(w * 0.58f, cTopY),
                    end = Offset(w * 0.58f, bTop + h * 0.04f),
                    strokeWidth = 1f
                )
                // Crane Hook
                drawCircle(
                    color = Color(0xFF475569),
                    radius = 2f * scale,
                    center = Offset(w * 0.58f, bTop + h * 0.04f)
                )

                // Yellow Hardhat safety layout (Bottom-left quadrant)
                val hatX = w * 0.26f
                val hatY = h * 0.64f
                val hatR = (w * 0.13f).coerceAtLeast(0f)
                // Dome
                if (hatR > 0f) {
                    drawArc(
                        color = Color(0xFFF59E0B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(hatX - hatR, hatY - hatR),
                        size = Size(hatR * 2f, hatR * 2f)
                    )
                    // Front rim brim
                    drawRoundRect(
                        color = Color(0xFFD97706),
                        topLeft = Offset(hatX - hatR * 1.2f, hatY - 1f),
                        size = Size((hatR * 2.4f).coerceAtLeast(0f), (h * 0.025f).coerceAtLeast(0f)),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    // Safety crest badge highlight
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

            // Lower centered branding badge & system layout
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (12 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Metallic obsidian ConstructPro slab
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .clip(RoundedCornerShape((8 * scale).dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF090D16))
                            )
                        )
                        .border(
                            (1 * scale).dp.coerceAtLeast(0.5.dp),
                            Brush.linearGradient(listOf(NeonCyan, Color(0xFFF59E0B))),
                            RoundedCornerShape((8 * scale).dp)
                        )
                        .padding(vertical = (4 * scale).dp, horizontal = (4 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Construct",
                            fontSize = (11.5f * scale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.2.sp
                        )
                        Text(
                            text = "Pro",
                            fontSize = (12.5f * scale).sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFCD34D),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                // Yellow slogan banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFF59E0B))
                        .padding(vertical = (1 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PAY MANAGEMENT SYSTEM",
                        fontSize = (5.5f * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

