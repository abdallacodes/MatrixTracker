package com.tracker.matrixtracker.util

import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.matrix.PeriodHeader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AnalyticsUtilsTest {

    @Test
    fun calculateCurrentStreak_handlesDoneSkippedAndBreaks() {
        val taskId = 10L
        val periods = listOf(
            PeriodHeader(key = "2026-10-18", label = "Sun 18"),
            PeriodHeader(key = "2026-10-19", label = "Mon 19"),
            PeriodHeader(key = "2026-10-20", label = "Tue 20"),
            PeriodHeader(key = "2026-10-21", label = "Wed 21"),
            PeriodHeader(key = "2026-10-22", label = "Thu 22", isCurrent = true)
        )

        val cells = mapOf(
            Pair(taskId, "2026-10-19") to CellEntry(id = 1, taskId = taskId, periodKey = "2026-10-19", status = CellStatus.FAILED),
            Pair(taskId, "2026-10-20") to CellEntry(id = 2, taskId = taskId, periodKey = "2026-10-20", status = CellStatus.DONE),
            Pair(taskId, "2026-10-21") to CellEntry(id = 3, taskId = taskId, periodKey = "2026-10-21", status = CellStatus.SKIPPED),
            Pair(taskId, "2026-10-22") to CellEntry(id = 4, taskId = taskId, periodKey = "2026-10-22", status = CellStatus.DONE)
        )

        val streak = AnalyticsUtils.calculateCurrentStreak(taskId, periods, cells)
        assertEquals(2, streak)
    }

    @Test
    fun calculateCurrentStreak_unmarkedTodayDoesNotBreakYesterdayStreak() {
        val taskId = 10L
        val periods = listOf(
            PeriodHeader(key = "2026-10-20", label = "Tue 20"),
            PeriodHeader(key = "2026-10-21", label = "Wed 21"),
            PeriodHeader(key = "2026-10-22", label = "Thu 22", isCurrent = true)
        )

        val cells = mapOf(
            Pair(taskId, "2026-10-20") to CellEntry(id = 1, taskId = taskId, periodKey = "2026-10-20", status = CellStatus.DONE),
            Pair(taskId, "2026-10-21") to CellEntry(id = 2, taskId = taskId, periodKey = "2026-10-21", status = CellStatus.DONE)
        )

        val streak = AnalyticsUtils.calculateCurrentStreak(taskId, periods, cells)
        assertEquals(2, streak)
    }

    @Test
    fun calculateBestDayOfWeek_identifiesTopWeekday() {
        val task1 = Task(id = 1, boardId = 1, title = "Task 1", sortOrder = 0)
        val task2 = Task(id = 2, boardId = 1, title = "Task 2", sortOrder = 1)
        val tasks = listOf(task1, task2)

        val periods = listOf(
            PeriodHeader(key = "2026-10-22", label = "Thu 22"),
            PeriodHeader(key = "2026-10-23", label = "Fri 23", isCurrent = true)
        )

        val cells = mapOf(
            Pair(1L, "2026-10-22") to CellEntry(id = 1, taskId = 1, periodKey = "2026-10-22", status = CellStatus.DONE),
            Pair(2L, "2026-10-22") to CellEntry(id = 2, taskId = 2, periodKey = "2026-10-22", status = CellStatus.DONE),
            Pair(1L, "2026-10-23") to CellEntry(id = 3, taskId = 1, periodKey = "2026-10-23", status = CellStatus.DONE),
            Pair(2L, "2026-10-23") to CellEntry(id = 4, taskId = 2, periodKey = "2026-10-23", status = CellStatus.FAILED)
        )

        val bestDay = AnalyticsUtils.calculateBestDayOfWeek(tasks, periods, cells)
        assertNotNull(bestDay)
        assertEquals("Thursday", bestDay!!.first)
        assertEquals(100, bestDay.second)
    }

    @Test
    fun calculateOverallConsistency_calculatesPercentageCorrectly() {
        val task1 = Task(id = 1, boardId = 1, title = "Task 1", sortOrder = 0)
        val task2 = Task(id = 2, boardId = 1, title = "Task 2", sortOrder = 1)
        val tasks = listOf(task1, task2)

        val periods = listOf(
            PeriodHeader(key = "2026-10-21", label = "Wed 21"),
            PeriodHeader(key = "2026-10-22", label = "Thu 22", isCurrent = true)
        )

        val cells = mapOf(
            Pair(1L, "2026-10-21") to CellEntry(id = 1, taskId = 1, periodKey = "2026-10-21", status = CellStatus.DONE),
            Pair(2L, "2026-10-21") to CellEntry(id = 2, taskId = 2, periodKey = "2026-10-21", status = CellStatus.DONE),
            Pair(1L, "2026-10-22") to CellEntry(id = 3, taskId = 1, periodKey = "2026-10-22", status = CellStatus.DONE)
        )

        val consistency = AnalyticsUtils.calculateOverallConsistency(tasks, periods, cells)
        assertEquals(75, consistency)
    }
}
