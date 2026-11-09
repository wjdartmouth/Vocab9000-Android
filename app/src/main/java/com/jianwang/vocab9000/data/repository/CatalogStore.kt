package com.jianwang.vocab9000.data.repository

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.jianwang.vocab9000.domain.CatalogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class CatalogStore private constructor(private val appContext: Context) {
    private val _entries = MutableStateFlow<List<CatalogEntry>>(emptyList())
    val entries: StateFlow<List<CatalogEntry>> = _entries.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private var byLetter: Map<String, List<CatalogEntry>> = emptyMap()

    suspend fun loadIfNeeded() {
        if (_isLoaded.value) return
        withContext(Dispatchers.IO) {
            try {
                appContext.assets.open("longman9000.json").use { stream ->
                    val json = stream.bufferedReader().readText()
                    val arr: JsonArray = JsonParser.parseString(json).asJsonArray
                    val decoded = ArrayList<CatalogEntry>(arr.size())
                    for (el in arr) {
                        val o = el.asJsonObject
                        val word = o.get("word")?.asString ?: continue
                        decoded += CatalogEntry(
                            word = word,
                            pos = o.get("pos")?.takeIf { !it.isJsonNull }?.asString,
                            sense = o.get("sense")?.takeIf { !it.isJsonNull }?.asString,
                            ja = o.get("ja")?.takeIf { !it.isJsonNull }?.asString,
                            zhHans = o.get("zhHans")?.takeIf { !it.isJsonNull }?.asString,
                            zhHant = o.get("zhHant")?.takeIf { !it.isJsonNull }?.asString,
                        )
                    }
                    _entries.value = decoded
                    byLetter = decoded.groupBy { it.letter }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoaded.value = true
            }
        }
    }

    fun letters(): List<String> = byLetter.keys.sorted()

    fun entriesForLetter(letter: String): List<CatalogEntry> = byLetter[letter].orEmpty()

    fun entriesMatching(query: String): List<CatalogEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return _entries.value
        return _entries.value.filter {
            it.word.lowercase().startsWith(q) || it.word.lowercase().contains(q)
        }
    }

    fun entriesPosContains(token: String): List<CatalogEntry> {
        val t = token.lowercase()
        return _entries.value.filter { (it.pos ?: "").lowercase().contains(t) }
    }

    fun entryForKey(key: String): CatalogEntry? = _entries.value.firstOrNull { it.id == key }

    fun entriesLetterRange(start: Char, end: Char): List<CatalogEntry> {
        return _entries.value.filter { entry ->
            val c = entry.letter.firstOrNull() ?: return@filter false
            c.isLetter() && c in start..end
        }
    }

    companion object {
        @Volatile private var INSTANCE: CatalogStore? = null
        fun get(context: Context): CatalogStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CatalogStore(context.applicationContext).also { INSTANCE = it }
            }
    }
}
