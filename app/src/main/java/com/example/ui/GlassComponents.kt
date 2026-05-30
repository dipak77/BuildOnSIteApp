package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassAtmosphereBox(
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NebulaAtmosphere")
    val breathingValue by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8200, easing = LinearOutSlowInEasing),
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
                    val shiftX1 = cos(floatingOffsetAngle) * (w * 0.05f)
                    val shiftY1 = sin(floatingOffsetAngle) * (h * 0.04f)
                    val shiftX2 = sin(floatingOffsetAngle * 1.5f) * (w * 0.04f)
                    val shiftY2 = cos(floatingOffsetAngle * 1.5f) * (h * 0.05f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.22f * breathingValue), Color.Transparent),
                            center = Offset(w * 0.12f + shiftX1, h * 0.14f + shiftY1),
                            radius = w * 0.80f * breathingValue
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.20f * (2f - breathingValue)), Color.Transparent),
                            center = Offset(w * 0.88f + shiftX2, h * 0.86f + shiftY2),
                            radius = w * 0.82f * (2f - breathingValue)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GoldMetallic.copy(alpha = 0.09f), Color.Transparent),
                            center = Offset(w * 0.35f - shiftX1 * 0.5f, h * 0.60f - shiftY2 * 0.5f),
                            radius = w * 0.50f
                        )
                    )

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

                    val starPositions = listOf(
                        Offset(w * 0.20f, h * 0.25f),
                        Offset(w * 0.75f, h * 0.18f),
                        Offset(w * 0.15f, h * 0.75f),
                        Offset(w * 0.80f, h * 0.65f),
                        Offset(w * 0.45f, h * 0.40f)
                    )
                    starPositions.forEachIndexed { idx, pos ->
                        val starAlpha = (0.24f + 0.18f * sin(floatingOffsetAngle * 2f + idx * 1.5f)).coerceIn(0.08f, 0.52f)
                        drawCircle(color = NeonCyan.copy(alpha = starAlpha), radius = 1.5.dp.toPx(), center = pos)
                    }
                } else {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFBAE6FD).copy(alpha = 0.50f), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.12f),
                            radius = w * 0.80f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFBCFE8).copy(alpha = 0.40f), Color.Transparent),
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
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "glassCardScale"
    )

    val activeBorderColor = glowColor ?: if (darkTheme) NeonCyan else Color(0xFF6366F1)
    val bg = if (darkTheme) Color(0x52090D1A) else Color(0xD9FFFFFF)
    val borderBrush = if (borderColor != null) {
        Brush.linearGradient(listOf(borderColor, borderColor))
    } else if (darkTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                activeBorderColor.copy(alpha = 0.42f),
                Color.White.copy(alpha = 0.10f)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x344F46E5),
                activeBorderColor.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.75f)
            )
        )
    }

    val cardModifier = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .drawBehind {
            val aura = glowColor ?: if (darkTheme) NeonCyan else Color(0xFF6366F1)
            if (darkTheme) {
                drawCircle(color = aura.copy(alpha = if (isPressed) 0.12f else 0.07f), radius = size.maxDimension * 0.45f, center = center)
            } else {
                drawRoundRect(
                    color = aura.copy(alpha = 0.055f),
                    topLeft = Offset(-4f, 6f),
                    size = Size(size.width + 8f, size.height + 8f),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
        }
        .clip(RoundedCornerShape(22.dp))
        .background(bg)
        .then(
            if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ) else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, borderBrush),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

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
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val pulseTransition = rememberInfiniteTransition(label = "ButtonPulseGlow")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "buttonPulseAlpha"
    )
    val sweepOffset by pulseTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(animation = tween(2800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "buttonSweepOffset"
    )

    val resolvedGlowColor = if (!darkTheme && glowColor == NeonCyan) Color(0xFF0369A1) else glowColor
    val gradientColors = when (glowColor) {
        NeonCyan -> GradientCosmicBlue
        NeonPurple -> GradientLuxuryPurple
        NeonAmber -> GradientSunsetAmber
        GoldLight, GoldMetallic -> GradientMetallicGold
        else -> listOf(resolvedGlowColor, resolvedGlowColor.mix(Color.Black, 0.20f))
    }
    val bgBrush = if (outlineMode) Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)) else Brush.linearGradient(gradientColors)
    val resolvedTextColor = if (outlineMode) {
        resolvedGlowColor
    } else if (resolvedGlowColor == NeonCyan || resolvedGlowColor == NeonAmber || resolvedGlowColor == GoldLight || resolvedGlowColor == GoldMetallic) {
        Color(0xFF070B14)
    } else Color.White

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .drawBehind {
                if (darkTheme && !outlineMode && enabled) {
                    drawCircle(color = resolvedGlowColor.copy(alpha = pulseAlpha), radius = size.width * 0.58f, center = center)
                }
            }
            .clip(RoundedCornerShape(99.dp))
            .background(if (enabled) bgBrush else Brush.linearGradient(listOf(Color(0x339CA3AF), Color(0x339CA3AF))))
            .then(if (outlineMode) Modifier.background(if (darkTheme) Color(0x1F111827) else Color(0x1FAFB8C8)) else Modifier)
            .then(
                if (outlineMode) Modifier.border(
                    BorderStroke(1.5.dp, if (enabled) resolvedGlowColor.copy(alpha = 0.8f) else Color(0x4D9CA3AF)),
                    RoundedCornerShape(99.dp)
                ) else Modifier
            )
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
            .drawWithContent {
                drawContent()
                if (enabled && !outlineMode) {
                    val brushWidth = size.width * 0.35f
                    val sweepCenter = size.width * sweepOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0f)),
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = resolvedTextColor)
                Spacer(modifier = Modifier.width(8.dp))
            }
            CompositionLocalProvider(LocalContentColor provides resolvedTextColor) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, content = content)
            }
        }
    }
}

