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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class PdfExportUtilsTest {

    @Test
    fun createBoardPdfDocument_rendersSinglePagePdf() {
        val board = Board(id = 1, title = "Daily Habits", cadence = BoardCadence.DAILY)
        val task = Task(id = 10, boardId = 1, title = "Exercise 30 mins", sortOrder = 0)
        val periods = listOf(
            PeriodHeader(key = "2026-10-01", label = "Thu 1"),
            PeriodHeader(key = "2026-10-02", label = "Fri 2")
        )
        val cells = mapOf(
            Pair(10L, "2026-10-01") to CellEntry(id = 1, taskId = 10, periodKey = "2026-10-01", status = CellStatus.DONE)
        )

        val pdfDoc = PdfExportUtils.createBoardPdfDocument(
            board = board,
            tasks = listOf(task),
            periods = periods,
            cells = cells,
            taskCompletionPercentage = mapOf(10L to 50f),
            periodCompletionPercentage = mapOf("2026-10-01" to 100f, "2026-10-02" to 0f),
            overallCompletionPercentage = 50f,
            periodTitle = "October 2026"
        )

        assertNotNull(pdfDoc)
        pdfDoc.close()
    }

    @Test
    fun exportPdfToBytes_generatesValidPdfHeader() {
        val board = Board(id = 1, title = "Daily Habits", cadence = BoardCadence.DAILY)
        val task = Task(id = 10, boardId = 1, title = "Read 15 Pages", sortOrder = 0)
        val periods = listOf(PeriodHeader(key = "2026-10-01", label = "Thu 1"))
        val cells = emptyMap<Pair<Long, String>, CellEntry>()

        val pdfBytes = PdfExportUtils.exportPdfToBytes(
            board = board,
            tasks = listOf(task),
            periods = periods,
            cells = cells,
            taskCompletionPercentage = mapOf(10L to 0f),
            periodCompletionPercentage = mapOf("2026-10-01" to 0f),
            overallCompletionPercentage = 0f,
            periodTitle = "October 2026"
        )

        assertTrue(pdfBytes.isNotEmpty())
        val pdfHeader = String(pdfBytes.take(4).toByteArray())
        assertEquals("%PDF", pdfHeader)
    }
}
