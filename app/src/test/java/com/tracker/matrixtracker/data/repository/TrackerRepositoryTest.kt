package com.tracker.matrixtracker.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackerRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TrackerRepository

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = TrackerRepositoryImpl(
            boardDao = database.boardDao(),
            taskDao = database.taskDao(),
            cellEntryDao = database.cellEntryDao()
        )
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun toggleCellStatusCycle() = runBlocking {
        val boardId = repository.saveBoard(Board(title = "Board", cadence = BoardCadence.DAILY))
        val taskId = repository.saveTask(Task(boardId = boardId, title = "Task", sortOrder = 0))
        val periodKey = "2026-10-22"

        repository.toggleCellStatus(taskId, periodKey, CellStatus.EMPTY)
        var entries = repository.getCellEntries(listOf(taskId), listOf(periodKey)).first()
        assertEquals(1, entries.size)
        assertEquals(CellStatus.DONE, entries[0].status)

        repository.toggleCellStatus(taskId, periodKey, CellStatus.DONE)
        entries = repository.getCellEntries(listOf(taskId), listOf(periodKey)).first()
        assertEquals(1, entries.size)
        assertEquals(CellStatus.FAILED, entries[0].status)

        repository.toggleCellStatus(taskId, periodKey, CellStatus.FAILED)
        entries = repository.getCellEntries(listOf(taskId), listOf(periodKey)).first()
        assertEquals(1, entries.size)
        assertEquals(CellStatus.SKIPPED, entries[0].status)

        repository.toggleCellStatus(taskId, periodKey, CellStatus.SKIPPED)
        entries = repository.getCellEntries(listOf(taskId), listOf(periodKey)).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun updateTaskOrder() = runBlocking {
        val boardId = repository.saveBoard(Board(title = "Board", cadence = BoardCadence.DAILY))
        repository.saveTask(Task(boardId = boardId, title = "Task 1", sortOrder = 0))
        repository.saveTask(Task(boardId = boardId, title = "Task 2", sortOrder = 1))

        var tasks = repository.getTasksForBoard(boardId).first()
        assertEquals("Task 1", tasks[0].title)
        assertEquals("Task 2", tasks[1].title)

        val reordered = listOf(tasks[1], tasks[0])
        repository.updateTaskOrder(reordered)

        tasks = repository.getTasksForBoard(boardId).first()
        assertEquals("Task 2", tasks[0].title)
        assertEquals("Task 1", tasks[1].title)
    }
}
