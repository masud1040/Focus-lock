package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val dailyLimitMinutes: Int = 0, // 0 = no limit, otherwise in minutes
    val category: String = "App",
    val createdAt: Long = System.currentTimeMillis()
)
