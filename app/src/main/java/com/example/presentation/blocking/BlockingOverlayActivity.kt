package com.example.presentation.blocking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.ScheduleEngine
import com.example.domain.repository.AppBlockerRepository
import com.example.presentation.components.PinPromptDialog
import com.example.security.PinSecurityManager
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FocusLockTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BlockingOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_SCHEDULE_TITLE = "extra_schedule_title"
        const val EXTRA_REMAINING_MS = "extra_remaining_ms"
        const val EXTRA_AVAILABLE_AT = "extra_available_at"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "This Application"
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "SCHEDULE"
        val scheduleTitle = intent.getStringExtra(EXTRA_SCHEDULE_TITLE) ?: "Active Schedule"
        val initialRemainingMs = intent.getLongExtra(EXTRA_REMAINING_MS, 0L)
        val availableAt = intent.getStringExtra(EXTRA_AVAILABLE_AT) ?: "Scheduled Time"

        val repository = AppBlockerRepository(applicationContext)

        setContent {
            FocusLockTheme(darkTheme = true) {
                BlockingScreenContent(
                    packageName = packageName,
                    appName = appName,
                    reason = reason,
                    scheduleTitle = scheduleTitle,
                    initialRemainingMs = initialRemainingMs,
                    availableAt = availableAt,
                    repository = repository,
                    onGoBack = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onUnlocked = {
                        Toast.makeText(this, "$appName unlocked for 5 minutes", Toast.LENGTH_LONG).show()
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun BlockingScreenContent(
    packageName: String,
    appName: String,
    reason: String,
    scheduleTitle: String,
    initialRemainingMs: Long,
    availableAt: String,
    repository: AppBlockerRepository,
    onGoBack: () -> Unit,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var remainingMillis by remember { mutableLongStateOf(initialRemainingMs) }
    var showPinDialog by remember { mutableStateOf(false) }
    var isEmergencyAllowed by remember { mutableStateOf(true) }
    var emergencyDurationMinutes by remember { mutableStateOf(5) }
    var isPinSet by remember { mutableStateOf(false) }

    // Live countdown ticker
    LaunchedEffect(Unit) {
        val prefs = repository.preferencesRepository.userPreferencesFlow.first()
        isEmergencyAllowed = prefs.isEmergencyUnlockEnabled
        emergencyDurationMinutes = prefs.emergencyUnlockDurationMinutes
        isPinSet = prefs.isPinSet

        while (true) {
            delay(1000)
            if (remainingMillis > 0) {
                remainingMillis = maxOf(0L, remainingMillis - 1000)
            }
        }
    }

    // Glowing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        color = ObsidianBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Protection Active",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FOCUSLOCK ENFORCEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = CyanAccent
                    )
                }

                // Center visual lock with pulse ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            CyanAccent.copy(alpha = 0.20f),
                                            CyanAccent.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Inner circular frame
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF18181B))
                                .border(1.dp, CyanAccent.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = CyanAccent,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Take a break.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$appName is currently locked.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFA1A1AA),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Time and Reason Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (scheduleTitle.isNotBlank()) {
                                Text(
                                    text = scheduleTitle.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.2.sp,
                                    color = CyanAccent
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (remainingMillis > 0) {
                                Text(
                                    text = "REMAINING TIME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = Color(0xFF71717A)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ScheduleEngine.formatRemainingCountdown(remainingMillis),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-1).sp,
                                    color = Color.White
                                )
                            }

                            if (availableAt.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = "Available at",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Available at $availableAt",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CyanAccent
                                    )
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onGoBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("blocking_go_back_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF050505)
                        )
                    ) {
                        Text(
                            text = "Go Back to Home",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isEmergencyAllowed) {
                        OutlinedButton(
                            onClick = {
                                if (isPinSet) {
                                    showPinDialog = true
                                } else {
                                    // Unlock directly if PIN is not configured
                                    scope.launch {
                                        repository.startEmergencyUnlock(packageName, emergencyDurationMinutes)
                                        onUnlocked()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("blocking_emergency_unlock_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x14FFFFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA1A1AA))
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Emergency Unlock",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency Unlock ($emergencyDurationMinutes min)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        PinPromptDialog(
            title = "Emergency Unlock",
            subtitle = "Enter your security PIN to unlock $appName for $emergencyDurationMinutes minutes.",
            repository = repository,
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                scope.launch {
                    repository.startEmergencyUnlock(packageName, emergencyDurationMinutes)
                    onUnlocked()
                }
            }
        )
    }
}
