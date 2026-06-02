package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

// ─────────────────────────────────────────────────────────────────────────────
// PIN PREFS HELPERS
// ─────────────────────────────────────────────────────────────────────────────
private const val PREFS_NAME = "constructpro_prefs"
private const val KEY_PIN    = "app_security_pin"
private const val KEY_PIN_HASH = "app_security_pin_hash"
private const val KEY_PIN_SALT = "app_security_pin_salt"
private const val KEY_PIN_DISABLED = "app_security_pin_disabled"

private data class PinCredential(
    val hash: String?,
    val salt: String?,
    val disabled: Boolean
)

private fun hashPin(pin: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun newSalt(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun readPinCredential(context: android.content.Context): PinCredential {
    val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    val disabled = prefs.getBoolean(KEY_PIN_DISABLED, false)
    val hash = prefs.getString(KEY_PIN_HASH, null)
    val salt = prefs.getString(KEY_PIN_SALT, null)
    if (disabled || (hash != null && salt != null)) {
        return PinCredential(hash = hash, salt = salt, disabled = disabled)
    }

    val legacyPin = prefs.getString(KEY_PIN, null)
    return when {
        legacyPin == "SKIP" -> {
            prefs.edit()
                .remove(KEY_PIN)
                .putBoolean(KEY_PIN_DISABLED, true)
                .apply()
            PinCredential(hash = null, salt = null, disabled = true)
        }
        legacyPin != null && legacyPin.length == 4 && legacyPin.all { it.isDigit() } -> {
            val migratedSalt = newSalt()
            val migratedHash = hashPin(legacyPin, migratedSalt)
            prefs.edit()
                .remove(KEY_PIN)
                .putString(KEY_PIN_HASH, migratedHash)
                .putString(KEY_PIN_SALT, migratedSalt)
                .putBoolean(KEY_PIN_DISABLED, false)
                .apply()
            PinCredential(hash = migratedHash, salt = migratedSalt, disabled = false)
        }
        else -> PinCredential(hash = null, salt = null, disabled = false)
    }
}

fun savePin(context: android.content.Context, pin: String) {
    val salt = newSalt()
    context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_PIN)
        .putString(KEY_PIN_HASH, hashPin(pin, salt))
        .putString(KEY_PIN_SALT, salt)
        .putBoolean(KEY_PIN_DISABLED, false)
        .apply()
}

private fun disablePin(context: android.content.Context) {
    context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_PIN)
        .remove(KEY_PIN_HASH)
        .remove(KEY_PIN_SALT)
        .putBoolean(KEY_PIN_DISABLED, true)
        .apply()
}

