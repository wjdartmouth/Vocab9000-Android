package com.jianwang.vocab9000.domain

import java.util.Locale

/** Immutable catalog row from bundled longman9000.json */
data class CatalogEntry(
    val word: String,
    val pos: String? = null,
    val sense: String? = null,
    val ja: String? = null,
    val zhHans: String? = null,
    val zhHant: String? = null,
) {
    val id: String
        get() = "${word.lowercase()}|${sense.orEmpty()}|${pos.orEmpty()}"

    val displayPos: String
        get() = if (pos.isNullOrEmpty()) "—" else pos

    val letter: String
        get() {
            val first = word.firstOrNull()?.uppercaseChar()
            return if (first != null && first.isLetter()) first.toString() else "#"
        }

    fun primaryBack(locale: Locale = Locale.getDefault()): String {
        val id = locale.toString().lowercase()
        val lang = locale.language.lowercase()
        if (id.contains("hant") || id.contains("tw") || id.contains("hk") || id.contains("mo")) {
            zhHant?.takeIf { it.isNotEmpty() }?.let { return it }
            zhHans?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        if (lang == "zh" || id.startsWith("zh")) {
            zhHans?.takeIf { it.isNotEmpty() }?.let { return it }
            zhHant?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        ja?.takeIf { it.isNotEmpty() }?.let { return it }
        return fallbackBack
    }

    val primaryBack: String get() = primaryBack()

    val fallbackBack: String
        get() {
            val parts = mutableListOf<String>()
            if (!pos.isNullOrEmpty()) parts += pos
            parts += "Longman Communication 9000"
            if (sense != null) parts += "sense $sense"
            return parts.joinToString(" · ")
        }
}
