package com.jianwang.vocab9000.domain

import java.util.Date

data class StudyItem(
    val id: String,
    val front: String,
    val back: String,
    val ja: String? = null,
    val pos: String? = null,
    val catalogKey: String? = null,
    val isLearned: Boolean = false,
    val isNew: Boolean = true,
    val isDue: Boolean = false,
    val nextReviewDate: Date? = null,
    val intervalDays: Double = 0.0,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
    val flashCardId: String? = null,
)
