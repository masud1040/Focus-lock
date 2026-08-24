package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BlockedAppEntity::class,
        ScheduleEntity::class,
        BlockedAttemptLogEntity::class,
        EmergencyUnlockSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusLockDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun blockedAttemptDao(): BlockedAttemptDao
    abstract fun emergencyUnlockDao(): EmergencyUnlockDao

    companion object {
        @Volatile
        private var INSTANCE: FocusLockDatabase? = null

        fun getInstance(context: Context): FocusLockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusLockDatabase::class.java,
                    "focus_lock_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
