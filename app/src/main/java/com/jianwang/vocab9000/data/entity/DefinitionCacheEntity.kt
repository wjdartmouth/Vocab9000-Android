package com.jianwang.vocab9000.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "definition_cache")
data class DefinitionCacheEntity(
    @PrimaryKey val word: String,
    val definition: String,
    val phonetic: String? = null,
    val fetchedAt: Long = System.currentTimeMillis(),
)
