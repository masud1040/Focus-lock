package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String = "", // Empty means all blocked apps or specific package
    val title: String,
    val startHour: Int, // 0-23
    val startMinute: Int, // 0-59
    val endHour: Int, // 0-23
    val endMinute: Int, // 0-59
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val isEnabled: Boolean = true,
    val dailyLimitMinutes: Int = 0, // optional limit attached to this schedule
    val createdAt: Long = System.currentTimeMillis()
)
