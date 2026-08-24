package com.example.domain.engine

import com.example.data.database.ScheduleEntity
import com.example.domain.model.ActiveScheduleStatus
import com.example.domain.model.BlockReason
import java.util.Calendar
import java.util.Locale

object ScheduleEngine {

    /**
     * Converts Java Calendar DAY_OF_WEEK (Sunday=1, Monday=2..Saturday=7)
     * to standard 1=Mon..7=Sun
     */
    fun calendarDayToStandard(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun parseDays(daysCsv: String): Set<Int> {
        return daysCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    /**
     * Checks if a given schedule is currently active at the specified timestamp.
     * Returns the schedule status with remaining milliseconds and target end time.
     */
    fun evaluateSchedule(schedule: ScheduleEntity, nowMillis: Long = System.currentTimeMillis()): ActiveScheduleStatus {
        if (!schedule.isEnabled) {
            return ActiveScheduleStatus.UNLOCKED
        }

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentSecond = cal.get(Calendar.SECOND)
        val currentDay = calendarDayToStandard(cal.get(Calendar.DAY_OF_WEEK))

        val days = parseDays(schedule.daysOfWeek)
        val isOvernight = schedule.startHour > schedule.endHour ||
                (schedule.startHour == schedule.endHour && schedule.startMinute > schedule.endMinute)

        val currentTotalMinutes = currentHour * 60 + currentMinute
        val startTotalMinutes = schedule.startHour * 60 + schedule.startMinute
        val endTotalMinutes = schedule.endHour * 60 + schedule.endMinute

        if (!isOvernight) {
            // Same-day schedule: e.g. 09:00 -> 17:00
            if (days.contains(currentDay) && currentTotalMinutes >= startTotalMinutes && currentTotalMinutes < endTotalMinutes) {
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = nowMillis
                    set(Calendar.HOUR_OF_DAY, schedule.endHour)
                    set(Calendar.MINUTE, schedule.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val remainingMs = maxOf(0L, endCal.timeInMillis - nowMillis)
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", schedule.endHour, schedule.endMinute)
                return ActiveScheduleStatus(
                    isBlocked = true,
                    scheduleTitle = schedule.title,
                    remainingMillis = remainingMs,
                    availableAtFormatted = formattedTime,
                    reason = BlockReason.SCHEDULE
                )
            }
        } else {
            // Overnight schedule: e.g. 22:00 -> 07:00
            // Case A: Before midnight today (e.g. 23:00 on Monday)
            if (days.contains(currentDay) && currentTotalMinutes >= startTotalMinutes) {
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = nowMillis
                    add(Calendar.DAY_OF_YEAR, 1) // Ends tomorrow
                    set(Calendar.HOUR_OF_DAY, schedule.endHour)
                    set(Calendar.MINUTE, schedule.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val remainingMs = maxOf(0L, endCal.timeInMillis - nowMillis)
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", schedule.endHour, schedule.endMinute)
                return ActiveScheduleStatus(
                    isBlocked = true,
                    scheduleTitle = schedule.title,
                    remainingMillis = remainingMs,
                    availableAtFormatted = formattedTime,
                    reason = BlockReason.SCHEDULE
                )
            }

            // Case B: After midnight today (e.g. 04:00 on Tuesday morning, started Monday night)
            val yesterdayDay = if (currentDay == 1) 7 else currentDay - 1
            if (days.contains(yesterdayDay) && currentTotalMinutes < endTotalMinutes) {
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = nowMillis
                    set(Calendar.HOUR_OF_DAY, schedule.endHour)
                    set(Calendar.MINUTE, schedule.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val remainingMs = maxOf(0L, endCal.timeInMillis - nowMillis)
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", schedule.endHour, schedule.endMinute)
                return ActiveScheduleStatus(
                    isBlocked = true,
                    scheduleTitle = schedule.title,
                    remainingMillis = remainingMs,
                    availableAtFormatted = formattedTime,
                    reason = BlockReason.SCHEDULE
                )
            }
        }

        return ActiveScheduleStatus.UNLOCKED
    }

    /**
     * Evaluates multiple schedules and returns the active blocking status with the longest remaining time,
     * or UNLOCKED if none are active.
     */
    fun evaluateSchedules(schedules: List<ScheduleEntity>, nowMillis: Long = System.currentTimeMillis()): ActiveScheduleStatus {
        var mostRestrictive = ActiveScheduleStatus.UNLOCKED

        for (schedule in schedules) {
            val status = evaluateSchedule(schedule, nowMillis)
            if (status.isBlocked) {
                if (status.remainingMillis > mostRestrictive.remainingMillis) {
                    mostRestrictive = status
                }
            }
        }

        return mostRestrictive
    }

    /**
     * Formats milliseconds into HH:MM:SS countdown format
     */
    fun formatRemainingCountdown(millis: Long): String {
        if (millis <= 0) return "00:00:00"
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