private fun Color.mix(other: Color, ratio: Float): Color = Color(
    red = red * (1 - ratio) + other.red * ratio,
    green = green * (1 - ratio) + other.green * ratio,
    blue = blue * (1 - ratio) + other.blue * ratio,
    alpha = alpha * (1 - ratio) + other.alpha * ratio
)

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
    val containerBg = if (darkTheme) Color(0x280B0F19) else Color(0x75FFFFFF)
    val textC = if (darkTheme) TextPrimary else TextPrimaryLight
    val labelC = if (darkTheme) TextSecondary else TextSecondaryLight

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        textStyle = LocalTextStyle.current.copy(color = textC, fontSize = 15.sp, fontWeight = FontWeight.Medium),
        keyboardOptions = KeyboardOptions(keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text),
        leadingIcon = if (icon != null) ({ Icon(imageVector = icon, contentDescription = null, tint = focusedStroke) }) else null,
        label = { Text(text = label, color = labelC, fontWeight = FontWeight.SemiBold) },
        placeholder = { Text(text = placeholder, color = labelC.copy(alpha = 0.48f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (darkTheme) Color(0x3B070A13) else Color(0x99FFFFFF),
            unfocusedContainerColor = containerBg,
            focusedBorderColor = focusedStroke,
            unfocusedBorderColor = if (darkTheme) GlassBorderDark else GlassBorderLight,
            cursorColor = focusedStroke,
            focusedLabelColor = focusedStroke,
            unfocusedLabelColor = labelC
        )
    )
}

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
    } else activeColor

    val bg = if (selected) resolvedActiveColor.copy(alpha = if (darkTheme) 0.28f else 0.18f) else if (darkTheme) Color(0x1AFFFFFF) else Color(0x0C111827)
    val borderC = if (selected) resolvedActiveColor else if (darkTheme) Color(0x1F9CA3AF) else Color(0x12111827)
    val textC = if (selected) resolvedActiveColor else if (darkTheme) TextSecondary else TextSecondaryLight

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
        Text(text = text, color = textC, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GlassProgressBar(
    progress: Float,
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
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "progressShimmerOffset"
    )

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
                .background(Brush.horizontalGradient(listOf(glowColor.copy(alpha = 0.55f), glowColor, glowColor.copy(alpha = 0.82f))))
                .drawBehind {
                    if (darkTheme) drawCircle(color = glowColor.copy(alpha = 0.45f), radius = size.height * 1.6f, center = Offset(size.width, size.height / 2f))
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dx = size.width * shimmerOffset
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0f)),
                        startX = dx,
                        endX = dx + 40.dp.toPx()
                    )
                )
            }
        }
    }
}

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
                    if (darkTheme) drawCircle(color = glowColor.copy(alpha = 0.15f), radius = size.maxDimension * 0.58f, center = center)
                }
                .clip(RoundedCornerShape(26.dp))
                .background(if (darkTheme) Color(0xF2090D1A) else Color(0xFAF8FAFC))
                .border(
                    BorderStroke(1.5.dp, Brush.verticalGradient(listOf(glowColor.copy(alpha = 0.65f), if (darkTheme) Color(0x30FFFFFF) else Color(0x30111827)))),
                    RoundedCornerShape(26.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(42.dp).height(4.dp).clip(CircleShape).background(if (darkTheme) Color(0x3DFFFFFF) else Color(0x24000000))
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(glowColor))
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
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (darkTheme) Color(0x1AFFFFFF) else Color(0x0D000000))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = if (darkTheme) TextSecondary else TextSecondaryLight, modifier = Modifier.size(16.dp))
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp), content = content)
            }
        }
    }
}

