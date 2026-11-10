package com.jianwang.vocab9000.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val prefs = AppPreferences(context)
        if (prefs.notificationsEnabled) {
            NotificationHelper.scheduleDaily(
                context,
                prefs.reminderHour,
                prefs.reminderMinute,
                0,
            )
        }
    }
}
