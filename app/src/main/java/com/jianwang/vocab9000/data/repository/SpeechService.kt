package com.jianwang.vocab9000.data.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechService(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var languageTag: String = "en"

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) applyLanguage(languageTag)
    }

    fun setLanguage(tag: String) {
        languageTag = tag
        if (ready) applyLanguage(tag)
    }

    private fun applyLanguage(tag: String) {
        val locale = when (tag) {
            "ja" -> Locale.JAPANESE
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            "zh-TW", "zh-HK" -> Locale.TRADITIONAL_CHINESE
            "ko" -> Locale.KOREAN
            "fr" -> Locale.FRENCH
            "es" -> Locale("es", "ES")
            else -> Locale.US
        }
        tts?.language = locale
    }

    fun speak(text: String, languageCode: String? = null) {
        if (!ready) return
        if (languageCode != null) applyLanguage(languageCode)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocab9000")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
