package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .defaultMinSize(minHeight = 48.dp),
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
