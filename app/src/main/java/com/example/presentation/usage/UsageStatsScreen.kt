package com.example.presentation.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val tabs = listOf("App Usage", "Deflected Attempts")

    val usedApps = remember(installedApps) {
        installedApps.filter { it.todayUsageMinutes > 0 || it.isBlocked || it.dailyLimitMinutes > 0 }
            .sortedByDescending { it.todayUsageMinutes }
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
            text = "Track screen time, daily limits, and focus enforcement",
            fontSize = 13.sp,
            color = Zinc500
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Total Screen Time Banner
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
                Column {
                    Text(
                        text = "TOTAL SCREEN TIME TODAY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(totalScreenTimeMinutes),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A).copy(alpha = 0.6f))
                        .border(1.dp, MinimalBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
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
                            Text("No application usage tracked today yet.", color = Zinc500, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(usedApps, key = { it.packageName }) { app ->
                        AppUsageItemCard(app = app)
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
fun AppUsageItemCard(app: InstalledApp) {
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
                    Text(
                        text = if (hasLimit) "Daily Limit: ${app.dailyLimitMinutes}m" else if (app.isBlocked) "Locked by Schedule" else "Allowed",
                        fontSize = 11.sp,
                        color = if (isLimitExceeded) Color(0xFFF43F5E) else Zinc400
                    )
                }

                Text(
                    text = "${app.todayUsageMinutes}m",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLimitExceeded) Color(0xFFF43F5E) else CyanAccent
                )
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
