package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.domain.repository.AppBlockerRepository
import com.example.service.FocusLockNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            FocusLockNotificationHelper.createNotificationChannels(context)

            val repository = AppBlockerRepository(context.applicationContext)
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val prefs = repository.preferencesRepository.userPreferencesFlow.first()
                if (prefs.isProtectionActive && prefs.notificationsEnabled) {
                    val activeApps = repository.blockedAppDao.getActiveBlockedApps().first()
                    FocusLockNotificationHelper.showProtectionNotification(
                        context,
                        activeApps.size,
                        "Protection restored after restart"
                    )
                }
            }
        }
    }
}
