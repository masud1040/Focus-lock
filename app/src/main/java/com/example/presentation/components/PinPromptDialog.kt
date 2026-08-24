package com.example.presentation.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.repository.AppBlockerRepository
import com.example.security.PinSecurityManager
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinPromptDialog(
    title: String = "Enter Security PIN",
    subtitle: String = "Enter your 4-digit PIN to continue.",
    repository: AppBlockerRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    fun vibrateDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    LaunchedEffect(Unit) {
        val prefs = repository.preferencesRepository.userPreferencesFlow.first()
        if (PinSecurityManager.isLockedOut(prefs.pinLockoutUntil)) {
            lockoutSeconds = PinSecurityManager.getRemainingLockoutSeconds(prefs.pinLockoutUntil)
        }
    }

    // Countdown lockout timer
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000)
            lockoutSeconds -= 1
        }
    }

    fun verifyPinAttempt(pin: String) {
        if (lockoutSeconds > 0 || isChecking) return
        isChecking = true

        scope.launch {
            val prefs = repository.preferencesRepository.userPreferencesFlow.first()
            if (!prefs.isPinSet) {
                // No PIN configured, allow access
                onSuccess()
                return@launch
            }

            if (PinSecurityManager.verifyPin(pin, prefs.pinHash, prefs.pinSalt)) {
                repository.preferencesRepository.resetPinAttempts()
                onSuccess()
            } else {
                repository.preferencesRepository.recordFailedPinAttempt(prefs.failedPinAttempts)
                val updatedPrefs = repository.preferencesRepository.userPreferencesFlow.first()
                if (PinSecurityManager.isLockedOut(updatedPrefs.pinLockoutUntil)) {
                    lockoutSeconds = PinSecurityManager.getRemainingLockoutSeconds(updatedPrefs.pinLockoutUntil)
                    errorMessage = "Too many failed attempts. Locked out for ${lockoutSeconds}s."
                } else {
                    val remainingTries = 5 - updatedPrefs.failedPinAttempts
                    errorMessage = "Incorrect PIN. $remainingTries attempts remaining."
                }

                vibrateDevice()
                enteredPin = ""
                // Shake animation
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 400
                        0f at 0
                        -20f at 50
                        20f at 100
                        -15f at 150
                        15f at 200
                        -10f at 250
                        10f at 300
                        0f at 400
                    }
                )
            }
            isChecking = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A).copy(alpha = 0.5f))
                        .border(1.dp, Color(0x14FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN Lock",
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFFA1A1AA),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) CyanAccent else Color(0xFF27272A))
                                .border(1.dp, if (isFilled) CyanAccent else Color(0x14FFFFFF), CircleShape)
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFF43F5E),
                        textAlign = TextAlign.Center
                    )
                }

                if (lockoutSeconds > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try again in ${lockoutSeconds}s",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in keys) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (key in row) {
                                KeypadButton(
                                    label = key,
                                    enabled = lockoutSeconds == 0 && !isChecking,
                                    onClick = {
                                        when (key) {
                                            "C" -> {
                                                enteredPin = ""
                                                errorMessage = null
                                            }
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    errorMessage = null
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    val newPin = enteredPin + key
                                                    enteredPin = newPin
                                                    errorMessage = null
                                                    if (newPin.length == 4) {
                                                        verifyPinAttempt(newPin)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("pin_cancel_button")
                ) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(if (label == "DEL" || label == "C") Color.Transparent else Color(0xFF27272A).copy(alpha = 0.5f))
            .border(
                1.dp,
                if (label == "DEL" || label == "C") Color.Transparent else Color(0x14FFFFFF),
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("pin_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = if (enabled) Color(0xFFA1A1AA) else Color(0xFF52525B),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = if (label == "C") 16.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else Color(0xFF52525B)
            )
        }
    }
}
