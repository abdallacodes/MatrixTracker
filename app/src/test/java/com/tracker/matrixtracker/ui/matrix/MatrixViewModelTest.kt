package com.tracker.matrixtracker.ui.matrix

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.SettingsRepository
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MatrixViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var database: AppDatabase
    private lateinit var viewModel: MatrixViewModel

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = TrackerRepositoryImpl(
            boardDao = database.boardDao(),
            taskDao = database.taskDao(),
            cellEntryDao = database.cellEntryDao()
        )

        val settingsRepository = SettingsRepository(
            ApplicationProvider.getApplicationContext()
        )

        viewModel = MatrixViewModel(repository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun initialState_seedsDefaultBoardWhenDbEmpty() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val state = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        assertNotNull(state.currentBoard)
        assertEquals("Daily Habits", state.currentBoard?.title)
        assertEquals(4, state.tasks.size)

        job.cancel()
    }

    @Test
    fun toggleCell_updatesStateAndCalculatesProgress() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val firstTask = initialState.tasks.first()
        val firstPeriod = initialState.periods.first()

        viewModel.toggleCell(firstTask.id, firstPeriod.key)

        val updatedState = viewModel.uiState.first {
            it.cells[Pair(firstTask.id, firstPeriod.key)]?.status == CellStatus.DONE
        }

        assertEquals(CellStatus.DONE, updatedState.cells[Pair(firstTask.id, firstPeriod.key)]?.status)
        assertTrue(updatedState.taskCompletionPercentage[firstTask.id]!! > 0f)
        assertTrue(updatedState.periodCompletionPercentage[firstPeriod.key]!! > 0f)

        job.cancel()
    }

    @Test
    fun periodNavigation_updatesSelectedYearMonth() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val state1 = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val initialYM = state1.selectedYearMonth

        viewModel.nextPeriod()
        val stateNext = viewModel.uiState.first { it.selectedYearMonth == initialYM.plusMonths(1) }
        assertEquals(initialYM.plusMonths(1), stateNext.selectedYearMonth)

        viewModel.previousPeriod()
        val statePrev = viewModel.uiState.first { it.selectedYearMonth == initialYM }
        assertEquals(initialYM, statePrev.selectedYearMonth)

        job.cancel()
    }

    @Test
    fun taskManagement_addNewTaskDeleteTaskAndReorder() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val state1 = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val initialCount = state1.tasks.size

        viewModel.addNewTask("New Habit")
        val stateWithNewTask = viewModel.uiState.first { it.tasks.size == initialCount + 1 }
        assertEquals("New Habit", stateWithNewTask.tasks.last().title)

        viewModel.reorderTasks(fromIndex = 0, toIndex = 1)
        val stateReordered = viewModel.uiState.first { it.tasks[1].title == state1.tasks[0].title }
        assertEquals(state1.tasks[0].title, stateReordered.tasks[1].title)

        val taskToDelete = stateWithNewTask.tasks.last()
        viewModel.deleteTask(taskToDelete)
        val stateAfterDelete = viewModel.uiState.first { it.tasks.none { t -> t.id == taskToDelete.id } }
        assertFalse(stateAfterDelete.tasks.any { it.id == taskToDelete.id })

        job.cancel()
    }

    @Test
    fun taskManagement_renameTask() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val firstTask = initialState.tasks.first()

        viewModel.renameTask(firstTask, "Renamed Habit")
        val stateAfterRename = viewModel.uiState.first { state ->
            state.tasks.any { it.id == firstTask.id && it.title == "Renamed Habit" }
        }
        assertEquals("Renamed Habit", stateAfterRename.tasks.find { it.id == firstTask.id }?.title)

        job.cancel()
    }

    @Test
    fun boardManagement_createUpdateAndDeleteBoard() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val initialBoardCount = initialState.availableBoards.size

        viewModel.createBoard("Weekly Workouts", BoardCadence.WEEKLY)
        val stateAfterCreate = viewModel.uiState.first { it.availableBoards.size == initialBoardCount + 1 }
        val createdBoard = stateAfterCreate.availableBoards.find { it.title == "Weekly Workouts" }!!
        assertEquals(BoardCadence.WEEKLY, createdBoard.cadence)
        assertEquals(createdBoard.id, stateAfterCreate.currentBoard?.id)

        viewModel.updateBoard(createdBoard, "Fitness Routine")
        val stateAfterUpdate = viewModel.uiState.first { it.currentBoard?.title == "Fitness Routine" }
        assertEquals("Fitness Routine", stateAfterUpdate.currentBoard?.title)

        viewModel.deleteBoard(createdBoard)
        val stateAfterDelete = viewModel.uiState.first { it.availableBoards.none { b -> b.id == createdBoard.id } }
        assertFalse(stateAfterDelete.availableBoards.any { it.id == createdBoard.id })

        job.cancel()
    }

    @Test
    fun batchCellOperations_markAllHabitsAndRecentDays() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val periodKey = initialState.periods.first().key

        viewModel.markAllHabitsForPeriod(periodKey, CellStatus.DONE)
        val stateAfterMarkAll = viewModel.uiState.first { state ->
            state.tasks.all { task -> state.cells[Pair(task.id, periodKey)]?.status == CellStatus.DONE }
        }
        assertTrue(stateAfterMarkAll.tasks.all { task ->
            stateAfterMarkAll.cells[Pair(task.id, periodKey)]?.status == CellStatus.DONE
        })

        viewModel.clearPeriod(periodKey)
        val stateAfterClear = viewModel.uiState.first { state ->
            state.tasks.all { task -> state.cells[Pair(task.id, periodKey)] == null }
        }
        assertTrue(stateAfterClear.tasks.all { task ->
            stateAfterClear.cells[Pair(task.id, periodKey)] == null
        })

        val firstTask = initialState.tasks.first()
        viewModel.markTaskRecentDays(firstTask.id, 7, CellStatus.DONE)
        val stateAfterRecent = viewModel.uiState.first { state ->
            state.cells.count { it.key.first == firstTask.id && it.value.status == CellStatus.DONE } >= 1
        }
        assertTrue(stateAfterRecent.cells.any { it.key.first == firstTask.id && it.value.status == CellStatus.DONE })

        job.cancel()
    }

    @Test
    fun cellNoteOperations_setAndClearCellNote() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val firstTask = initialState.tasks.first()
        val periodKey = initialState.periods.first().key

        viewModel.setCellNote(firstTask.id, periodKey, "Drink 2L Water Before 12PM")
        val stateWithNote = viewModel.uiState.first { state ->
            state.cells[Pair(firstTask.id, periodKey)]?.note == "Drink 2L Water Before 12PM"
        }
        assertEquals("Drink 2L Water Before 12PM", stateWithNote.cells[Pair(firstTask.id, periodKey)]?.note)

        viewModel.deleteCellNote(firstTask.id, periodKey)
        val stateAfterDeleteNote = viewModel.uiState.first { state ->
            state.cells[Pair(firstTask.id, periodKey)]?.note.isNullOrBlank()
        }
        assertTrue(stateAfterDeleteNote.cells[Pair(firstTask.id, periodKey)]?.note.isNullOrBlank())

        job.cancel()
    }

    @Test
    fun exportAndImportDataFromJson_preservesDataIntegrity() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        val firstTask = initialState.tasks.first()
        val periodKey = initialState.periods.first().key

        viewModel.toggleCell(firstTask.id, periodKey)
        viewModel.uiState.first { it.cells[Pair(firstTask.id, periodKey)]?.status == CellStatus.DONE }

        val outputStream = java.io.ByteArrayOutputStream()
        val exportResult = viewModel.exportDataToJson(outputStream)
        assertTrue(exportResult.isSuccess)
        val jsonBytes = outputStream.toByteArray()
        assertTrue(jsonBytes.isNotEmpty())

        val inputStream = java.io.ByteArrayInputStream(jsonBytes)
        val importResult = viewModel.importDataFromJson(inputStream)
        assertTrue(importResult.isSuccess)

        job.cancel()
    }
}
