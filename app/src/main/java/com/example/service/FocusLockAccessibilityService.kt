package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.domain.model.BlockReason
import com.example.domain.repository.AppBlockerRepository
import com.example.presentation.blocking.BlockingOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusLockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: AppBlockerRepository

    private var lastBlockedPackage: String? = null
    private var lastBlockedTimestamp: Long = 0L

    override fun onCreate() {
        super.onCreate()
        repository = AppBlockerRepository(applicationContext)
        FocusLockNotificationHelper.createNotificationChannels(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Ignore our own package and system UI navigation / keyboard
            if (packageName == applicationContext.packageName ||
                packageName == "com.android.systemui" ||
                packageName == "com.google.android.inputmethod.latin" ||
                packageName.contains("launcher", ignoreCase = true)
            ) {
                return
            }

            // Throttle duplicate events within 800ms for the same package
            val now = SystemClock.elapsedRealtime()
            if (packageName == lastBlockedPackage && (now - lastBlockedTimestamp) < 800) {
                return
            }

            serviceScope.launch {
                try {
                    val prefs = repository.preferencesRepository.userPreferencesFlow.first()
                    if (!prefs.isProtectionActive) return@launch

                    val decision = repository.shouldBlockApp(packageName)
                    if (decision.isBlocked) {
                        lastBlockedPackage = packageName
                        lastBlockedTimestamp = SystemClock.elapsedRealtime()

                        // Log the blocked attempt
                        repository.logBlockedAttempt(
                            packageName = packageName,
                            appName = decision.appName,
                            reason = decision.reason.name
                        )

                        // Launch full-screen blocking overlay
                        val intent = Intent(applicationContext, BlockingOverlayActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra(BlockingOverlayActivity.EXTRA_PACKAGE_NAME, decision.packageName)
                            putExtra(BlockingOverlayActivity.EXTRA_APP_NAME, decision.appName)
                            putExtra(BlockingOverlayActivity.EXTRA_REASON, decision.reason.name)
                            putExtra(BlockingOverlayActivity.EXTRA_SCHEDULE_TITLE, decision.scheduleTitle)
                            putExtra(BlockingOverlayActivity.EXTRA_REMAINING_MS, decision.remainingMillis)
                            putExtra(BlockingOverlayActivity.EXTRA_AVAILABLE_AT, decision.availableAtFormatted)
                        }
                        startActivity(intent)

                        // Return to home so the blocked app is closed
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                } catch (e: Exception) {
                    Log.e("FocusLockService", "Error evaluating blocked app: ${e.message}", e)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
