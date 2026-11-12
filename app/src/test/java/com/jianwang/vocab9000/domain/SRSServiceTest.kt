package com.jianwang.vocab9000.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class SRSServiceTest {
    private val now = Date(1_700_000_000_000L)

    @Test
    fun firstGoodIsOneDayAndNotYetLearned() {
        val next = SRSService.schedule(SRSState.DEFAULT, SRSGrade.GOOD, now)
        assertEquals(2.5, next.easeFactor, 1e-9)
        assertEquals(1.0, next.intervalDays, 1e-9)
        assertEquals(1, next.repetitions)
        assertFalse(next.isLearned)
        assertEquals(now, next.lastReviewed)
        assertEquals(daysLater(now, 1), next.nextReviewDate)
    }

    @Test
    fun firstEasyIsFourDays() {
        val next = SRSService.schedule(SRSState.DEFAULT, SRSGrade.EASY, now)
        assertEquals(2.6, next.easeFactor, 1e-9)
        assertEquals(4.0, next.intervalDays, 1e-9)
        assertEquals(1, next.repetitions)
        assertFalse(next.isLearned)
    }

    @Test
    fun firstHardIsOneDayWithLowerEase() {
        val next = SRSService.schedule(SRSState.DEFAULT, SRSGrade.HARD, now)
        assertEquals(2.36, next.easeFactor, 1e-9)
        assertEquals(1.0, next.intervalDays, 1e-9)
        assertEquals(1, next.repetitions)
        assertFalse(next.isLearned)
    }

    @Test
    fun againResetsToTenMinutes() {
        val started = SRSService.schedule(SRSState.DEFAULT, SRSGrade.GOOD, now)
        val next = SRSService.schedule(started, SRSGrade.AGAIN, now)
        assertEquals(2.5 - 0.54, next.easeFactor, 1e-9)
        assertEquals(0.0, next.intervalDays, 1e-9)
        assertEquals(0, next.repetitions)
        assertFalse(next.isLearned)
        assertEquals(Date(now.time + 10 * 60 * 1000L), next.nextReviewDate)
    }

    @Test
    fun secondGoodMarksLearnedWithSixDayInterval() {
        val first = SRSService.schedule(SRSState.DEFAULT, SRSGrade.GOOD, now)
        val second = SRSService.schedule(first, SRSGrade.GOOD, now)
        assertEquals(6.0, second.intervalDays, 1e-9)
        assertEquals(2, second.repetitions)
        assertTrue(second.isLearned)
        assertEquals(daysLater(now, 6), second.nextReviewDate)
    }

    @Test
    fun laterGoodMultipliesByEase() {
        val first = SRSService.schedule(SRSState.DEFAULT, SRSGrade.GOOD, now)
        val second = SRSService.schedule(first, SRSGrade.GOOD, now)
        val third = SRSService.schedule(second, SRSGrade.GOOD, now)
        assertEquals(15.0, third.intervalDays, 1e-9)
        assertEquals(3, third.repetitions)
        assertTrue(third.isLearned)
    }

    @Test
    fun manualMasteredPersistsLearnedFlag() {
        val marked = SRSService.markManuallyLearned(SRSState.DEFAULT, now)
        assertTrue(marked.isLearned)
        assertEquals(6.0, marked.intervalDays, 1e-9)
        assertEquals(2, marked.repetitions)
        assertEquals(2.5, marked.easeFactor, 1e-9)
        assertEquals(now, marked.lastReviewed)
        assertEquals(daysLater(now, 6), marked.nextReviewDate)

        val mid = SRSState(easeFactor = 2.8, intervalDays = 10.0, repetitions = 5)
        val kept = SRSService.markManuallyLearned(mid, now)
        assertTrue(kept.isLearned)
        assertEquals(10.0, kept.intervalDays, 1e-9)
        assertEquals(5, kept.repetitions)
        assertEquals(2.8, kept.easeFactor, 1e-9)
    }

    @Test
    fun clearManualLearnedReturnsNewCard() {
        val cleared = SRSService.clearManualLearned()
        assertEquals(SRSState.DEFAULT, cleared)
        assertFalse(cleared.isLearned)
    }

    @Test
    fun intervalHintsUseScheduledResult() {
        val fresh = SRSState.DEFAULT
        assertEquals("<10m", SRSService.formatIntervalHint(fresh, SRSGrade.AGAIN, now))
        assertEquals("1d", SRSService.formatIntervalHint(fresh, SRSGrade.HARD, now))
        assertEquals("1d", SRSService.formatIntervalHint(fresh, SRSGrade.GOOD, now))
        assertEquals("4d", SRSService.formatIntervalHint(fresh, SRSGrade.EASY, now))

        val first = SRSService.schedule(fresh, SRSGrade.GOOD, now)
        val second = SRSService.schedule(first, SRSGrade.GOOD, now)
        assertEquals("15d", SRSService.formatIntervalHint(second, SRSGrade.GOOD, now))
        assertEquals("7d", SRSService.formatIntervalHint(second, SRSGrade.HARD, now))
        assertEquals("20d", SRSService.formatIntervalHint(second, SRSGrade.EASY, now))
    }

    @Test
    fun easeNeverDropsBelowMinimum() {
        var state = SRSState.DEFAULT
        repeat(20) {
            state = SRSService.schedule(state, SRSGrade.AGAIN, now)
        }
        assertEquals(SRSService.MIN_EASE, state.easeFactor, 1e-9)
    }

    private fun daysLater(from: Date, days: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = from
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.time
    }
}
