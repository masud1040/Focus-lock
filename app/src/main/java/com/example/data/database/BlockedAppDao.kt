package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): BlockedAppEntity?

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    fun getAppFlow(packageName: String): Flow<BlockedAppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: BlockedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<BlockedAppEntity>)

    @Update
    suspend fun updateApp(app: BlockedAppEntity)

    @Query("UPDATE blocked_apps SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun setBlockedState(packageName: String, isBlocked: Boolean)

    @Query("UPDATE blocked_apps SET dailyLimitMinutes = :limitMinutes WHERE packageName = :packageName")
    suspend fun setDailyLimit(packageName: String, limitMinutes: Int)

    @Delete
    suspend fun deleteApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("DELETE FROM blocked_apps")
    suspend fun clearAll()
}
