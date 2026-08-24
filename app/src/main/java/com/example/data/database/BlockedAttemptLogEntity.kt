package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_attempts")
data class BlockedAttemptLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = "SCHEDULE" // "SCHEDULE", "DAILY_LIMIT", "DIRECT_LOCK"
)