private fun verifyPin(pin: String, credential: PinCredential): Boolean {
    val hash = credential.hash ?: return false
    val salt = credential.salt ?: return false
    return hashPin(pin, salt) == hash
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN PIN SCREEN  (setup ↔ verify)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PinScreen(
    dark: Boolean,
    userName: String,
    onPinVerified: () -> Unit
) {
    val context = LocalContext.current
    val haptic  = LocalHapticFeedback.current
    val credential = remember { readPinCredential(context) }
    val isSetupMode = credential.hash == null && !credential.disabled
    val PIN_LENGTH  = 4

    LaunchedEffect(credential.disabled) {
        if (credential.disabled) {
            onPinVerified()
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────
    var pin         by remember { mutableStateOf("") }
    var confirmPin  by remember { mutableStateOf("") }        // only in setup
    var inConfirm   by remember { mutableStateOf(false) }     // setup step 2
    var errorMsg    by remember { mutableStateOf<String?>(null) }
    var shakeState  by remember { mutableStateOf(false) }
    var unlocked    by remember { mutableStateOf(false) }

    // Shake animation
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeState) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "shake"
    )
    val dotShiftX = remember { Animatable(0f) }
    LaunchedEffect(shakeState) {
        if (shakeState) {
            repeat(4) {
                dotShiftX.animateTo(10f, tween(60))
                dotShiftX.animateTo(-10f, tween(60))
            }
            dotShiftX.animateTo(0f, tween(60))
            delay(200)
            shakeState = false
        }
    }

    // Unlock success animation
    val successScale by animateFloatAsState(
        targetValue = if (unlocked) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "unlock_scale"
    )
    LaunchedEffect(unlocked) {
        if (unlocked) {
            delay(600)
            onPinVerified()
        }
    }

    // ── Colors ────────────────────────────────────────────────────────────────
    val bgGrad = if (dark)
        Brush.verticalGradient(listOf(Color(0xFF020817), Color(0xFF0D1B3E)))
    else
        Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF)))

    val accentBlue   = Color(0xFF00D4FF)
    val accentViolet = Color(0xFF7C3AED)
    val accentGreen  = Color(0xFF00FF87)
    val cardBg = if (dark) Color(0xFF0C1322) else Color.White
    val textColor = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subText = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    val currentPin = if (isSetupMode && inConfirm) confirmPin else pin

    // ── Numpad input logic ────────────────────────────────────────────────────
    fun onKey(digit: String) {
        if (unlocked) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        errorMsg = null
        if (isSetupMode) {
            if (!inConfirm) {
                if (pin.length < PIN_LENGTH) {
                    pin += digit
                    if (pin.length == PIN_LENGTH) {
                        // Move to confirm step automatically
                        inConfirm = true
                    }
                }
            } else {
                if (confirmPin.length < PIN_LENGTH) {
                    confirmPin += digit
                    if (confirmPin.length == PIN_LENGTH) {
                        if (confirmPin == pin) {
                            savePin(context, pin)
                            unlocked = true
                        } else {
                            shakeState = true
                            errorMsg = "PINs don't match. Try again."
                            confirmPin = ""
                        }
                    }
                }
            }
        } else {
            if (pin.length < PIN_LENGTH) {
                pin += digit
                if (pin.length == PIN_LENGTH) {
                    if (verifyPin(pin, credential)) {
                        unlocked = true
                    } else {
                        shakeState = true
                        errorMsg = "Incorrect PIN. Try again."
                        pin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        errorMsg = null
        if (isSetupMode && inConfirm) {
            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
        } else {
            if (pin.isNotEmpty()) pin = pin.dropLast(1)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier.fillMaxSize().background(bgGrad),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // ── Lock icon + title ─────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .scale(successScale)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(accentViolet.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(2.dp, accentViolet.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (unlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (unlocked) accentGreen else accentViolet,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = if (unlocked) "Access Granted!" else if (isSetupMode) "Set Up Your PIN" else "Welcome back,",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (unlocked) accentGreen else textColor
                )
                if (!isSetupMode && !unlocked) {
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentBlue
                    )
                }
                Text(
                    text = when {
                        unlocked     -> "Entering app…"
                        isSetupMode && !inConfirm -> "Choose a 4-digit PIN to secure your app"
                        isSetupMode && inConfirm  -> "Confirm your PIN"
                        else         -> "Enter your 4-digit PIN"
                    },
                    fontSize = 13.sp,
                    color = subText,
                    textAlign = TextAlign.Center
                )
            }

            // ── PIN dots ──────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(x = dotShiftX.value.dp)
            ) {
                val activePinLen = currentPin.length
                repeat(PIN_LENGTH) { idx ->
                    val filled = idx < activePinLen
                    val dotColor = when {
                        unlocked       -> accentGreen
                        errorMsg != null -> Color(0xFFFF6B6B)
                        filled         -> accentViolet
                        else           -> borderColor
                    }
                    val dotSize by animateDpAsState(
                        targetValue = if (filled) 18.dp else 14.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "dot_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(if (filled) dotColor else Color.Transparent)
                            .border(2.dp, dotColor, CircleShape)
                    )
                }
            }

            // ── Error message ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = errorMsg != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Numpad ────────────────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rows = listOf(
                    listOf("1","2","3"),
                    listOf("4","5","6"),
                    listOf("7","8","9"),
                    listOf("","0","⌫")
                )
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { key ->
                            PinKey(
                                label = key,
                                dark = dark,
                                accentColor = accentViolet,
                                enabled = !unlocked,
                                onClick = {
                                    when (key) {
                                        "⌫" -> onBackspace()
                                        ""  -> { /* empty slot */ }
                                        else -> onKey(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ── Skip / Reset hint ─────────────────────────────────────────────
            if (isSetupMode && !inConfirm) {
                Text(
                    text = "Skip (no PIN protection)",
                    fontSize = 13.sp,
                    color = subText,
                    modifier = Modifier.clickable {
                        disablePin(context)
                        onPinVerified()
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NUMPAD KEY BUTTON
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PinKey(
    label: String,
    dark: Boolean,
    accentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isBackspace = label == "⌫"
    val isEmpty     = label.isEmpty()

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "key_scale_$label"
    )

    val cardBg   = if (dark) Color(0xFF0C1322) else Color.White
    val border   = if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textColor = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .then(
                if (isEmpty) Modifier else Modifier
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(
                        width = if (isBackspace) 0.dp else 1.5.dp,
                        color = if (isBackspace) Color.Transparent else border,
                        shape = CircleShape
                    )
                    .clickable(enabled = enabled && !isEmpty) {
                        pressed = true
                        onClick()
                    }
            ),
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(80)
                pressed = false
            }
        }
        if (!isEmpty) {
            if (isBackspace) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
