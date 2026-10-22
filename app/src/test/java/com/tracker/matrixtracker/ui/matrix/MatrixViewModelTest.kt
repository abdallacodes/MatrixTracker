package com.tracker.matrixtracker.ui.matrix

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.domain.model.CellStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var viewModel: MatrixViewModel

    @Before
    fun setUp() {
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

        viewModel = MatrixViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun initialState_seedsDefaultBoardWhenDbEmpty() = runBlocking {
        val job = launch(testDispatcher) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.first { !it.isLoading && it.tasks.isNotEmpty() }
        assertNotNull(state.currentBoard)
        assertEquals("Daily Habits", state.currentBoard?.title)
        assertEquals(4, state.tasks.size)

        job.cancel()
    }

    @Test
    fun toggleCell_updatesStateAndCalculatesProgress() = runBlocking {
        val job = launch(testDispatcher) { viewModel.uiState.collect {} }

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
    fun periodNavigation_updatesSelectedYearMonth() = runBlocking {
        val job = launch(testDispatcher) { viewModel.uiState.collect {} }

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
    fun taskManagement_addNewTaskDeleteTaskAndReorder() = runBlocking {
        val job = launch(testDispatcher) { viewModel.uiState.collect {} }

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
}
