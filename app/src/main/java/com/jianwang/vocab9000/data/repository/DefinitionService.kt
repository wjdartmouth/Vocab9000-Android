package com.jianwang.vocab9000.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jianwang.vocab9000.data.dao.DefinitionCacheDao
import com.jianwang.vocab9000.data.entity.DefinitionCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class DefinitionService(private val dao: DefinitionCacheDao) {

    suspend fun cached(word: String): DefinitionCacheEntity? =
        dao.get(word.lowercase())

    /** Returns cached or freshly fetched definition. Caches into Room. */
    suspend fun definition(forWord: String): Pair<String, String?> = withContext(Dispatchers.IO) {
        val key = forWord.lowercase()
        cached(key)?.let { return@withContext it.definition to it.phonetic }
        try {
            val encoded = URLEncoder.encode(forWord, "UTF-8").replace("+", "%20")
            val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$encoded")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }
            if (conn.responseCode != 200) return@withContext "" to null
            val body = conn.inputStream.bufferedReader().readText()
            val type = object : TypeToken<List<FreeDictionaryResponse>>() {}.type
            val decoded: List<FreeDictionaryResponse> = Gson().fromJson(body, type)
            val first = decoded.firstOrNull() ?: return@withContext "" to null
            val defs = (first.meanings.orEmpty()).flatMap { meaning ->
                meaning.definitions.take(2).map { d ->
                    val pos = meaning.partOfSpeech?.let { "($it) " }.orEmpty()
                    pos + d.definition
                }
            }
            val text = defs.take(3).joinToString("\n\n")
            val phonetic = first.phonetic
                ?: first.phonetics?.mapNotNull { it.text }?.firstOrNull()
            if (text.isNotEmpty()) {
                dao.upsert(
                    DefinitionCacheEntity(
                        word = key,
                        definition = text,
                        phonetic = phonetic,
                    )
                )
            }
            text to phonetic
        } catch (_: Exception) {
            "" to null
        }
    }

    data class FreeDictionaryResponse(
        val word: String? = null,
        val phonetic: String? = null,
        val phonetics: List<Phonetic>? = null,
        val meanings: List<Meaning>? = null,
    ) {
        data class Phonetic(val text: String? = null)
        data class Meaning(
            val partOfSpeech: String? = null,
            val definitions: List<Definition> = emptyList(),
        )
        data class Definition(
            val definition: String = "",
            val example: String? = null,
        )
    }
}
