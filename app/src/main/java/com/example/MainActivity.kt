package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.ThemeMode
import com.example.presentation.FocusLockViewModel
import com.example.presentation.apps.AppsScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.presentation.components.FirstTimeGuideOverlay
import com.example.presentation.components.PinPromptDialog
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.schedules.SchedulesScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.usage.UsageStatsScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FocusLockTheme
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface

enum class AppDestination(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Home", Icons.Default.Dashboard, "nav_dashboard"),
    APPS("Apps", Icons.Default.AppBlocking, "nav_apps"),
    SCHEDULES("Schedules", Icons.Default.Schedule, "nav_schedules"),
    USAGE("Usage", Icons.Default.BarChart, "nav_usage"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FocusLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPreferences by viewModel.userPreferences.collectAsState()
            val isDark = when (userPreferences.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FocusLockTheme(darkTheme = isDark) {
                FocusLockApp(
                    viewModel = viewModel,
                    isOnboarding = !userPreferences.isOnboardingCompleted
                )
            }
        }
    }
}

@Composable
fun FocusLockApp(
    viewModel: FocusLockViewModel,
    isOnboarding: Boolean
) {
    if (isOnboarding) {
        OnboardingScreen(
            onComplete = { viewModel.completeOnboarding() }
        )
        return
    }

    var showInitialLoading by remember { mutableStateOf(true) }
    var showQuickGuide by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        showInitialLoading = false
        showQuickGuide = true
    }

    if (showInitialLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09090B)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.focus_lock_icon_1787572031797),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "FocusLock লোড হচ্ছে...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ইনস্টলড অ্যাপস ও ব্যবহারের পরিসংখ্যান প্রস্তুত করা হচ্ছে",
                    fontSize = 12.sp,
                    color = Color(0xFF71717A)
                )
            }
        }
        return
    }

    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    val userPreferences by viewModel.userPreferences.collectAsState()
    val dashboardOverview by viewModel.dashboardOverview.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val recentAttempts by viewModel.recentAttempts.collectAsState()
    val weeklyUsage by viewModel.weeklyUsage.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()

    var showPinDialogForDestination by remember { mutableStateOf<AppDestination?>(null) }
    var pendingActionAfterPin by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun navigateWithPinCheck(destination: AppDestination) {
        if (destination == AppDestination.SETTINGS && userPreferences.isLockSettingsWithPin && userPreferences.isPinSet) {
            showPinDialogForDestination = destination
        } else {
            currentDestination = destination
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF09090B).copy(alpha = 0.92f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(
                        1.dp,
                        Color(0x14FFFFFF),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .testTag("bottom_navigation_bar")
            ) {
                AppDestination.entries.forEach { dest ->
                    val selected = currentDestination == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateWithPinCheck(dest) },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = dest.label.uppercase(),
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            unselectedIconColor = Color(0xFF71717A),
                            unselectedTextColor = Color(0xFF71717A),
                            indicatorColor = Color(0xFF22D3EE).copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag(dest.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_navigation"
            ) { target ->
                when (target) {
                    AppDestination.DASHBOARD -> DashboardScreen(
                        overview = dashboardOverview,
                        weeklyUsage = weeklyUsage,
                        onToggleProtection = { active -> viewModel.toggleProtection(active) },
                        onNavigateToApps = { currentDestination = AppDestination.APPS },
                        onNavigateToSchedules = { currentDestination = AppDestination.SCHEDULES },
                        onNavigateToUsage = { currentDestination = AppDestination.USAGE },
                        onNavigateToSettings = { navigateWithPinCheck(AppDestination.SETTINGS) }
                    )
                    AppDestination.APPS -> AppsScreen(
                        installedApps = installedApps,
                        isLoading = isLoadingApps,
                        onToggleBlocked = { pkg, name, blocked ->
                            if (userPreferences.isLockSettingsWithPin && userPreferences.isPinSet) {
                                pendingActionAfterPin = {
                                    viewModel.toggleAppBlocked(pkg, name, blocked)
                                }
                            } else {
                                viewModel.toggleAppBlocked(pkg, name, blocked)
                            }
                        },
                        onSetDailyLimit = { pkg, name, limit ->
                            viewModel.setAppDailyLimit(pkg, name, limit)
                        },
                        onRefresh = { viewModel.loadInstalledApps() }
                    )
                    AppDestination.SCHEDULES -> SchedulesScreen(
                        schedules = schedules,
                        installedApps = installedApps,
                        onSaveSchedule = { schedule -> viewModel.saveSchedule(schedule) },
                        onDeleteSchedule = { id -> viewModel.deleteSchedule(id) },
                        onToggleSchedule = { id, enabled -> viewModel.toggleSchedule(id, enabled) }
                    )
                    AppDestination.USAGE -> UsageStatsScreen(
                        installedApps = installedApps,
                        weeklyUsage = weeklyUsage,
                        recentAttempts = recentAttempts,
                        totalScreenTimeMinutes = dashboardOverview.totalScreenTimeMinutes
                    )
                    AppDestination.SETTINGS -> SettingsScreen(
                        userPreferences = userPreferences,
                        repository = viewModel.repository,
                        onToggleProtection = { viewModel.toggleProtection(it) },
                        onSetLockSettingsWithPin = { viewModel.setLockSettingsWithPin(it) },
                        onSetEmergencyUnlockEnabled = { viewModel.setEmergencyUnlockEnabled(it) },
                        onSetEmergencyUnlockDuration = { viewModel.setEmergencyUnlockDuration(it) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetNotificationsEnabled = { viewModel.setNotificationsEnabled(it) },
                        onSetPin = { viewModel.setPin(it) },
                        onRemovePin = { viewModel.removePin() },
                        onResetAllData = { viewModel.resetAllData() }
                    )
                }
            }
        }
    }

    if (showPinDialogForDestination != null) {
        val dest = showPinDialogForDestination!!
        PinPromptDialog(
            title = "Security Verification",
            subtitle = "Enter PIN to access Settings and security controls.",
            repository = viewModel.repository,
            onDismiss = { showPinDialogForDestination = null },
            onSuccess = {
                showPinDialogForDestination = null
                currentDestination = dest
            }
        )
    }

    if (pendingActionAfterPin != null) {
        val action = pendingActionAfterPin!!
        PinPromptDialog(
            title = "Security Verification",
            subtitle = "Enter PIN to alter app blocking rules.",
            repository = viewModel.repository,
            onDismiss = { pendingActionAfterPin = null },
            onSuccess = {
                pendingActionAfterPin = null
                action()
            }
        )
    }

    if (showQuickGuide) {
        FirstTimeGuideOverlay(
            onDismissOrSkip = { showQuickGuide = false }
        )
    }
}
