package com.tracker.matrixtracker.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.matrix.PeriodHeader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatrixExportUtilsTest {

    @Test
    fun csvGeneration_formatsCorrectly() {
        val board = Board(id = 1, title = "Daily Habits", cadence = BoardCadence.DAILY)
        val task1 = Task(id = 10, boardId = 1, title = "Exercise", sortOrder = 0)
        val task2 = Task(id = 20, boardId = 1, title = "Read", sortOrder = 1)

        val periods = listOf(
            PeriodHeader(key = "2026-10-01", label = "Thu 1"),
            PeriodHeader(key = "2026-10-02", label = "Fri 2")
        )

        val cells = mapOf(
            Pair(10L, "2026-10-01") to CellEntry(id = 1, taskId = 10, periodKey = "2026-10-01", status = CellStatus.DONE),
            Pair(10L, "2026-10-02") to CellEntry(id = 2, taskId = 10, periodKey = "2026-10-02", status = CellStatus.FAILED),
            Pair(20L, "2026-10-01") to CellEntry(id = 3, taskId = 20, periodKey = "2026-10-01", status = CellStatus.DONE)
        )

        val csv = MatrixExportUtils.generateCsvForBoard(board, listOf(task1, task2), periods, cells)

        assertTrue(csv.contains("\"Task / Habit\",\"Thu 1\",\"Fri 2\",\"Completion %\""))
        assertTrue(csv.contains("\"Exercise\",\"V\",\"X\",\"50%\""))
        assertTrue(csv.contains("\"Read\",\"V\",\"\",\"50%\""))
        assertTrue(csv.contains("\"Period Completion %\",\"100%\",\"0%\",\"\""))
    }

    @Test
    fun jsonBackupSerializationAndParsing() {
        val board = Board(id = 1, title = "Daily Habits", cadence = BoardCadence.DAILY)
        val task = Task(id = 10, boardId = 1, title = "Exercise", sortOrder = 0)
        val entry = CellEntry(id = 100, taskId = 10, periodKey = "2026-10-01", status = CellStatus.DONE, note = "Great workout!")

        val json = MatrixExportUtils.exportFullBackupJson(listOf(board), listOf(task), listOf(entry))
        assertTrue(json.contains("Daily Habits"))
        assertTrue(json.contains("Great workout!"))

        val bundle = MatrixExportUtils.parseBackupJson(json)
        assertNotNull(bundle)
        assertEquals(1, bundle!!.boards.size)
        assertEquals("Daily Habits", bundle.boards[0].title)
        assertEquals(1, bundle.tasks.size)
        assertEquals("Exercise", bundle.tasks[0].title)
        assertEquals(1, bundle.entries.size)
        assertEquals(CellStatus.DONE, bundle.entries[0].status)
        assertEquals("Great workout!", bundle.entries[0].note)
    }
}
