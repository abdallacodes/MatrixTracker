package com.tracker.matrixtracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tracker.matrixtracker.data.local.entity.BoardEntity
import com.tracker.matrixtracker.data.local.entity.CellEntryEntity
import com.tracker.matrixtracker.data.local.entity.TaskEntity
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertBoardAndTasks() = runBlocking {
        val board = BoardEntity(title = "Daily Habits", cadence = BoardCadence.DAILY)
        val boardId = database.boardDao().insertBoard(board)

        val task1 = TaskEntity(boardId = boardId, title = "Task 1", sortOrder = 0)
        val task2 = TaskEntity(boardId = boardId, title = "Task 2", sortOrder = 1)
        database.taskDao().insertTask(task1)
        database.taskDao().insertTask(task2)

        val fetchedTasks = database.taskDao().getTasksByBoardId(boardId).first()
        assertEquals(2, fetchedTasks.size)
        assertEquals("Task 1", fetchedTasks[0].title)
        assertEquals("Task 2", fetchedTasks[1].title)
    }

    @Test
    fun foreignKeyCascadeDeleteBoard() = runBlocking {
        val board = BoardEntity(title = "Board", cadence = BoardCadence.DAILY)
        val boardId = database.boardDao().insertBoard(board)

        val task = TaskEntity(boardId = boardId, title = "Task", sortOrder = 0)
        val taskId = database.taskDao().insertTask(task)

        val entry = CellEntryEntity(taskId = taskId, periodKey = "2026-10-22", status = CellStatus.DONE)
        database.cellEntryDao().upsertCellEntry(entry)

        val fetchedBoard = database.boardDao().getBoardById(boardId)
        assertNotNull(fetchedBoard)
        database.boardDao().deleteBoard(fetchedBoard!!)

        val tasks = database.taskDao().getTasksByBoardId(boardId).first()
        assertTrue(tasks.isEmpty())

        val entries = database.cellEntryDao().getCellEntriesForTasksAndPeriods(listOf(taskId), listOf("2026-10-22")).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun foreignKeyCascadeDeleteTask() = runBlocking {
        val board = BoardEntity(title = "Board", cadence = BoardCadence.DAILY)
        val boardId = database.boardDao().insertBoard(board)

        val task = TaskEntity(boardId = boardId, title = "Task", sortOrder = 0)
        val taskId = database.taskDao().insertTask(task)

        val entry = CellEntryEntity(taskId = taskId, periodKey = "2026-10-22", status = CellStatus.DONE)
        database.cellEntryDao().upsertCellEntry(entry)

        val fetchedTask = database.taskDao().getTaskById(taskId)
        assertNotNull(fetchedTask)
        database.taskDao().deleteTask(fetchedTask!!)

        val entries = database.cellEntryDao().getCellEntriesForTasksAndPeriods(listOf(taskId), listOf("2026-10-22")).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun upsertCellEntryUniqueConstraint() = runBlocking {
        val board = BoardEntity(title = "Board", cadence = BoardCadence.DAILY)
        val boardId = database.boardDao().insertBoard(board)

        val task = TaskEntity(boardId = boardId, title = "Task", sortOrder = 0)
        val taskId = database.taskDao().insertTask(task)

        val periodKey = "2026-10-22"
        val entry1 = CellEntryEntity(taskId = taskId, periodKey = periodKey, status = CellStatus.DONE, note = "First")
        database.cellEntryDao().upsertCellEntry(entry1)

        val fetched1 = database.cellEntryDao().getCellEntriesForTasksAndPeriods(listOf(taskId), listOf(periodKey)).first()
        assertEquals(1, fetched1.size)
        assertEquals(CellStatus.DONE, fetched1[0].status)
        assertEquals("First", fetched1[0].note)

        val entry2 = CellEntryEntity(taskId = taskId, periodKey = periodKey, status = CellStatus.FAILED, note = "Updated")
        database.cellEntryDao().upsertCellEntry(entry2)

        val fetched2 = database.cellEntryDao().getCellEntriesForTasksAndPeriods(listOf(taskId), listOf(periodKey)).first()
        assertEquals(1, fetched2.size)
        assertEquals(CellStatus.FAILED, fetched2[0].status)
        assertEquals("Updated", fetched2[0].note)
    }
}
