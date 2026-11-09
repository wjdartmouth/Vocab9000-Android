package com.jianwang.vocab9000.data.repository

import com.jianwang.vocab9000.data.db.AppDatabase
import com.jianwang.vocab9000.data.entity.DeckEntity
import java.util.Calendar

data class LetterProgress(
    val letter: String,
    val learned: Int,
    val total: Int,
) {
    val ratio: Double get() = if (total == 0) 0.0 else learned.toDouble() / total
}

data class DeckProgress(
    val id: String,
    val title: String,
    val learned: Int,
    val total: Int,
    val due: Int,
) {
    val ratio: Double get() = if (total == 0) 0.0 else learned.toDouble() / total
}

data class StudyStatsSnapshot(
    val catalogTotal: Int,
    val learnedCount: Int,
    val dueToday: Int,
    val reviewedToday: Int,
    val inProgress: Int,
    val lastStudied: Long?,
    val streakDays: Int,
    val byLetter: List<LetterProgress>,
    val byDeck: List<DeckProgress>,
)

class StatsService(
    private val db: AppDatabase,
    private val catalog: CatalogStore,
    private val seed: SeedService,
) {
    suspend fun snapshot(): StudyStatsSnapshot {
        catalog.loadIfNeeded()
        val progress = db.progressDao().getAll()
        val cards = db.flashCardDao().getAll()
        val decks = db.deckDao().getAll()
        val progressByKey = progress.associateBy { it.catalogKey }
        val now = System.currentTimeMillis()
        val startOfToday = startOfDay(now)

        val learnedCatalog = progress.count { it.isLearned }
        val learnedUserOnly = cards.count { it.catalogKey == null && it.isLearned }
        val learnedCount = learnedCatalog + learnedUserOnly

        val dueProgress = progress.count { row ->
            val next = row.nextReviewDate
            if (next == null) row.isLearned && row.lastReviewed != null
            else next <= now
        }
        val dueCards = cards.count { card ->
            if (card.catalogKey != null && progressByKey[card.catalogKey] != null) false
            else {
                val next = card.nextReviewDate ?: return@count false
                next <= now
            }
        }
        val dueToday = dueProgress + dueCards

        val reviewedToday = progress.count { row ->
            val last = row.lastReviewed ?: return@count false
            last >= startOfToday
        } + cards.count { card ->
            if (card.catalogKey != null) return@count false
            val last = card.lastReviewed ?: return@count false
            last >= startOfToday
        }

        val inProgress = progress.count { !it.isNew && !it.isLearned }

        val lastDates = progress.mapNotNull { it.lastReviewed } + cards.mapNotNull { it.lastReviewed }
        val lastStudied = lastDates.maxOrNull()
        val streakDays = computeStreak(lastDates, now)

        val letterTotals = mutableMapOf<String, Int>()
        val letterLearned = mutableMapOf<String, Int>()
        for (entry in catalog.entries.value) {
            val L = entry.letter
            letterTotals[L] = (letterTotals[L] ?: 0) + 1
            if (progressByKey[entry.id]?.isLearned == true) {
                letterLearned[L] = (letterLearned[L] ?: 0) + 1
            }
        }
        val byLetter = letterTotals.keys.sorted().map { L ->
            LetterProgress(L, letterLearned[L] ?: 0, letterTotals[L] ?: 0)
        }

        val byDeck = decks.map { deck ->
            if (deck.isBuiltIn) {
                val (learned, total) = seed.builtInStats(deck)
                val due = seed.dueCount(deck)
                DeckProgress(deck.id, localizedDeckTitle(deck.title), learned, total, due)
            } else {
                val deckCards = cards.filter { it.deckId == deck.id }
                DeckProgress(
                    deck.id,
                    localizedDeckTitle(deck.title),
                    deckCards.count { it.isLearned },
                    deckCards.size,
                    deckCards.count { it.isDue(now) },
                )
            }
        }.sortedByDescending { it.ratio }

        return StudyStatsSnapshot(
            catalogTotal = catalog.entries.value.size,
            learnedCount = learnedCount,
            dueToday = dueToday,
            reviewedToday = reviewedToday,
            inProgress = inProgress,
            lastStudied = lastStudied,
            streakDays = streakDays,
            byLetter = byLetter,
            byDeck = byDeck,
        )
    }

    companion object {
        fun startOfDay(millis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun computeStreak(dates: List<Long>, now: Long = System.currentTimeMillis()): Int {
            if (dates.isEmpty()) return 0
            val days = dates.map { startOfDay(it) }.toSet()
            var cursor = startOfDay(now)
            if (cursor !in days) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = cursor
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = cal.timeInMillis
                if (yesterday !in days) return 0
                cursor = yesterday
            }
            var streak = 0
            while (cursor in days) {
                streak++
                val cal = Calendar.getInstance()
                cal.timeInMillis = cursor
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cursor = cal.timeInMillis
            }
            return streak
        }

        fun localizedDeckTitle(title: String): String = when (title) {
            "__seed.starter50__", "はじめての50語" -> "はじめての50語"
            else -> title
        }
    }
}
