package com.example.presentation.apps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.InstalledApp
import com.example.ui.theme.CyanAccent
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

enum class AppFilter { ALL, BLOCKED, ALLOWED, FREQUENT }
enum class AppSort { NAME, USAGE, STATUS }

@Composable
fun AppsScreen(
    installedApps: List<InstalledApp>,
    isLoading: Boolean,
    onToggleBlocked: (packageName: String, appName: String, isBlocked: Boolean) -> Unit,
    onSetDailyLimit: (packageName: String, appName: String, limitMinutes: Int) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentFilter by remember { mutableStateOf(AppFilter.ALL) }
    var currentSort by remember { mutableStateOf(AppSort.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedAppForLimit by remember { mutableStateOf<InstalledApp?>(null) }

    val filteredApps = remember(installedApps, searchQuery, currentFilter, currentSort) {
        var list = installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }

        list = when (currentFilter) {
            AppFilter.ALL -> list
            AppFilter.BLOCKED -> list.filter { it.isBlocked }
            AppFilter.ALLOWED -> list.filter { !it.isBlocked }
            AppFilter.FREQUENT -> list.filter { it.todayUsageMinutes > 0 }
        }

        when (currentSort) {
            AppSort.NAME -> list.sortedBy { it.appName.lowercase() }
            AppSort.USAGE -> list.sortedByDescending { it.todayUsageMinutes }
            AppSort.STATUS -> list.sortedWith(compareByDescending<InstalledApp> { it.isBlocked }.thenBy { it.appName.lowercase() })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Applications",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${installedApps.count { it.isBlocked }} blocked of ${installedApps.size} apps",
                    fontSize = 13.sp,
                    color = Zinc500
                )
            }

            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF18181B))
                        .border(1.dp, MinimalBorder, CircleShape)
                        .testTag("apps_sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort Apps",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sort by Name") },
                        onClick = { currentSort = AppSort.NAME; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort by Usage Today") },
                        onClick = { currentSort = AppSort.USAGE; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort by Blocked Status") },
                        onClick = { currentSort = AppSort.STATUS; showSortMenu = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed applications...", color = Zinc500, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Zinc400,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Zinc400, modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apps_search_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF18181B).copy(alpha = 0.6f),
                unfocusedContainerColor = Color(0xFF18181B).copy(alpha = 0.6f),
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = MinimalBorderSubtle
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == AppFilter.ALL,
                onClick = { currentFilter = AppFilter.ALL },
                label = { Text("All", fontSize = 12.sp) },
                colors = filterChipColors()
            )
            FilterChip(
                selected = currentFilter == AppFilter.BLOCKED,
                onClick = { currentFilter = AppFilter.BLOCKED },
                label = { Text("Blocked", fontSize = 12.sp) },
                colors = filterChipColors()
            )
            FilterChip(
                selected = currentFilter == AppFilter.ALLOWED,
                onClick = { currentFilter = AppFilter.ALLOWED },
                label = { Text("Allowed", fontSize = 12.sp) },
                colors = filterChipColors()
            )
            FilterChip(
                selected = currentFilter == AppFilter.FREQUENT,
                onClick = { currentFilter = AppFilter.FREQUENT },
                label = { Text("Used Today", fontSize = 12.sp) },
                colors = filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyanAccent)
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = Zinc500,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No applications match your filter",
                        fontSize = 14.sp,
                        color = Zinc400
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        onToggle = { isBlocked ->
                            onToggleBlocked(app.packageName, app.appName, isBlocked)
                        },
                        onConfigureLimit = {
                            selectedAppForLimit = app
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Daily Limit Configuration Dialog
    if (selectedAppForLimit != null) {
        val app = selectedAppForLimit!!
        var limitMinutes by remember { mutableFloatStateOf(if (app.dailyLimitMinutes > 0) app.dailyLimitMinutes.toFloat() else 30f) }
        var isLimitEnabled by remember { mutableStateOf(app.dailyLimitMinutes > 0) }

        AlertDialog(
            onDismissRequest = { selectedAppForLimit = null },
            title = {
                Text(
                    text = "Daily Limit: ${app.appName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set maximum allowed usage per day. Once reached, FocusLock will lock ${app.appName} until midnight.",
                        fontSize = 13.sp,
                        color = Zinc400
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLimitEnabled) "Limit: ${limitMinutes.toInt()} min/day" else "Daily Limit Disabled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLimitEnabled) CyanAccent else Zinc400
                        )
                        Switch(
                            checked = isLimitEnabled,
                            onCheckedChange = { isLimitEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF050505),
                                checkedTrackColor = CyanAccent,
                                uncheckedThumbColor = Zinc400,
                                uncheckedTrackColor = Color(0xFF27272A)
                            )
                        )
                    }

                    if (isLimitEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = limitMinutes,
                            onValueChange = { limitMinutes = it },
                            valueRange = 5f..240f,
                            steps = 46, // 5 min increments
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent,
                                inactiveTrackColor = Color(0xFF27272A)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("5m", fontSize = 11.sp, color = Zinc500)
                            Text("1h", fontSize = 11.sp, color = Zinc500)
                            Text("2h", fontSize = 11.sp, color = Zinc500)
                            Text("4h", fontSize = 11.sp, color = Zinc500)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalLimit = if (isLimitEnabled) limitMinutes.toInt() else 0
                        onSetDailyLimit(app.packageName, app.appName, finalLimit)
                        selectedAppForLimit = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF050505))
                ) {
                    Text("Save Limit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppForLimit = null }) {
                    Text("Cancel", color = Zinc400)
                }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun AppItemCard(
    app: InstalledApp,
    onToggle: (Boolean) -> Unit,
    onConfigureLimit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (app.isBlocked) Color(0x3322D3EE) else MinimalBorderSubtle,
                RoundedCornerShape(18.dp)
            )
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            AppIconRenderer(drawable = app.icon, fallbackLetter = app.appName.firstOrNull()?.toString() ?: "A")

            Spacer(modifier = Modifier.width(14.dp))

            // App details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (app.todayUsageMinutes > 0) "${app.todayUsageMinutes}m today" else "0m today",
                        fontSize = 11.sp,
                        color = Zinc400
                    )

                    if (app.dailyLimitMinutes > 0) {
                        Text(
                            text = " • Limit: ${app.dailyLimitMinutes}m",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Limit configuration icon
            IconButton(
                onClick = onConfigureLimit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Configure Daily Limit",
                    tint = if (app.dailyLimitMinutes > 0) CyanAccent else Zinc500,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Toggle Switch
            Switch(
                checked = app.isBlocked,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("toggle_app_${app.packageName}"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF050505),
                    checkedTrackColor = CyanAccent,
                    uncheckedThumbColor = Zinc400,
                    uncheckedTrackColor = Color(0xFF27272A)
                )
            )
        }
    }
}

@Composable
fun AppIconRenderer(drawable: Drawable?, fallbackLetter: String) {
    if (drawable != null) {
        val bitmap = remember(drawable) {
            val bmp = Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF27272A).copy(alpha = 0.5f))
                .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackLetter,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent
            )
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color(0xFF18181B),
    labelColor = Zinc400,
    selectedContainerColor = CyanAccent,
    selectedLabelColor = Color(0xFF050505)
)
