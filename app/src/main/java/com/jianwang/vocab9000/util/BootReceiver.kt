package com.jianwang.vocab9000.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
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
