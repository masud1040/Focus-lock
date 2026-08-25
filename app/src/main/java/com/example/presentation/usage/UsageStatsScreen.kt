package com.example.presentation.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.BlockedAttemptLogEntity
import com.example.domain.model.InstalledApp
import com.example.presentation.apps.AppIconRenderer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UsageStatsScreen(
    installedApps: List<InstalledApp>,
    weeklyUsage: List<Pair<String, Int>>,
    recentAttempts: List<BlockedAttemptLogEntity>,
    totalScreenTimeMinutes: Int
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPeriod by remember { mutableIntStateOf(0) } // 0 = Daily, 1 = Monthly
    val tabs = listOf("App Usage", "Deflected Attempts")

    val totalMonthlyScreenTime = remember(installedApps) {
        installedApps.sumOf { it.monthlyUsageMinutes }
    }

    val usedApps = remember(installedApps, selectedPeriod) {
        installedApps.filter {
            if (selectedPeriod == 0) {
                it.todayUsageMinutes > 0 || it.isBlocked || it.dailyLimitMinutes > 0
            } else {
                it.monthlyUsageMinutes > 0 || it.isBlocked || it.dailyLimitMinutes > 0
            }
        }.sortedByDescending {
            if (selectedPeriod == 0) it.todayUsageMinutes else it.monthlyUsageMinutes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Usage & Analytics",
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Track daily and monthly app screen time & limits",
            fontSize = 13.sp,
            color = Zinc500
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Total Screen Time Banner (Daily & Monthly)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MinimalBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TODAY'S SCREEN TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(totalScreenTimeMinutes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MinimalBorderSubtle)
                )

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = "THIS MONTH'S TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(totalMonthlyScreenTime),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF18181B).copy(alpha = 0.5f),
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanAccent
                )
            },
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(14.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) CyanAccent else Zinc400
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Daily / Monthly period sub-filter toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedPeriod == 0) CyanAccent.copy(alpha = 0.15f) else Color(0xFF18181B))
                        .border(1.dp, if (selectedPeriod == 0) CyanAccent else MinimalBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clickable { selectedPeriod = 0 }
                ) {
                    Text(
                        text = "Today (দৈনিক)",
                        fontSize = 12.sp,
                        fontWeight = if (selectedPeriod == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedPeriod == 0) CyanAccent else Zinc400
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedPeriod == 1) VioletAccent.copy(alpha = 0.15f) else Color(0xFF18181B))
                        .border(1.dp, if (selectedPeriod == 1) VioletAccent else MinimalBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clickable { selectedPeriod = 1 }
                ) {
                    Text(
                        text = "This Month (মাসিক)",
                        fontSize = 12.sp,
                        fontWeight = if (selectedPeriod == 1) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedPeriod == 1) VioletAccent else Zinc400
                    )
                }
            }

            // App-by-app usage list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (usedApps.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedPeriod == 0) "No application usage tracked today yet." else "No monthly application usage tracked yet.",
                                color = Zinc500,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(usedApps, key = { it.packageName }) { app ->
                        AppUsageItemCard(app = app, selectedPeriod = selectedPeriod)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // Deflected attempts log
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (recentAttempts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No blocked attempts recorded today.", color = Zinc500, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(recentAttempts, key = { it.id }) { log ->
                        BlockedAttemptLogCard(log = log)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun AppUsageItemCard(app: InstalledApp, selectedPeriod: Int = 0) {
    val hasLimit = app.dailyLimitMinutes > 0
    val progress = if (hasLimit) {
        (app.todayUsageMinutes.toFloat() / app.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val isLimitExceeded = hasLimit && app.todayUsageMinutes >= app.dailyLimitMinutes

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isLimitExceeded) Color(0x66F43F5E) else MinimalBorderSubtle, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconRenderer(drawable = app.icon, fallbackLetter = app.appName.firstOrNull()?.toString() ?: "A")
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasLimit) "Daily Limit: ${app.dailyLimitMinutes}m" else if (app.isBlocked) "Locked by Schedule" else "Allowed",
                        fontSize = 11.sp,
                        color = if (isLimitExceeded) Color(0xFFF43F5E) else Zinc400
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (selectedPeriod == 0) {
                        Text(
                            text = formatTime(app.todayUsageMinutes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLimitExceeded) Color(0xFFF43F5E) else CyanAccent
                        )
                        Text(
                            text = "Month: ${formatTime(app.monthlyUsageMinutes)}",
                            fontSize = 11.sp,
                            color = Zinc400
                        )
                    } else {
                        Text(
                            text = formatTime(app.monthlyUsageMinutes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VioletAccent
                        )
                        Text(
                            text = "Today: ${formatTime(app.todayUsageMinutes)}",
                            fontSize = 11.sp,
                            color = Zinc400
                        )
                    }
                }
            }

            if (hasLimit) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isLimitExceeded) Color(0xFFF43F5E) else if (progress > 0.8f) Color(0xFFF59E0B) else CyanAccent,
                    trackColor = Color(0xFF27272A)
                )
            }
        }
    }
}

@Composable
fun BlockedAttemptLogCard(log: BlockedAttemptLogEntity) {
    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("h:mm a • MMM d", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF43F5E).copy(alpha = 0.12f))
                    .border(1.dp, Color(0x22F43F5E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "Deflected: ${log.reason.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 11.sp,
                    color = Zinc400
                )
            }

            Text(
                text = timeFormatted,
                fontSize = 11.sp,
                color = Zinc500
            )
        }
    }
}

private fun formatTime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
