package com.example.domain.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null,
    val isBlocked: Boolean = false,
    val dailyLimitMinutes: Int = 0,
    val todayUsageMinutes: Int = 0,
    val monthlyUsageMinutes: Int = 0,
    val category: String = "App"
)

data class ActiveScheduleStatus(
    val isBlocked: Boolean,
    val scheduleTitle: String,
    val remainingMillis: Long,
    val availableAtFormatted: String,
    val reason: BlockReason
) {
    companion object {
        val UNLOCKED = ActiveScheduleStatus(
            isBlocked = false,
            scheduleTitle = "",
            remainingMillis = 0L,
            availableAtFormatted = "",
            reason = BlockReason.NONE
        )
    }
}

enum class BlockReason {
    NONE,
    SCHEDULE,
    DAILY_LIMIT,
    DIRECT_LOCK
}

data class DashboardOverview(
    val isProtectionActive: Boolean,
    val blockedAppsCount: Int,
    val totalScreenTimeMinutes: Int,
    val blockedAttemptsToday: Int,
    val focusTimeMinutes: Int,
    val mostBlockedAppName: String?,
    val mostBlockedAppCount: Int,
    val nearestActiveCountdownMillis: Long,
    val nearestActiveScheduleTitle: String?
)

data class DailyUsageItem(
    val dayLabel: String,
    val usageMinutes: Int
)

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
    val dailyLimitMinutes: Int,
    val blockedAttempts: Int
)
