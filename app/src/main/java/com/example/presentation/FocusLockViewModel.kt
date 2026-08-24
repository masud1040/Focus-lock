package com.example.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.BlockedAttemptLogEntity
import com.example.data.database.ScheduleEntity
import com.example.data.datastore.ThemeMode
import com.example.data.datastore.UserPreferences
import com.example.domain.engine.UsageStatsHelper
import com.example.domain.model.DashboardOverview
import com.example.domain.model.InstalledApp
import com.example.domain.repository.AppBlockerRepository
import com.example.security.PinSecurityManager
import com.example.service.FocusLockNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusLockViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppBlockerRepository(application)

    val userPreferences: StateFlow<UserPreferences> = repository.preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val dashboardOverview: StateFlow<DashboardOverview> = repository.getDashboardOverviewFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardOverview(
                isProtectionActive = true,
                blockedAppsCount = 0,
                totalScreenTimeMinutes = 0,
                blockedAttemptsToday = 0,
                focusTimeMinutes = 1440,
                mostBlockedAppName = null,
                mostBlockedAppCount = 0,
                nearestActiveCountdownMillis = 0L,
                nearestActiveScheduleTitle = null
            )
        )

    val schedules: StateFlow<List<ScheduleEntity>> = repository.allSchedulesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentAttempts: StateFlow<List<BlockedAttemptLogEntity>> = repository.recentAttemptsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _weeklyUsage = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val weeklyUsage: StateFlow<List<Pair<String, Int>>> = _weeklyUsage.asStateFlow()

    init {
        FocusLockNotificationHelper.createNotificationChannels(application)
        loadInstalledApps()
        loadWeeklyUsage()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            try {
                val apps = repository.getInstalledApps()
                _installedApps.value = apps
            } catch (e: Exception) {
                // Keep empty on fail
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun loadWeeklyUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val usage = UsageStatsHelper.getWeeklyUsageBreakdown(getApplication())
            _weeklyUsage.value = usage
        }
    }

    fun toggleProtection(active: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setProtectionActive(active)
            if (active) {
                val apps = repository.blockedAppDao.getActiveBlockedApps().stateIn(viewModelScope).value
                FocusLockNotificationHelper.showProtectionNotification(
                    getApplication(),
                    apps.size,
                    "Protection is active"
                )
            } else {
                FocusLockNotificationHelper.cancelProtectionNotification(getApplication())
            }
        }
    }

    fun toggleAppBlocked(packageName: String, appName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.setAppBlocked(packageName, appName, isBlocked)
            loadInstalledApps()
        }
    }

    fun setAppDailyLimit(packageName: String, appName: String, limitMinutes: Int) {
        viewModelScope.launch {
            repository.setAppDailyLimit(packageName, appName, limitMinutes)
            loadInstalledApps()
        }
    }

    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.saveSchedule(schedule)
        }
    }

    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(scheduleId)
        }
    }

    fun toggleSchedule(scheduleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setScheduleEnabled(scheduleId, isEnabled)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            val salt = PinSecurityManager.generateSalt()
            val hash = PinSecurityManager.hashPin(pin, salt)
            repository.preferencesRepository.setPin(hash, salt)
        }
    }

    fun removePin() {
        viewModelScope.launch {
            repository.preferencesRepository.removePin()
        }
    }

    fun setLockSettingsWithPin(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setLockSettingsWithPin(enabled)
        }
    }

    fun setEmergencyUnlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setEmergencyUnlockEnabled(enabled)
        }
    }

    fun setEmergencyUnlockDuration(minutes: Int) {
        viewModelScope.launch {
            repository.preferencesRepository.setEmergencyUnlockDuration(minutes)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.preferencesRepository.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            loadInstalledApps()
            loadWeeklyUsage()
        }
    }
}
