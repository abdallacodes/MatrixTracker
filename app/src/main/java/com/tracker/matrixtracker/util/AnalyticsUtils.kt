package com.tracker.matrixtracker.util

import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.matrix.PeriodHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object AnalyticsUtils {

    fun calculateCurrentStreak(
        taskId: Long,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Int {
        if (periods.isEmpty()) return 0

        val currentIndex = periods.indexOfFirst { it.isCurrent }
        val startIndex = if (currentIndex >= 0) currentIndex else periods.lastIndex

        var streak = 0
        var isChecking = true

        for (i in startIndex downTo 0) {
            val periodKey = periods[i].key
            val status = cells[Pair(taskId, periodKey)]?.status ?: CellStatus.EMPTY

            if (i == startIndex) {
                when (status) {
                    CellStatus.DONE -> streak++
                    CellStatus.SKIPPED, CellStatus.EMPTY -> {
                        // Neutral for current day if not marked yet
                    }
                    CellStatus.FAILED -> {
                        isChecking = false
                    }
                }
            } else {
                when (status) {
                    CellStatus.DONE -> streak++
                    CellStatus.SKIPPED -> {
                        // Neutral, does not break streak
                    }
                    CellStatus.FAILED, CellStatus.EMPTY -> {
                        isChecking = false
                    }
                }
            }

            if (!isChecking) break
        }

        return streak
    }

    fun calculateBestDayOfWeek(
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Pair<String, Int>? {
        if (tasks.isEmpty() || periods.isEmpty()) return null

        val currentIndex = periods.indexOfFirst { it.isCurrent }
        val elapsed = if (currentIndex >= 0) periods.take(currentIndex + 1) else periods

        val weekdayOpportunities = mutableMapOf<java.time.DayOfWeek, Int>()
        val weekdayCompletions = mutableMapOf<java.time.DayOfWeek, Int>()

        elapsed.forEach { period ->
            runCatching {
                val date = LocalDate.parse(period.key, DateTimeFormatter.ISO_LOCAL_DATE)
                val dayOfWeek = date.dayOfWeek

                val currentOpp = weekdayOpportunities.getOrDefault(dayOfWeek, 0)
                weekdayOpportunities[dayOfWeek] = currentOpp + tasks.size

                val doneCount = tasks.count { task ->
                    cells[Pair(task.id, period.key)]?.status == CellStatus.DONE
                }
                val currentComp = weekdayCompletions.getOrDefault(dayOfWeek, 0)
                weekdayCompletions[dayOfWeek] = currentComp + doneCount
            }
        }

        if (weekdayOpportunities.isEmpty()) return null

        var bestDay: java.time.DayOfWeek? = null
        var bestPercent = -1

        weekdayOpportunities.forEach { (day, totalOpp) ->
            if (totalOpp > 0) {
                val done = weekdayCompletions.getOrDefault(day, 0)
                val percent = ((done.toFloat() / totalOpp.toFloat()) * 100f).toInt()
                if ((percent > bestPercent) && (done > 0)) {
                    bestPercent = percent
                    bestDay = day
                }
            }
        }

        val day = bestDay
        return if (day != null) {
            val dayName = day.getDisplayName(TextStyle.FULL, Locale.US)
            Pair(dayName, bestPercent)
        } else null
    }

    fun calculateOverallConsistency(
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Int {
        if (tasks.isEmpty() || periods.isEmpty()) return 0

        val currentIndex = periods.indexOfFirst { it.isCurrent }
        val elapsed = if (currentIndex >= 0) periods.take(currentIndex + 1) else periods

        val totalOpportunities = (tasks.size * elapsed.size).toFloat()
        if (totalOpportunities == 0f) return 0

        val doneCount = tasks.sumOf { task ->
            elapsed.count { period ->
                cells[Pair(task.id, period.key)]?.status == CellStatus.DONE
            }
        }

        return ((doneCount.toFloat() / totalOpportunities) * 100f).toInt()
    }
}
