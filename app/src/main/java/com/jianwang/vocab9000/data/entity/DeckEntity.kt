package com.jianwang.vocab9000.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val languageCode: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val isBuiltIn: Boolean = false,
    /** Optional filter for virtual built-in letter-range decks, e.g. "A-C" */
    val letterRange: String? = null,
)
