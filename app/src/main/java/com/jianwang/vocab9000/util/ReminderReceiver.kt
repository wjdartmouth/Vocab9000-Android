package com.jianwang.vocab9000.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val due = intent?.getIntExtra(NotificationHelper.EXTRA_DUE, 0) ?: 0
        NotificationHelper.showNow(context, due)
        val prefs = AppPreferences(context)
        NotificationHelper.scheduleDaily(
            context,
            prefs.reminderHour,
            prefs.reminderMinute,
            due,
        )
    }
}
