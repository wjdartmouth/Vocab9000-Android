package com.jianwang.vocab9000.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "flash_cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("deckId"), Index("catalogKey")],
)
data class FlashCardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deckId: String,
    val front: String,
    val back: String,
    val example: String? = null,
    val pos: String? = null,
    val catalogKey: String? = null,
    val isLearned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    // SM-2
    val easeFactor: Double = 2.5,
    val intervalDays: Double = 0.0,
    val repetitions: Int = 0,
    val nextReviewDate: Long? = null,
    val lastReviewed: Long? = null,
) {
    val isNew: Boolean
        get() = lastReviewed == null && repetitions == 0 && intervalDays == 0.0

    fun isDue(now: Long = System.currentTimeMillis()): Boolean {
        val next = nextReviewDate ?: return false
        return next <= now
    }
}
