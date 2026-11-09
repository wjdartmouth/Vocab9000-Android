package com.jianwang.vocab9000.util

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var ttsLanguage: String
        get() = prefs.getString(KEY_TTS, "en") ?: "en"
        set(v) = prefs.edit().putString(KEY_TTS, v).apply()

    var uiLanguage: String
        get() = prefs.getString(KEY_UI_LANG, "ja") ?: "ja"
        set(v) = prefs.edit().putString(KEY_UI_LANG, v).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIF, false)
        set(v) = prefs.edit().putBoolean(KEY_NOTIF, v).apply()

    var reminderHour: Int
        get() = prefs.getInt(KEY_HOUR, 20)
        set(v) = prefs.edit().putInt(KEY_HOUR, v).apply()

    var reminderMinute: Int
        get() = prefs.getInt(KEY_MINUTE, 0)
        set(v) = prefs.edit().putInt(KEY_MINUTE, v).apply()

    companion object {
        private const val PREFS = "vocab9000.settings"
        private const val KEY_TTS = "tts.language"
        private const val KEY_UI_LANG = "ui.language"
        private const val KEY_NOTIF = "settings.notifications.enabled"
        private const val KEY_HOUR = "settings.notifications.hour"
        private const val KEY_MINUTE = "settings.notifications.minute"
    }
}
