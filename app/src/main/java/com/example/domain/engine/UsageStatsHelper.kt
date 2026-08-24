package com.example.domain.engine

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Returns total foreground usage in minutes for each app today (from 00:00 to now).
     */
    fun getTodayUsagePerApp(context: Context): Map<String, Int> {
        if (!hasUsageStatsPermission(context)) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageMap = mutableMapOf<String, Long>()
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (statsList != null) {
            for (stat in statsList) {
                val existing = usageMap[stat.packageName] ?: 0L
                usageMap[stat.packageName] = existing + stat.totalTimeInForeground
            }
        }

        // Convert milliseconds to minutes
        return usageMap.mapValues { (it.value / (1000 * 60)).toInt() }
    }

    /**
     * Calculates total screen time (in minutes) across all apps today.
     */
    fun getTotalScreenTimeMinutesToday(context: Context): Int {
        val usageMap = getTodayUsagePerApp(context)
        return usageMap.values.sum()
    }

    /**
     * Calculates past 7 days usage in minutes.
     */
    fun getWeeklyUsageBreakdown(context: Context): List<Pair<String, Int>> {
        if (!hasUsageStatsPermission(context)) {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            return days.map { it to 0 }
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val result = mutableListOf<Pair<String, Int>>()

        val now = System.currentTimeMillis()
        for (i in 6 downTo 0) {
            val calStart = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calEnd = Calendar.getInstance().apply {
                timeInMillis = calStart.timeInMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                calStart.timeInMillis,
                minOf(calEnd.timeInMillis, now)
            )

            val totalMinutes = (stats?.sumOf { it.totalTimeInForeground } ?: 0L) / (1000 * 60)
            val dayOfWeek = dayNames[calStart.get(Calendar.DAY_OF_WEEK) - 1]
            result.add(dayOfWeek to totalMinutes.toInt())
        }

        return result
    }
}
