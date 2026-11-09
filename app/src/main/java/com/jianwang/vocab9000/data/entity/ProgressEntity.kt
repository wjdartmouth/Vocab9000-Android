package com.jianwang.vocab9000.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Per-catalog-word learning + SM-2 spaced repetition state */
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val catalogKey: String,
    val word: String,
    val isLearned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
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
