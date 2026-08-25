package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500

@Composable
fun RechartsBarChart(
    weeklyUsage: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxMinutes = remember(weeklyUsage) {
        (weeklyUsage.maxOfOrNull { it.second } ?: 120).coerceAtLeast(60)
    }

    val totalWeeklyMinutes = remember(weeklyUsage) {
        weeklyUsage.sumOf { it.second }
    }

    val dailyAverage = remember(totalWeeklyMinutes, weeklyUsage) {
        if (weeklyUsage.isNotEmpty()) totalWeeklyMinutes / weeklyUsage.size else 0
    }

    var selectedIndex by remember { mutableIntStateOf(weeklyUsage.size - 1) } // Default to today (last bar)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MinimalBorder, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Chart Header with Recharts Title and Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LAST 7 DAYS USAGE HISTORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Zinc400
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily App Screen Time (Recharts)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "7-Day Total",
                        fontSize = 10.sp,
                        color = Zinc500
                    )
                    Text(
                        text = formatTimeHoursMins(totalWeeklyMinutes),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Bar Recharts Tooltip Overlay Card
            if (selectedIndex in weeklyUsage.indices) {
                val selectedPair = weeklyUsage[selectedIndex]
                val diffFromAvg = selectedPair.second - dailyAverage
                val diffPercent = if (dailyAverage > 0) (diffFromAvg.toFloat() / dailyAverage * 100).toInt() else 0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF27272A).copy(alpha = 0.8f))
                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAY: ${selectedPair.first.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Zinc400
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Screen Time: ${formatTimeHoursMins(selectedPair.second)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (diffFromAvg > 0) Color(0x33F43F5E) else Color(0x3310B981)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (diffFromAvg > 0) "+$diffPercent% vs avg" else "$diffPercent% vs avg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diffFromAvg > 0) Color(0xFFF43F5E) else Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recharts Bar Chart Area (Bars + Axis Grid)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Background Gridlines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridLines = 4
                    val stepY = size.height / gridLines
                    for (i in 0..gridLines) {
                        val y = i * stepY
                        drawLine(
                            color = Color(0x1AFFFFFF),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            strokeWidth = 1f
                        )
                    }
                }

                // Interactive Bar Columns
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyUsage.forEachIndexed { index, pair ->
                        val isSelected = index == selectedIndex
                        val barHeightFraction = (pair.second.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedIndex = index }
                                .padding(horizontal = 4.dp)
                        ) {
                            // Time label floating on top of selected bar
                            if (isSelected) {
                                Text(
                                    text = formatTimeMinsOnly(pair.second),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            // The Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(barHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = if (isSelected) {
                                                listOf(CyanAccent, VioletAccent)
                                            } else {
                                                listOf(
                                                    CyanAccent.copy(alpha = 0.35f),
                                                    Color(0xFF27272A)
                                                )
                                            }
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) CyanAccent else Color.Transparent,
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Day Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyUsage.forEachIndexed { index, pair ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pair.first,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CyanAccent else Zinc500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Legend Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Selected Day", fontSize = 11.sp, color = Zinc400)

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CyanAccent.copy(alpha = 0.35f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Past Days", fontSize = 11.sp, color = Zinc500)
                }

                Text(
                    text = "Daily Avg: ${formatTimeHoursMins(dailyAverage)}",
                    fontSize = 11.sp,
                    color = VioletAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatTimeHoursMins(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun formatTimeMinsOnly(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h${m}m" else "${m}m"
}
