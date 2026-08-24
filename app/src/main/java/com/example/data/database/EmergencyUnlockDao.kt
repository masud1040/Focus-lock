package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyUnlockDao {
    @Query("SELECT * FROM emergency_unlock_sessions WHERE (packageName = :packageName OR packageName = 'ALL') AND expiresAt > :currentTime AND isActive = 1 ORDER BY expiresAt DESC LIMIT 1")
    suspend fun getActiveUnlockSession(packageName: String, currentTime: Long = System.currentTimeMillis()): EmergencyUnlockSessionEntity?

    @Query("SELECT * FROM emergency_unlock_sessions WHERE expiresAt > :currentTime AND isActive = 1 ORDER BY expiresAt DESC")
    fun getAllActiveUnlockSessions(currentTime: Long = System.currentTimeMillis()): Flow<List<EmergencyUnlockSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: EmergencyUnlockSessionEntity): Long

    @Query("UPDATE emergency_unlock_sessions SET isActive = 0 WHERE packageName = :packageName OR packageName = 'ALL'")
    suspend fun deactivateSession(packageName: String)

    @Query("UPDATE emergency_unlock_sessions SET isActive = 0 WHERE expiresAt <= :currentTime")
    suspend fun expireOldSessions(currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM emergency_unlock_sessions")
    suspend fun clearAll()
}
