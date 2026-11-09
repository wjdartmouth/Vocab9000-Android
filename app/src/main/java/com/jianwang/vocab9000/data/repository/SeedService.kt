package com.jianwang.vocab9000.data.repository

import android.content.Context
import com.jianwang.vocab9000.data.db.AppDatabase
import com.jianwang.vocab9000.data.entity.DeckEntity
import com.jianwang.vocab9000.data.entity.FlashCardEntity
import com.jianwang.vocab9000.data.entity.ProgressEntity
import com.jianwang.vocab9000.domain.CatalogEntry
import com.jianwang.vocab9000.domain.SRSGrade
import com.jianwang.vocab9000.domain.SRSService
import com.jianwang.vocab9000.domain.SRSState
import com.jianwang.vocab9000.domain.StudyItem
import java.util.Date
import java.util.Locale

/**
 * Port of iOS SeedService: first-launch decks, SRS apply, study queues.
 * Built-in "All" never materializes 9000 FlashCards — ProgressEntity aggregation only.
 */
class SeedService(
    private val context: Context,
    private val db: AppDatabase,
    private val catalog: CatalogStore,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    suspend fun seedIfNeeded() {
        if (prefs.getBoolean(SEEDED_KEY, false)) {
            migrateSRSIfNeeded()
            return
        }
        catalog.loadIfNeeded()

        val ranges = listOf(
            Triple("Longman 9000 — A–C", 'A', 'C'),
            Triple("Longman 9000 — D–F", 'D', 'F'),
            Triple("Longman 9000 — G–I", 'G', 'I'),
            Triple("Longman 9000 — J–L", 'J', 'L'),
            Triple("Longman 9000 — M–O", 'M', 'O'),
            Triple("Longman 9000 — P–R", 'P', 'R'),
            Triple("Longman 9000 — S–U", 'S', 'U'),
            Triple("Longman 9000 — V–Z", 'V', 'Z'),
        )

        val decks = mutableListOf(
            DeckEntity(
                title = "Longman 9000 — All",
                languageCode = "en",
                isBuiltIn = true,
                letterRange = "A-Z",
            )
        )
        for ((title, start, end) in ranges) {
            decks += DeckEntity(
                title = title,
                languageCode = "en",
                isBuiltIn = true,
                letterRange = "$start-$end",
            )
        }
        val starter = DeckEntity(
            title = "__seed.starter50__",
            languageCode = "en",
            isBuiltIn = false,
        )
        decks += starter
        db.deckDao().upsertAll(decks)

        val locale = Locale.getDefault()
        val cards = catalog.entries.value.take(50).map { entry ->
            FlashCardEntity(
                deckId = starter.id,
                front = entry.word,
                back = entry.primaryBack(locale),
                pos = entry.pos,
                catalogKey = entry.id,
            )
        }
        db.flashCardDao().upsertAll(cards)

        prefs.edit()
            .putBoolean(SEEDED_KEY, true)
            .putBoolean(SRS_MIGRATED_KEY, true)
            .apply()
    }

    suspend fun migrateSRSIfNeeded() {
        if (prefs.getBoolean(SRS_MIGRATED_KEY, false)) return
        for (row in db.progressDao().getAll()) {
            if (row.isLearned && row.lastReviewed == null && row.repetitions == 0) {
                val state = SRSService.migrateIfNeeded(
                    SRSState(
                        easeFactor = if (row.easeFactor == 0.0) 2.5 else row.easeFactor,
                        intervalDays = row.intervalDays,
                        repetitions = row.repetitions,
                        nextReviewDate = row.nextReviewDate?.let { Date(it) },
                        lastReviewed = row.lastReviewed?.let { Date(it) },
                        isLearned = row.isLearned,
                    ),
                    legacyLearned = true,
                )
                db.progressDao().upsert(applyToProgress(row, state))
            }
        }
        for (card in db.flashCardDao().getAll()) {
            if (card.isLearned && card.lastReviewed == null && card.repetitions == 0) {
                val state = SRSService.migrateIfNeeded(
                    SRSState(
                        easeFactor = if (card.easeFactor == 0.0) 2.5 else card.easeFactor,
                        intervalDays = card.intervalDays,
                        repetitions = card.repetitions,
                        nextReviewDate = card.nextReviewDate?.let { Date(it) },
                        lastReviewed = card.lastReviewed?.let { Date(it) },
                        isLearned = card.isLearned,
                    ),
                    legacyLearned = true,
                )
                db.flashCardDao().upsert(applyToCard(card, state))
            }
        }
        prefs.edit().putBoolean(SRS_MIGRATED_KEY, true).apply()
    }

    fun catalogEntriesFor(deck: DeckEntity): List<CatalogEntry> {
        if (!deck.isBuiltIn) return emptyList()
        val range = deck.letterRange ?: return emptyList()
        if (range == "A-Z") return catalog.entries.value
        val parts = range.split("-")
        if (parts.size != 2) return emptyList()
        val s = parts[0].firstOrNull() ?: return emptyList()
        val e = parts[1].firstOrNull() ?: return emptyList()
        return catalog.entriesLetterRange(s, e)
    }

    suspend fun builtInStats(deck: DeckEntity): Pair<Int, Int> {
        val entries = catalogEntriesFor(deck)
        val total = entries.size
        if (total == 0) return 0 to 0
        val progress = db.progressDao().getAll().associateBy { it.catalogKey }
        val learned = entries.count { progress[it.id]?.isLearned == true }
        return learned to total
    }

    suspend fun dueCount(deck: DeckEntity): Int {
        val now = System.currentTimeMillis()
        if (deck.isBuiltIn && deck.letterRange != null) {
            val entries = catalogEntriesFor(deck)
            val progress = db.progressDao().getAll().associateBy { it.catalogKey }
            return entries.count { entry ->
                val row = progress[entry.id] ?: return@count false
                val isNew = row.isNew
                if (isNew) return@count false
                val next = row.nextReviewDate
                when {
                    next != null -> next <= now
                    else -> row.repetitions > 0 || row.isLearned
                }
            }
        }
        val cards = db.flashCardDao().getForDeck(deck.id)
        return cards.count { card ->
            if (card.isNew) return@count false
            val next = card.nextReviewDate
            when {
                next != null -> next <= now
                else -> card.repetitions > 0 || card.isLearned
            }
        }
    }

    suspend fun nextDueDate(deck: DeckEntity): Long? {
        val now = System.currentTimeMillis()
        if (deck.isBuiltIn && deck.letterRange != null) {
            val keys = catalogEntriesFor(deck).map { it.id }.toSet()
            return db.progressDao().getAll()
                .filter { it.catalogKey in keys }
                .mapNotNull { it.nextReviewDate }
                .filter { it > now }
                .minOrNull()
        }
        return db.flashCardDao().getForDeck(deck.id)
            .mapNotNull { it.nextReviewDate }
            .filter { it > now }
            .minOrNull()
    }

    suspend fun studyQueue(deck: DeckEntity, capped: Boolean = true): List<StudyItem> {
        val all = studyItems(deck)
        val now = System.currentTimeMillis()
        val due = all.filter { item ->
            if (item.isNew) return@filter false
            val next = item.nextReviewDate?.time
            if (next != null) next <= now
            else item.repetitions > 0 || item.isLearned
        }.sortedBy { it.nextReviewDate?.time ?: Long.MIN_VALUE }

        val dueIds = due.map { it.id }.toSet()
        val news = all.filter { it.isNew && it.id !in dueIds }
        var queue = due + news
        if (queue.isEmpty()) {
            queue = all.filter { !it.isLearned } + all.filter { it.isLearned }
        }
        if (!capped) return queue
        val result = mutableListOf<StudyItem>()
        var newUsed = 0
        for (item in queue) {
            if (item.isNew) {
                if (newUsed >= SRSService.NEW_CARD_CAP) continue
                newUsed++
            }
            result += item
            if (result.size >= SRSService.SESSION_CAP) break
        }
        return result
    }

    suspend fun studyItems(deck: DeckEntity): List<StudyItem> {
        val now = System.currentTimeMillis()
        if (deck.isBuiltIn && deck.letterRange != null) {
            val entries = catalogEntriesFor(deck)
            val progress = db.progressDao().getAll().associateBy { it.catalogKey }
            val locale = Locale.getDefault()
            return entries.map { entry ->
                val row = progress[entry.id]
                var state = row?.let {
                    SRSState(
                        easeFactor = if (it.easeFactor == 0.0) 2.5 else it.easeFactor,
                        intervalDays = it.intervalDays,
                        repetitions = it.repetitions,
                        nextReviewDate = it.nextReviewDate?.let { t -> Date(t) },
                        lastReviewed = it.lastReviewed?.let { t -> Date(t) },
                        isLearned = it.isLearned,
                    )
                } ?: SRSState.DEFAULT
                if (row?.isLearned == true) {
                    state = SRSService.migrateIfNeeded(state, legacyLearned = true)
                }
                val isNew = state.lastReviewed == null && state.repetitions == 0 && state.intervalDays == 0.0
                val isDue = when {
                    isNew -> false
                    state.nextReviewDate == null -> true
                    else -> state.nextReviewDate!!.time <= now
                }
                StudyItem(
                    id = entry.id,
                    front = entry.word,
                    back = entry.primaryBack(locale),
                    ja = entry.ja,
                    pos = entry.pos,
                    catalogKey = entry.id,
                    isLearned = state.isLearned,
                    isNew = isNew,
                    isDue = isDue || (!isNew && state.nextReviewDate == null),
                    nextReviewDate = state.nextReviewDate,
                    intervalDays = state.intervalDays,
                    easeFactor = state.easeFactor,
                    repetitions = state.repetitions,
                    flashCardId = null,
                )
            }
        }
        return db.flashCardDao().getForDeck(deck.id).map { card ->
            var state = SRSState(
                easeFactor = if (card.easeFactor == 0.0) 2.5 else card.easeFactor,
                intervalDays = card.intervalDays,
                repetitions = card.repetitions,
                nextReviewDate = card.nextReviewDate?.let { Date(it) },
                lastReviewed = card.lastReviewed?.let { Date(it) },
                isLearned = card.isLearned,
            )
            if (card.isLearned) {
                state = SRSService.migrateIfNeeded(state, legacyLearned = true)
            }
            val isNew = card.isNew
            val isDue = card.isDue(now) || (!isNew && card.nextReviewDate == null && !card.isLearned)
            StudyItem(
                id = card.id,
                front = card.front,
                back = card.back,
                ja = null,
                pos = card.pos,
                catalogKey = card.catalogKey,
                isLearned = state.isLearned,
                isNew = isNew,
                isDue = isDue,
                nextReviewDate = state.nextReviewDate,
                intervalDays = state.intervalDays,
                easeFactor = state.easeFactor,
                repetitions = state.repetitions,
                flashCardId = card.id,
            )
        }
    }

    suspend fun applyGrade(
        catalogKey: String?,
        word: String,
        grade: SRSGrade,
        flashCardId: String?,
    ) {
        if (flashCardId != null) {
            val card = db.flashCardDao().getById(flashCardId)
            if (card != null) {
                var state = SRSState(
                    easeFactor = if (card.easeFactor == 0.0) 2.5 else card.easeFactor,
                    intervalDays = card.intervalDays,
                    repetitions = card.repetitions,
                    nextReviewDate = card.nextReviewDate?.let { Date(it) },
                    lastReviewed = card.lastReviewed?.let { Date(it) },
                    isLearned = card.isLearned,
                )
                state = SRSService.schedule(state, grade)
                db.flashCardDao().upsert(applyToCard(card, state))
            }
        }
        if (catalogKey != null) {
            val existing = db.progressDao().getByKey(catalogKey)
            val row = existing ?: ProgressEntity(catalogKey = catalogKey, word = word)
            var state = SRSState(
                easeFactor = if (row.easeFactor == 0.0) 2.5 else row.easeFactor,
                intervalDays = row.intervalDays,
                repetitions = row.repetitions,
                nextReviewDate = row.nextReviewDate?.let { Date(it) },
                lastReviewed = row.lastReviewed?.let { Date(it) },
                isLearned = row.isLearned,
            )
            state = SRSService.schedule(state, grade)
            db.progressDao().upsert(applyToProgress(row, state))
        }
    }

    suspend fun setLearned(catalogKey: String, word: String, learned: Boolean) {
        applyGrade(
            catalogKey = catalogKey,
            word = word,
            grade = if (learned) SRSGrade.GOOD else SRSGrade.AGAIN,
            flashCardId = null,
        )
    }

    private fun applyToProgress(row: ProgressEntity, state: SRSState) = row.copy(
        easeFactor = state.easeFactor,
        intervalDays = state.intervalDays,
        repetitions = state.repetitions,
        nextReviewDate = state.nextReviewDate?.time,
        lastReviewed = state.lastReviewed?.time,
        isLearned = state.isLearned,
        updatedAt = System.currentTimeMillis(),
    )

    private fun applyToCard(card: FlashCardEntity, state: SRSState) = card.copy(
        easeFactor = state.easeFactor,
        intervalDays = state.intervalDays,
        repetitions = state.repetitions,
        nextReviewDate = state.nextReviewDate?.time,
        lastReviewed = state.lastReviewed?.time,
        isLearned = state.isLearned,
        updatedAt = System.currentTimeMillis(),
    )

    companion object {
        private const val PREFS = "vocab9000"
        private const val SEEDED_KEY = "vocab9000.seeded.v1"
        private const val SRS_MIGRATED_KEY = "vocab9000.srs.migrated.v1"
    }
}
