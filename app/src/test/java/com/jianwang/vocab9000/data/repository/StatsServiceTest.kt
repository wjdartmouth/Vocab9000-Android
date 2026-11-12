package com.jianwang.vocab9000.data.repository

import com.jianwang.vocab9000.data.entity.FlashCardEntity
import com.jianwang.vocab9000.data.entity.ProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsServiceTest {
    private val now = 1_700_000_000_000L
    private val day = 86_400_000L

    @Test
    fun streakIsZeroWhenEmpty() {
        assertEquals(0, StatsService.computeStreak(emptyList(), now))
    }

    @Test
    fun streakCountsToday() {
        assertEquals(1, StatsService.computeStreak(listOf(now), now))
    }

    @Test
    fun streakIncludesYesterdayWhenTodayMissing() {
        assertEquals(1, StatsService.computeStreak(listOf(now - day), now))
    }

    @Test
    fun streakBreaksOnOlderGap() {
        assertEquals(0, StatsService.computeStreak(listOf(now - 2 * day), now))
    }

    @Test
    fun streakWalksBackConsecutiveDays() {
        val dates = listOf(now, now - day, now - 2 * day, now - 5 * day)
        assertEquals(3, StatsService.computeStreak(dates, now))
    }

    @Test
    fun dueCountIgnoresNewAndFutureCards() {
        val dueProgress = ProgressEntity(
            catalogKey = "a|1|n",
            word = "apple",
            isLearned = true,
            nextReviewDate = now - 1,
            lastReviewed = now - day,
        )
        val futureProgress = ProgressEntity(
            catalogKey = "b|1|n",
            word = "banana",
            isLearned = true,
            nextReviewDate = now + day,
            lastReviewed = now - day,
        )
        val learnedNoDates = ProgressEntity(
            catalogKey = "c|1|n",
            word = "cat",
            isLearned = true,
        )
        val overlappingCard = FlashCardEntity(
            deckId = "d1",
            front = "apple",
            back = "りんご",
            catalogKey = "a|1|n",
            nextReviewDate = now - 1,
        )
        val userCardDue = FlashCardEntity(
            deckId = "d1",
            front = "custom",
            back = "カスタム",
            catalogKey = null,
            nextReviewDate = now - 1,
        )
        val count = StatsService.countDue(
            listOf(dueProgress, futureProgress, learnedNoDates),
            listOf(overlappingCard, userCardDue),
            now,
        )
        assertEquals(2, count)
    }
}
