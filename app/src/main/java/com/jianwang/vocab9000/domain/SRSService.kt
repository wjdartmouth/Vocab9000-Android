package com.jianwang.vocab9000.domain

import java.util.Calendar
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

/** Anki-style grades mapped onto SM-2 quality. */
enum class SRSGrade(val quality: Int) {
    AGAIN(1),
    HARD(3),
    GOOD(4),
    EASY(5);
}

data class SRSState(
    val easeFactor: Double = 2.5,
    val intervalDays: Double = 0.0,
    val repetitions: Int = 0,
    val nextReviewDate: Date? = null,
    val lastReviewed: Date? = null,
    val isLearned: Boolean = false,
) {
    companion object {
        val DEFAULT = SRSState()
    }
}

/**
 * SuperMemo SM-2 scheduling (quality 0–5). Port of iOS SRSService.swift.
 * Grades Again/Hard/Good/Easy map to quality 1/3/4/5.
 */
object SRSService {
    const val MIN_EASE = 1.3
    const val SESSION_CAP = 40
    const val NEW_CARD_CAP = 20

    fun schedule(state: SRSState, grade: SRSGrade, now: Date = Date()): SRSState {
        val q = grade.quality
        var ef = state.easeFactor
        var interval = state.intervalDays
        var reps = state.repetitions

        // Ease update (SM-2)
        ef += 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
        if (ef < MIN_EASE) ef = MIN_EASE

        if (q < 3) {
            // Again: reset streak, review in ~10 minutes
            return state.copy(
                easeFactor = ef,
                intervalDays = 0.0,
                repetitions = 0,
                lastReviewed = now,
                nextReviewDate = Date(now.time + 10 * 60 * 1000L),
                isLearned = false,
            )
        }

        // Passed
        if (reps == 0) {
            interval = when (grade) {
                SRSGrade.EASY -> 4.0
                SRSGrade.HARD -> 1.0
                else -> 1.0
            }
        } else if (reps == 1) {
            interval = when (grade) {
                SRSGrade.EASY -> 7.0
                SRSGrade.HARD -> 3.0
                else -> 6.0
            }
        } else {
            val mult = when (grade) {
                SRSGrade.HARD -> 1.2
                SRSGrade.GOOD -> ef
                SRSGrade.EASY -> ef * 1.3
                SRSGrade.AGAIN -> 1.0
            }
            interval = max(1.0, (interval * mult).roundToInt().toDouble())
        }
        reps += 1

        val cal = Calendar.getInstance()
        cal.time = now
        cal.add(Calendar.DAY_OF_YEAR, interval.roundToInt())
        val next = cal.time

        return state.copy(
            easeFactor = ef,
            intervalDays = interval,
            repetitions = reps,
            lastReviewed = now,
            nextReviewDate = next,
            isLearned = reps >= 2 || interval >= 6,
        )
    }

    /** Migrate legacy isLearned-only rows into a sensible SRS snapshot. */
    fun migrateIfNeeded(state: SRSState, legacyLearned: Boolean): SRSState {
        if (!legacyLearned || state.lastReviewed != null || state.repetitions != 0) return state
        val yesterday = Date(System.currentTimeMillis() - 86_400_000L)
        return state.copy(
            easeFactor = max(state.easeFactor, 2.5),
            intervalDays = 1.0,
            repetitions = 1,
            lastReviewed = yesterday,
            nextReviewDate = Date(),
            isLearned = true,
        )
    }

    fun formatIntervalHintDays(days: Double, grade: SRSGrade): String {
        if (grade == SRSGrade.AGAIN) return "<10m"
        if (days <= 0) {
            return when (grade) {
                SRSGrade.EASY -> "4d"
                SRSGrade.AGAIN -> "<10m"
                else -> "1d"
            }
        }
        return "${max(1, days.roundToInt())}d"
    }
}