@Composable
fun BuildOnSiteLogo(
    modifier: Modifier = Modifier.size(120.dp),
    darkTheme: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoMachinery")
    val rotAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(30000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "logoCompassRotate"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            val strokeScale = w / 120f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (darkTheme) listOf(Color(0xFF1E293B), Color(0xFF030712)) else listOf(Color(0xFFFEF08A).copy(alpha = 0.2f), Color(0xFFE2E8F0)),
                    center = center,
                    radius = w * 0.5f
                ),
                radius = w * 0.5f
            )
            drawCircle(
                brush = Brush.sweepGradient(listOf(GoldLight, GoldDark, GoldMetallic, GoldLight)),
                radius = w * 0.48f,
                style = Stroke(width = (2.5f * strokeScale).coerceAtLeast(1f))
            )
            drawCircle(
                color = if (darkTheme) NeonCyan.copy(alpha = 0.15f) else Color(0x330284C7),
                radius = w * 0.44f,
                style = Stroke(width = 0.8f * strokeScale)
            )

            val gridAlpha = if (darkTheme) 0.09f else 0.16f
            val gridColor = if (darkTheme) NeonCyan else Color(0xFF0284C7)
            for (degree in 0 until 360 step 45) {
                val rad = Math.toRadians((degree + rotAngle).toDouble())
                val startX = w * 0.5f + cos(rad).toFloat() * (w * 0.38f)
                val startY = h * 0.5f + sin(rad).toFloat() * (h * 0.38f)
                val endX = w * 0.5f + cos(rad).toFloat() * (w * 0.42f)
                val endY = h * 0.5f + sin(rad).toFloat() * (h * 0.42f)
                drawLine(gridColor.copy(alpha = 0.28f), Offset(startX, startY), Offset(endX, endY), strokeWidth = 1f)
            }
            for (i in 1..4) {
                val x = w * (i * 0.2f)
                val y = h * (i * 0.2f)
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(x, 0.05f * h), Offset(x, 0.95f * h), strokeWidth = 0.8f)
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(0.05f * w, y), Offset(0.95f * w, y), strokeWidth = 0.8f)
            }

            val baseLineY = h * 0.76f
            val tw = w * 0.08f
            val t1Left = w * 0.34f
            val t2Left = w * 0.46f
            val t3Left = w * 0.58f
            drawRect(color = if (darkTheme) Color(0xFF475569) else Color(0xFF64748B), topLeft = Offset(t1Left, h * 0.35f), size = Size(tw, baseLineY - h * 0.35f))
            drawRect(color = if (darkTheme) GoldDark else GoldMetallic, topLeft = Offset(t2Left, h * 0.24f), size = Size(tw, baseLineY - h * 0.24f))
            drawRect(color = if (darkTheme) Color(0xFF334155) else Color(0xFF1E293B), topLeft = Offset(t3Left, h * 0.42f), size = Size(tw, baseLineY - h * 0.42f))

            val towers = listOf(Triple(t1Left, h * 0.35f, 4), Triple(t2Left, h * 0.24f, 6), Triple(t3Left, h * 0.42f, 3))
            towers.forEach { (tx, ty, floors) ->
                val flH = (baseLineY - ty) / floors
                for (fl in 0 until floors) {
                    val currY = ty + fl * flH
                    drawLine(color = if (darkTheme) NeonCyan.copy(alpha = 0.6f) else Color(0xFF0EA5E9), start = Offset(tx - 1f, currY), end = Offset(tx + tw + 1f, currY), strokeWidth = 0.8f)
                    drawRect(color = if (fl % 2 == 0) NeonGreen.copy(alpha = 0.7f) else NeonAmber.copy(alpha = 0.7f), topLeft = Offset(tx + tw * 0.2f, currY + flH * 0.25f), size = Size(tw * 0.6f, flH * 0.5f))
                }
            }

            val cX = w * 0.18f
            val cTopY = h * 0.18f
            val cRightArmX = w * 0.90f
            drawLine(GoldMetallic, Offset(cX, baseLineY), Offset(cX, cTopY), strokeWidth = 1.8f * strokeScale)
            drawLine(GoldMetallic, Offset(w * 0.04f, cTopY), Offset(cRightArmX, cTopY), strokeWidth = 1.5f * strokeScale)
            for (cxShift in 0..6) {
                val step = (cRightArmX - cX) / 7
                val currPivot = cX + cxShift * step
                drawLine(GoldDark, Offset(currPivot, cTopY), Offset(currPivot + step * 0.5f, cTopY + h * 0.05f), strokeWidth = 0.8f)
                drawLine(GoldDark, Offset(currPivot + step * 0.5f, cTopY + h * 0.05f), Offset(currPivot + step, cTopY), strokeWidth = 0.8f)
            }
            val slingX = w * 0.52f
            drawLine(color = if (darkTheme) TextSecondary else Color.Gray, start = Offset(slingX, cTopY), end = Offset(slingX, h * 0.24f), strokeWidth = 1f)
            drawCircle(color = Color.White, radius = 1.5f * strokeScale, center = Offset(slingX, h * 0.24f))
            drawRoundRect(
                color = if (darkTheme) Color(0x3B334155) else Color(0x2864748B),
                topLeft = Offset(w * 0.12f, baseLineY),
                size = Size(w * 0.76f, h * 0.06f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
fun PremiumStatusBadge(
    label: String,
    statusType: String,
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeStatusGlow")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
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
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).drawBehind { drawCircle(color = badgeDotColor.copy(alpha = dotAlpha)) })
        Text(text = label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = badgeTextColor, letterSpacing = 0.5.sp)
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
