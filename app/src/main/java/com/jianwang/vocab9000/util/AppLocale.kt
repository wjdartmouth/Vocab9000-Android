package com.jianwang.vocab9000.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/** Maps the in-app UI language preference onto a Context and Locale.setDefault. */
object AppLocale {
    fun localeFor(tag: String): Locale = when (tag) {
        "en" -> Locale.ENGLISH
        "zh-Hans" -> Locale.SIMPLIFIED_CHINESE
        "zh-Hant" -> Locale.TRADITIONAL_CHINESE
        else -> Locale.JAPANESE
    }

    fun wrap(base: Context, tag: String = AppPreferences(base).uiLanguage): Context {
        val locale = localeFor(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }
}
