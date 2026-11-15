package com.jianwang.vocab9000.widget

import android.content.Context
import com.jianwang.vocab9000.data.repository.WidgetSnapshot

object WidgetSnapshotStore {
    private const val PREFS = "vocab9000.widget"
    private const val KEY_DUE = "dueToday"
    private const val KEY_STREAK = "streakDays"
    private const val KEY_LEARNED = "learnedCount"
    private const val KEY_UPDATED = "updatedAt"

    fun load(context: Context): WidgetSnapshot {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return WidgetSnapshot(
            dueToday = prefs.getInt(KEY_DUE, 0),
            streakDays = prefs.getInt(KEY_STREAK, 0),
            learnedCount = prefs.getInt(KEY_LEARNED, 0),
            updatedAt = prefs.getLong(KEY_UPDATED, 0L),
        )
    }

    fun save(context: Context, snapshot: WidgetSnapshot) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DUE, snapshot.dueToday)
            .putInt(KEY_STREAK, snapshot.streakDays)
            .putInt(KEY_LEARNED, snapshot.learnedCount)
            .putLong(KEY_UPDATED, snapshot.updatedAt)
            .apply()
    }
}
