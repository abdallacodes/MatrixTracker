package com.tracker.matrixtracker.util

import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.ui.matrix.PeriodHeader
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

object PeriodUtils {

    private val dayOfWeekFormatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
    private val monthFormatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    private val monthDayFormatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    // --- Daily ---

    fun getDailyPeriodKeys(yearMonth: YearMonth): List<String> {
        return (1..yearMonth.lengthOfMonth()).map { day ->
            LocalDate.of(yearMonth.year, yearMonth.monthValue, day).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    fun formatDailyHeader(date: LocalDate, today: LocalDate = LocalDate.now()): PeriodHeader {
        val key = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val label = date.format(dayOfWeekFormatter)
        return PeriodHeader(
            key = key,
            label = label,
            isCurrent = date == today,
        )
    }

    fun getTodayKey(today: LocalDate = LocalDate.now()): String {
        return today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // --- Weekly ---

    fun getWeeklyPeriodKeys(year: Int): List<String> {
        val maxWeeks = getMaxWeeksInYear(year)
        return (1..maxWeeks).map { week ->
            String.format(Locale.US, "%d-W%02d", year, week)
        }
    }

    fun getMaxWeeksInYear(year: Int): Int {
        val dec28 = LocalDate.of(year, 12, 28)
        return dec28[WeekFields.ISO.weekOfWeekBasedYear()]
    }

    fun formatWeeklyHeader(year: Int, weekNumber: Int, today: LocalDate = LocalDate.now()): PeriodHeader {
        val key = String.format(Locale.US, "%d-W%02d", year, weekNumber)
        val label = "W$weekNumber"

        val firstDayOfWeek = LocalDate.of(year, 6, 1)
            .with(WeekFields.ISO.weekOfWeekBasedYear(), weekNumber.toLong())
            .with(WeekFields.ISO.dayOfWeek(), 1)

        val lastDayOfWeek = firstDayOfWeek.plusDays(6)

        val subLabel = "${firstDayOfWeek.format(monthDayFormatter)} - ${lastDayOfWeek.format(monthDayFormatter)}"
        val isCurrent = key == getCurrentWeekKey(today)

        return PeriodHeader(
            key = key,
            label = label,
            subLabel = subLabel,
            isCurrent = isCurrent
        )
    }

    fun getCurrentWeekKey(today: LocalDate = LocalDate.now()): String {
        val weekYear = today[WeekFields.ISO.weekBasedYear()]
        val weekNumber = today[WeekFields.ISO.weekOfWeekBasedYear()]
        return String.format(Locale.US, "%d-W%02d", weekYear, weekNumber)
    }

    // --- Monthly ---

    fun getMonthlyPeriodKeys(year: Int): List<String> {
        return (1..12).map { month ->
            String.format(Locale.US, "%d-%02d", year, month)
        }
    }

    fun formatMonthlyHeader(yearMonth: YearMonth, today: LocalDate = LocalDate.now()): PeriodHeader {
        val key = yearMonth.toString()
        val label = yearMonth.format(monthFormatter)
        val isCurrent = yearMonth == YearMonth.from(today)
        return PeriodHeader(
            key = key,
            label = label,
            isCurrent = isCurrent
        )
    }

    fun getCurrentMonthKey(today: LocalDate = LocalDate.now()): String {
        return YearMonth.from(today).toString()
    }

    // --- Unified Header Generator ---

    fun getPeriodHeaders(
        cadence: BoardCadence,
        selectedYearMonth: YearMonth,
        selectedYear: Int,
        today: LocalDate = LocalDate.now()
    ): List<PeriodHeader> {
        return when (cadence) {
            BoardCadence.DAILY -> {
                (1..selectedYearMonth.lengthOfMonth()).map { day ->
                    val date = LocalDate.of(selectedYearMonth.year, selectedYearMonth.monthValue, day)
                    formatDailyHeader(date, today)
                }
            }
            BoardCadence.WEEKLY -> {
                val maxWeeks = getMaxWeeksInYear(selectedYear)
                (1..maxWeeks).map { week ->
                    formatWeeklyHeader(selectedYear, week, today)
                }
            }
            BoardCadence.MONTHLY -> {
                (1..12).map { month ->
                    val ym = YearMonth.of(selectedYear, month)
                    formatMonthlyHeader(ym, today)
                }
            }
        }
    }
}
