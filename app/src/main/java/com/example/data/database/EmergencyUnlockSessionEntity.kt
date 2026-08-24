package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_unlock_sessions")
data class EmergencyUnlockSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String, // package name or "ALL"
    val unlockedAt: Long,
    val expiresAt: Long,
    val durationMinutes: Int,
    val isActive: Boolean = true
)
