package com.tracker.matrixtracker.util

import com.tracker.matrixtracker.domain.model.BoardCadence
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PeriodUtilsTest {

    @Test
    fun dailyPeriodKeysLengthAndFormat() {
        val oct2026 = YearMonth.of(2026, 10)
        val octKeys = PeriodUtils.getDailyPeriodKeys(oct2026)
        assertEquals(31, octKeys.size)
        assertEquals("2026-10-01", octKeys.first())
        assertEquals("2026-10-31", octKeys.last())

        val feb2025 = YearMonth.of(2025, 2)
        val feb2025Keys = PeriodUtils.getDailyPeriodKeys(feb2025)
        assertEquals(28, feb2025Keys.size)

        val feb2024Leap = YearMonth.of(2024, 2)
        val feb2024Keys = PeriodUtils.getDailyPeriodKeys(feb2024Leap)
        assertEquals(29, feb2024Keys.size)
    }

    @Test
    fun dailyHeaderFormatAndIsCurrent() {
        val today = LocalDate.of(2026, 10, 22)
        val headerToday = PeriodUtils.formatDailyHeader(today, today)
        assertEquals("2026-10-22", headerToday.key)
        assertEquals("Thu 22", headerToday.label)
        assertTrue(headerToday.isCurrent)

        val otherDay = LocalDate.of(2026, 10, 23)
        val headerOther = PeriodUtils.formatDailyHeader(otherDay, today)
        assertFalse(headerOther.isCurrent)
    }

    @Test
    fun weeklyPeriodKeysAndMaxWeeks() {
        val weeks2026 = PeriodUtils.getWeeklyPeriodKeys(2026)
        assertEquals(53, weeks2026.size)
        assertEquals("2026-W01", weeks2026.first())
        assertEquals("2026-W53", weeks2026.last())

        val weeks2025 = PeriodUtils.getWeeklyPeriodKeys(2025)
        assertEquals(52, weeks2025.size)
    }

    @Test
    fun weeklyHeaderFormat() {
        val today = LocalDate.of(2026, 10, 22)
        val header = PeriodUtils.formatWeeklyHeader(2026, 42, today)
        assertEquals("2026-W42", header.key)
        assertEquals("W42", header.label)
        assertTrue(header.subLabel!!.contains("-"))
    }

    @Test
    fun monthlyPeriodKeysAndHeaderFormat() {
        val monthKeys = PeriodUtils.getMonthlyPeriodKeys(2026)
        assertEquals(12, monthKeys.size)
        assertEquals("2026-01", monthKeys.first())
        assertEquals("2026-12", monthKeys.last())

        val today = LocalDate.of(2026, 10, 22)
        val ymOct = YearMonth.of(2026, 10)
        val headerOct = PeriodUtils.formatMonthlyHeader(ymOct, today)
        assertEquals("2026-10", headerOct.key)
        assertEquals("Oct", headerOct.label)
        assertTrue(headerOct.isCurrent)
    }

    @Test
    fun unifiedPeriodHeadersGeneration() {
        val headersDaily = PeriodUtils.getPeriodHeaders(
            cadence = BoardCadence.DAILY,
            selectedYearMonth = YearMonth.of(2026, 10),
            selectedYear = 2026
        )
        assertEquals(31, headersDaily.size)

        val headersWeekly = PeriodUtils.getPeriodHeaders(
            cadence = BoardCadence.WEEKLY,
            selectedYearMonth = YearMonth.of(2026, 10),
            selectedYear = 2026
        )
        assertEquals(53, headersWeekly.size)

        val headersMonthly = PeriodUtils.getPeriodHeaders(
            cadence = BoardCadence.MONTHLY,
            selectedYearMonth = YearMonth.of(2026, 10),
            selectedYear = 2026
        )
        assertEquals(12, headersMonthly.size)
    }
}
