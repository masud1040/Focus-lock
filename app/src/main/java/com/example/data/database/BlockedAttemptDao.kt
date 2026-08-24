package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAttemptDao {
    @Query("SELECT * FROM blocked_attempts ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAttempts(): Flow<List<BlockedAttemptLogEntity>>

    @Query("SELECT COUNT(*) FROM blocked_attempts WHERE timestamp >= :sinceTimestamp")
    fun getAttemptCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT packageName, appName, COUNT(*) as count FROM blocked_attempts WHERE timestamp >= :sinceTimestamp GROUP BY packageName ORDER BY count DESC LIMIT 1")
    suspend fun getMostBlockedAppSince(sinceTimestamp: Long): MostBlockedAppResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logAttempt(attempt: BlockedAttemptLogEntity)

    @Query("DELETE FROM blocked_attempts WHERE timestamp < :beforeTimestamp")
    suspend fun pruneOldLogs(beforeTimestamp: Long)

    @Query("DELETE FROM blocked_attempts")
    suspend fun clearAll()
}

data class MostBlockedAppResult(
    val packageName: String,
    val appName: String,
    val count: Int
)
