package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_lock_prefs")

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

data class UserPreferences(
    val isProtectionActive: Boolean = true,
    val isPinSet: Boolean = false,
    val pinHash: String = "",
    val pinSalt: String = "",
    val isLockSettingsWithPin: Boolean = false,
    val isEmergencyUnlockEnabled: Boolean = true,
    val emergencyUnlockDurationMinutes: Int = 5,
    val emergencyCooldownMinutes: Int = 15,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val notificationsEnabled: Boolean = true,
    val reminderBeforeLockMinutes: Int = 15,
    val tamperAlertsEnabled: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val failedPinAttempts: Int = 0,
    val pinLockoutUntil: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val PROTECTION_ACTIVE = booleanPreferencesKey("is_protection_active")
        val PIN_SET = booleanPreferencesKey("is_pin_set")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val LOCK_SETTINGS_WITH_PIN = booleanPreferencesKey("lock_settings_with_pin")
        val EMERGENCY_UNLOCK_ENABLED = booleanPreferencesKey("emergency_unlock_enabled")
        val EMERGENCY_UNLOCK_DURATION = intPreferencesKey("emergency_unlock_duration")
        val EMERGENCY_COOLDOWN = intPreferencesKey("emergency_cooldown")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_BEFORE_LOCK = intPreferencesKey("reminder_before_lock")
        val TAMPER_ALERTS = booleanPreferencesKey("tamper_alerts")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FAILED_PIN_ATTEMPTS = intPreferencesKey("failed_pin_attempts")
        val PIN_LOCKOUT_UNTIL = longPreferencesKey("pin_lockout_until")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            isProtectionActive = preferences[PreferencesKeys.PROTECTION_ACTIVE] ?: true,
            isPinSet = preferences[PreferencesKeys.PIN_SET] ?: false,
            pinHash = preferences[PreferencesKeys.PIN_HASH] ?: "",
            pinSalt = preferences[PreferencesKeys.PIN_SALT] ?: "",
            isLockSettingsWithPin = preferences[PreferencesKeys.LOCK_SETTINGS_WITH_PIN] ?: false,
            isEmergencyUnlockEnabled = preferences[PreferencesKeys.EMERGENCY_UNLOCK_ENABLED] ?: true,
            emergencyUnlockDurationMinutes = preferences[PreferencesKeys.EMERGENCY_UNLOCK_DURATION] ?: 5,
            emergencyCooldownMinutes = preferences[PreferencesKeys.EMERGENCY_COOLDOWN] ?: 15,
            themeMode = when (preferences[PreferencesKeys.THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "SYSTEM" -> ThemeMode.SYSTEM
                else -> ThemeMode.DARK
            },
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            reminderBeforeLockMinutes = preferences[PreferencesKeys.REMINDER_BEFORE_LOCK] ?: 15,
            tamperAlertsEnabled = preferences[PreferencesKeys.TAMPER_ALERTS] ?: true,
            isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            failedPinAttempts = preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] ?: 0,
            pinLockoutUntil = preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL] ?: 0L
        )
    }

    suspend fun setProtectionActive(active: Boolean) {
        dataStore.edit { it[PreferencesKeys.PROTECTION_ACTIVE] = active }
    }

    suspend fun setPin(hash: String, salt: String) {
        dataStore.edit {
            it[PreferencesKeys.PIN_HASH] = hash
            it[PreferencesKeys.PIN_SALT] = salt
            it[PreferencesKeys.PIN_SET] = true
            it[PreferencesKeys.FAILED_PIN_ATTEMPTS] = 0
            it[PreferencesKeys.PIN_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun removePin() {
        dataStore.edit {
            it[PreferencesKeys.PIN_HASH] = ""
            it[PreferencesKeys.PIN_SALT] = ""
            it[PreferencesKeys.PIN_SET] = false
            it[PreferencesKeys.LOCK_SETTINGS_WITH_PIN] = false
        }
    }

    suspend fun setLockSettingsWithPin(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.LOCK_SETTINGS_WITH_PIN] = enabled }
    }

    suspend fun setEmergencyUnlockEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.EMERGENCY_UNLOCK_ENABLED] = enabled }
    }

    suspend fun setEmergencyUnlockDuration(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.EMERGENCY_UNLOCK_DURATION] = minutes }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun recordFailedPinAttempt(currentFailures: Int) {
        val newFailures = currentFailures + 1
        val lockoutTime = if (newFailures >= 5) {
            System.currentTimeMillis() + (30_000L * (newFailures - 4)) // 30s, 60s, etc.
        } else {
            0L
        }
        dataStore.edit {
            it[PreferencesKeys.FAILED_PIN_ATTEMPTS] = newFailures
            it[PreferencesKeys.PIN_LOCKOUT_UNTIL] = lockoutTime
        }
    }

    suspend fun resetPinAttempts() {
        dataStore.edit {
            it[PreferencesKeys.FAILED_PIN_ATTEMPTS] = 0
            it[PreferencesKeys.PIN_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
