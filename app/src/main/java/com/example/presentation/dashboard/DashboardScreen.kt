package com.example.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.ScheduleEngine
import com.example.domain.model.DashboardOverview
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.MinimalSurfaceVariant
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    overview: DashboardOverview,
    weeklyUsage: List<Pair<String, Int>>,
    onToggleProtection: (Boolean) -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToUsage: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var liveCountdown by remember(overview.nearestActiveCountdownMillis) {
        mutableLongStateOf(overview.nearestActiveCountdownMillis)
    }

    LaunchedEffect(overview.nearestActiveCountdownMillis) {
        liveCountdown = overview.nearestActiveCountdownMillis
        while (liveCountdown > 0) {
            delay(1000)
            liveCountdown = maxOf(0L, liveCountdown - 1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Clean Minimalism Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "FocusLock",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Stay focused. Stay in control.",
                        fontSize = 14.sp,
                        color = Zinc500
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF18181B))
                        .border(1.dp, MinimalBorder, CircleShape)
                        .clickable { onNavigateToSettings() }
                        .testTag("dashboard_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Zinc400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Clean Minimalism Hero Status Card with Glow
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_status_card")
            ) {
                // Subtle ambient glow layer
                if (overview.isProtectionActive) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        CyanAccent.copy(alpha = 0.15f),
                                        BlueAccent.copy(alpha = 0.15f)
                                    )
                                )
                            )
                    )
                }

                // Main Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (overview.isProtectionActive) Color(0x3322D3EE) else MinimalBorder,
                            RoundedCornerShape(32.dp)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF18181B).copy(alpha = 0.85f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Top row inside Hero Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (overview.isProtectionActive) CyanAccent.copy(alpha = pulseAlpha)
                                                else Zinc500
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (overview.isProtectionActive) "PROTECTION ACTIVE" else "PROTECTION PAUSED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        color = if (overview.isProtectionActive) CyanAccent else Zinc500
                                    )
                                }

                                Text(
                                    text = "${overview.blockedAppsCount} Apps Locked",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x1AFFFFFF))
                                        .border(1.dp, Color(0x14FFFFFF), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = overview.nearestActiveScheduleTitle ?: if (overview.isProtectionActive) "Work Mode" else "Paused",
                                        fontSize = 11.sp,
                                        color = Zinc400,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Switch(
                                    checked = overview.isProtectionActive,
                                    onCheckedChange = onToggleProtection,
                                    modifier = Modifier.testTag("protection_master_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF050505),
                                        checkedTrackColor = CyanAccent,
                                        uncheckedThumbColor = Zinc400,
                                        uncheckedTrackColor = Color(0xFF27272A)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Large Clean Tabular Countdown
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AVAILABLE IN",
                                fontSize = 10.sp,
                                color = Zinc500,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (liveCountdown > 0) ScheduleEngine.formatRemainingCountdown(liveCountdown)
                                else if (overview.blockedAppsCount > 0) "Rules Enforced" else "00:00:00",
                                fontSize = if (liveCountdown > 0) 46.sp else 32.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-1.5).sp,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Slim Minimalist Progress Track (h-1.5)
                        val progressFraction = if (liveCountdown > 0) {
                            val totalSpan = maxOf(liveCountdown, 3600000L * 4)
                            (1f - (liveCountdown.toFloat() / totalSpan.toFloat())).coerceIn(0.15f, 0.95f)
                        } else {
                            if (overview.isProtectionActive) 1f else 0.05f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color(0x14FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progressFraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(CyanAccent, BlueAccent)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Clean Minimalism 2-Column Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric 1: Screen Time
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SCREEN TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Zinc500
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = formatMinutes(overview.totalScreenTimeMinutes),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Metric 2: Blocked
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "BLOCKED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Zinc500
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${overview.blockedAttemptsToday}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Attempts",
                                fontSize = 11.sp,
                                color = Zinc500
                            )
                        }
                    }
                }
            }
        }

        // Clean Minimalism 2x2 Action Buttons (h-24 style)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalActionButton(
                        icon = Icons.Default.AppBlocking,
                        label = "Block App",
                        accentColor = CyanAccent,
                        modifier = Modifier.weight(1f),
                        testTag = "action_block_app",
                        onClick = onNavigateToApps
                    )
                    MinimalActionButton(
                        icon = Icons.Default.AddAlarm,
                        label = "Schedules",
                        accentColor = BlueAccent,
                        modifier = Modifier.weight(1f),
                        testTag = "action_create_schedule",
                        onClick = onNavigateToSchedules
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalActionButton(
                        icon = Icons.Default.BarChart,
                        label = "Usage",
                        accentColor = VioletAccent,
                        modifier = Modifier.weight(1f),
                        testTag = "action_usage_stats",
                        onClick = onNavigateToUsage
                    )
                    MinimalActionButton(
                        icon = Icons.Default.Security,
                        label = "Settings",
                        accentColor = Zinc400,
                        modifier = Modifier.weight(1f),
                        testTag = "action_security_settings",
                        onClick = onNavigateToSettings
                    )
                }
            }
        }

        // Focus Trends Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Focus Trends",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Past 7 Days",
                            fontSize = 11.sp,
                            color = Zinc500
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val maxUsage = maxOf(1, weeklyUsage.maxOfOrNull { it.second } ?: 1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        for (item in weeklyUsage) {
                            val fraction = (item.second.toFloat() / maxUsage.toFloat()).coerceIn(0.08f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height((fraction * 56).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(CyanAccent, BlueAccent)
                                            )
                                        )
                                    )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.first.take(3),
                                    fontSize = 10.sp,
                                    color = Zinc500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MinimalActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF27272A).copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Zinc100
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
