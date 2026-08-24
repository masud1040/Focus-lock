package com.example.domain.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.database.BlockedAppDao
import com.example.data.database.BlockedAppEntity
import com.example.data.database.BlockedAttemptDao
import com.example.data.database.BlockedAttemptLogEntity
import com.example.data.database.EmergencyUnlockDao
import com.example.data.database.EmergencyUnlockSessionEntity
import com.example.data.database.FocusLockDatabase
import com.example.data.database.ScheduleDao
import com.example.data.database.ScheduleEntity
import com.example.data.datastore.UserPreferencesRepository
import com.example.domain.engine.ScheduleEngine
import com.example.domain.engine.UsageStatsHelper
import com.example.domain.model.ActiveScheduleStatus
import com.example.domain.model.BlockReason
import com.example.domain.model.DashboardOverview
import com.example.domain.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

data class BlockingDecision(
    val isBlocked: Boolean,
    val reason: BlockReason,
    val appName: String,
    val packageName: String,
    val scheduleTitle: String = "",
    val remainingMillis: Long = 0L,
    val availableAtFormatted: String = "",
    val dailyLimitMinutes: Int = 0,
    val usedMinutesToday: Int = 0
)

class AppBlockerRepository(
    private val context: Context,
    private val database: FocusLockDatabase = FocusLockDatabase.getInstance(context),
    val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context)
) {
    val blockedAppDao: BlockedAppDao = database.blockedAppDao()
    val scheduleDao: ScheduleDao = database.scheduleDao()
    val blockedAttemptDao: BlockedAttemptDao = database.blockedAttemptDao()
    val emergencyUnlockDao: EmergencyUnlockDao = database.emergencyUnlockDao()

    val allBlockedAppsFlow: Flow<List<BlockedAppEntity>> = blockedAppDao.getAllBlockedApps()
    val allSchedulesFlow: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    val recentAttemptsFlow: Flow<List<BlockedAttemptLogEntity>> = blockedAttemptDao.getRecentAttempts()

    /**
     * Scans installed applications on device and combines them with stored blocked info and usage stats.
     */
    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val usageMap = UsageStatsHelper.getTodayUsagePerApp(context)
        val ourPackage = context.packageName

        // Distinct packages with launcher intent
        val packageNames = resolveInfos.mapNotNull { it.activityInfo?.packageName }
            .filter { it != ourPackage }
            .distinct()

        val list = mutableListOf<InstalledApp>()
        for (pkg in packageNames) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val dbApp = blockedAppDao.getApp(pkg)

                list.add(
                    InstalledApp(
                        packageName = pkg,
                        appName = label,
                        isSystemApp = isSystem,
                        icon = icon,
                        isBlocked = dbApp?.isBlocked ?: false,
                        dailyLimitMinutes = dbApp?.dailyLimitMinutes ?: 0,
                        todayUsageMinutes = usageMap[pkg] ?: 0,
                        category = if (isSystem) "System" else "User Application"
                    )
                )
            } catch (e: Exception) {
                // Ignore missing package info
            }
        }
        list.sortedWith(compareByDescending<InstalledApp> { it.isBlocked }.thenBy { it.appName.lowercase() })
    }

    /**
     * Checks if a package is currently blocked.
     * Evaluates:
     * 1. Global protection master toggle
     * 2. Active emergency unlock session
     * 3. Database blocked state
     * 4. Daily usage limits (UsageStatsManager)
     * 5. Active schedules (same-day or overnight)
     */
    suspend fun shouldBlockApp(packageName: String): BlockingDecision = withContext(Dispatchers.IO) {
        if (packageName == context.packageName) {
            return@withContext BlockingDecision(
                isBlocked = false,
                reason = BlockReason.NONE,
                appName = "FocusLock",
                packageName = packageName
            )
        }

        // Check active emergency unlock session
        val activeUnlock = emergencyUnlockDao.getActiveUnlockSession(packageName)
        if (activeUnlock != null) {
            return@withContext BlockingDecision(
                isBlocked = false,
                reason = BlockReason.NONE,
                appName = getAppName(packageName),
                packageName = packageName
            )
        }

        val appEntity = blockedAppDao.getApp(packageName)
        val isExplicitlyBlocked = appEntity?.isBlocked == true
        val dailyLimit = appEntity?.dailyLimitMinutes ?: 0
        val appName = appEntity?.appName ?: getAppName(packageName)

        // Evaluate daily usage limits
        val todayUsage = UsageStatsHelper.getTodayUsagePerApp(context)[packageName] ?: 0
        if (dailyLimit > 0 && todayUsage >= dailyLimit) {
            val midnightCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val remainingMs = maxOf(0L, midnightCal.timeInMillis - System.currentTimeMillis())
            return@withContext BlockingDecision(
                isBlocked = true,
                reason = BlockReason.DAILY_LIMIT,
                appName = appName,
                packageName = packageName,
                scheduleTitle = "Daily Limit (${dailyLimit}m)",
                remainingMillis = remainingMs,
                availableAtFormatted = "Midnight (00:00)",
                dailyLimitMinutes = dailyLimit,
                usedMinutesToday = todayUsage
            )
        }

        // Evaluate Schedules for this app and global schedules
        val schedules = scheduleDao.getSchedulesForPackage(packageName)
        val scheduleStatus = ScheduleEngine.evaluateSchedules(schedules)
        if (scheduleStatus.isBlocked) {
            return@withContext BlockingDecision(
                isBlocked = true,
                reason = BlockReason.SCHEDULE,
                appName = appName,
                packageName = packageName,
                scheduleTitle = scheduleStatus.scheduleTitle,
                remainingMillis = scheduleStatus.remainingMillis,
                availableAtFormatted = scheduleStatus.availableAtFormatted,
                dailyLimitMinutes = dailyLimit,
                usedMinutesToday = todayUsage
            )
        }

        // Direct lock if user selected app to be blocked and no specific schedule is active
        if (isExplicitlyBlocked) {
            return@withContext BlockingDecision(
                isBlocked = true,
                reason = BlockReason.DIRECT_LOCK,
                appName = appName,
                packageName = packageName,
                scheduleTitle = "Permanent Lock",
                remainingMillis = 0L,
                availableAtFormatted = "Manual Unlock Required",
                dailyLimitMinutes = dailyLimit,
                usedMinutesToday = todayUsage
            )
        }

        return@withContext BlockingDecision(
            isBlocked = false,
            reason = BlockReason.NONE,
            appName = appName,
            packageName = packageName,
            dailyLimitMinutes = dailyLimit,
            usedMinutesToday = todayUsage
        )
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast(".")
        }
    }

    suspend fun setAppBlocked(packageName: String, appName: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        val existing = blockedAppDao.getApp(packageName)
        if (existing != null) {
            blockedAppDao.setBlockedState(packageName, isBlocked)
        } else {
            blockedAppDao.insertApp(
                BlockedAppEntity(
                    packageName = packageName,
                    appName = appName,
                    isBlocked = isBlocked
                )
            )
        }
    }

    suspend fun setAppDailyLimit(packageName: String, appName: String, limitMinutes: Int) = withContext(Dispatchers.IO) {
        val existing = blockedAppDao.getApp(packageName)
        if (existing != null) {
            blockedAppDao.setDailyLimit(packageName, limitMinutes)
        } else {
            blockedAppDao.insertApp(
                BlockedAppEntity(
                    packageName = packageName,
                    appName = appName,
                    isBlocked = true,
                    dailyLimitMinutes = limitMinutes
                )
            )
        }
    }

    suspend fun logBlockedAttempt(packageName: String, appName: String, reason: String) = withContext(Dispatchers.IO) {
        blockedAttemptDao.logAttempt(
            BlockedAttemptLogEntity(
                packageName = packageName,
                appName = appName,
                timestamp = System.currentTimeMillis(),
                reason = reason
            )
        )
    }

    suspend fun startEmergencyUnlock(packageName: String, durationMinutes: Int) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiresAt = now + (durationMinutes * 60 * 1000L)
        emergencyUnlockDao.insertSession(
            EmergencyUnlockSessionEntity(
                packageName = packageName,
                unlockedAt = now,
                expiresAt = expiresAt,
                durationMinutes = durationMinutes,
                isActive = true
            )
        )
    }

    suspend fun saveSchedule(schedule: ScheduleEntity): Long = withContext(Dispatchers.IO) {
        if (schedule.id == 0L) {
            scheduleDao.insertSchedule(schedule)
        } else {
            scheduleDao.updateSchedule(schedule)
            schedule.id
        }
    }

    suspend fun deleteSchedule(scheduleId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.deleteScheduleById(scheduleId)
    }

    suspend fun setScheduleEnabled(scheduleId: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        scheduleDao.setScheduleEnabled(scheduleId, isEnabled)
    }

    /**
     * Provides reactive combined dashboard overview data
     */
    fun getDashboardOverviewFlow(): Flow<DashboardOverview> {
        val midnightCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = midnightCal.timeInMillis

        return combine(
            preferencesRepository.userPreferencesFlow,
            blockedAppDao.getActiveBlockedApps(),
            scheduleDao.getEnabledSchedules(),
            blockedAttemptDao.getAttemptCountSince(startOfToday)
        ) { prefs, blockedApps, schedules, attemptCount ->
            val screenTimeToday = UsageStatsHelper.getTotalScreenTimeMinutesToday(context)
            val focusTime = maxOf(0, 1440 - screenTimeToday) // 24h - screen time

            var nearestCountdown = 0L
            var nearestScheduleTitle: String? = null

            val scheduleStatus = ScheduleEngine.evaluateSchedules(schedules)
            if (scheduleStatus.isBlocked) {
                nearestCountdown = scheduleStatus.remainingMillis
                nearestScheduleTitle = scheduleStatus.scheduleTitle
            }

            val mostBlocked = blockedAttemptDao.getMostBlockedAppSince(startOfToday)

            DashboardOverview(
                isProtectionActive = prefs.isProtectionActive,
                blockedAppsCount = blockedApps.size,
                totalScreenTimeMinutes = screenTimeToday,
                blockedAttemptsToday = attemptCount,
                focusTimeMinutes = focusTime,
                mostBlockedAppName = mostBlocked?.appName,
                mostBlockedAppCount = mostBlocked?.count ?: 0,
                nearestActiveCountdownMillis = nearestCountdown,
                nearestActiveScheduleTitle = nearestScheduleTitle
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        blockedAppDao.clearAll()
        scheduleDao.clearAll()
        blockedAttemptDao.clearAll()
        emergencyUnlockDao.clearAll()
        preferencesRepository.clearAll()
    }
}
