package com.example.domain

import com.example.data.database.ScheduleEntity
import com.example.domain.engine.ScheduleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleEngineTest {

    @Test
    fun testSameDaySchedule_Active() {
        val schedule = ScheduleEntity(
            id = 1L,
            packageName = "com.example.app",
            title = "Work Hours",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = "1,2,3,4,5,6,7",
            isEnabled = true
        )

        // 10:30 AM
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        val status = ScheduleEngine.isScheduleActive(schedule, cal)
        assertTrue(status.isBlocked)
        assertEquals("Work Hours", status.scheduleTitle)
        assertEquals("17:00", status.availableAtFormatted)
    }

    @Test
    fun testSameDaySchedule_Inactive() {
        val schedule = ScheduleEntity(
            id = 1L,
            packageName = "com.example.app",
            title = "Work Hours",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = "1,2,3,4,5,6,7",
            isEnabled = true
        )

        // 18:30 PM (after end)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        val status = ScheduleEngine.isScheduleActive(schedule, cal)
        assertFalse(status.isBlocked)
    }

    @Test
    fun testOvernightSchedule_ActiveLateNight() {
        val schedule = ScheduleEntity(
            id = 2L,
            packageName = "com.google.android.youtube",
            title = "Night Focus",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = "1,2,3,4,5,6,7",
            isEnabled = true
        )

        // 23:15 PM
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 15)
            set(Calendar.SECOND, 0)
        }

        val status = ScheduleEngine.isScheduleActive(schedule, cal)
        assertTrue(status.isBlocked)
        assertEquals("07:00 (Next Morning)", status.availableAtFormatted)
    }

    @Test
    fun testOvernightSchedule_ActiveEarlyMorning() {
        val schedule = ScheduleEntity(
            id = 2L,
            packageName = "com.google.android.youtube",
            title = "Night Focus",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = "1,2,3,4,5,6,7",
            isEnabled = true
        )

        // 05:45 AM
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 45)
            set(Calendar.SECOND, 0)
        }

        val status = ScheduleEngine.isScheduleActive(schedule, cal)
        assertTrue(status.isBlocked)
        assertEquals("07:00", status.availableAtFormatted)
    }

    @Test
    fun testOvernightSchedule_InactiveDaytime() {
        val schedule = ScheduleEntity(
            id = 2L,
            packageName = "com.google.android.youtube",
            title = "Night Focus",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = "1,2,3,4,5,6,7",
            isEnabled = true
        )

        // 14:00 PM
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val status = ScheduleEngine.isScheduleActive(schedule, cal)
        assertFalse(status.isBlocked)
    }

    @Test
    fun testFormatRemainingCountdown() {
        // 2 hours, 14 mins, 37 secs = (2*3600 + 14*60 + 37) * 1000 = 8077000 ms
        val formatted = ScheduleEngine.formatRemainingCountdown(8077000L)
        assertEquals("02:14:37", formatted)
    }
}
