package com.jianwang.vocab9000.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jianwang.vocab9000.Vocab9000App
import kotlinx.coroutines.runBlocking

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = AppPreferences(context)
        if (!prefs.notificationsEnabled) return
        val pending = goAsync()
        val fallback = intent?.getIntExtra(NotificationHelper.EXTRA_DUE, 0) ?: 0
        Thread {
            try {
                val due = try {
                    val app = context.applicationContext as? Vocab9000App
                    if (app != null) {
                        runBlocking { app.container.stats.dueTodayCount() }
                    } else {
                        fallback
                    }
                } catch (_: Exception) {
                    fallback
                }
                NotificationHelper.showNow(context, due)
                NotificationHelper.scheduleDaily(
                    context,
                    prefs.reminderHour,
                    prefs.reminderMinute,
                    due,
                )
            } finally {
                pending.finish()
            }
        }.start()
    }
}
