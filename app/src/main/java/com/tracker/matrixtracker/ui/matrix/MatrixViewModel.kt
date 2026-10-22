package com.tracker.matrixtracker.ui.matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.domain.repository.TrackerRepository
import com.tracker.matrixtracker.util.PeriodUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class MatrixViewModel(
    private val repository: TrackerRepository
) : ViewModel() {

    private val _selectedBoardId = MutableStateFlow<Long?>(null)
    val selectedBoardId: StateFlow<Long?> = _selectedBoardId.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow<YearMonth>(YearMonth.now())
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int>(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    val uiState: StateFlow<MatrixUiState> = combine(
        repository.getBoards(),
        _selectedBoardId,
        _selectedYearMonth,
        _selectedYear
    ) { boards, selectedBoardId, selectedYearMonth, selectedYear ->
        if (boards.isEmpty()) {
            createDefaultBoardIfNone()
            return@combine flowOf(
                MatrixUiState(
                    isLoading = false,
                    selectedYearMonth = selectedYearMonth,
                    selectedYear = selectedYear
                )
            )
        }

        val currentBoard = boards.find { it.id == selectedBoardId } ?: boards.first()
        val periods = PeriodUtils.getPeriodHeaders(
            cadence = currentBoard.cadence,
            selectedYearMonth = selectedYearMonth,
            selectedYear = selectedYear
        )
        val periodKeys = periods.map { it.key }

        repository.getTasksForBoard(currentBoard.id).flatMapLatest { tasks ->
            val taskIds = tasks.map { it.id }
            repository.getCellEntries(taskIds, periodKeys).map { cellEntries ->
                val cellsMap = cellEntries.associateBy { Pair(it.taskId, it.periodKey) }
                val taskProgress = calculateTaskCompletion(tasks, periods, cellsMap)
                val periodProgress = calculatePeriodCompletion(tasks, periods, cellsMap)

                MatrixUiState(
                    isLoading = false,
                    currentBoard = currentBoard,
                    availableBoards = boards,
                    tasks = tasks,
                    periods = periods,
                    cells = cellsMap,
                    selectedYearMonth = selectedYearMonth,
                    selectedYear = selectedYear,
                    taskCompletionPercentage = taskProgress,
                    periodCompletionPercentage = periodProgress
                )
            }
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MatrixUiState(isLoading = true)
        )

    fun selectBoard(boardId: Long) {
        _selectedBoardId.value = boardId
    }

    fun createBoard(title: String, cadence: BoardCadence) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newBoardId = repository.saveBoard(
                Board(title = title.trim(), cadence = cadence)
            )
            _selectedBoardId.value = newBoardId
        }
    }

    fun updateBoard(board: Board, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.saveBoard(board.copy(title = newTitle.trim()))
        }
    }

    fun deleteBoard(board: Board) {
        viewModelScope.launch {
            repository.deleteBoard(board)
            val remaining = repository.getBoards().first()
            if (remaining.isNotEmpty()) {
                if (_selectedBoardId.value == board.id) {
                    _selectedBoardId.value = remaining.first().id
                }
            } else {
                _selectedBoardId.value = null
                createDefaultBoardIfNone()
            }
        }
    }

    fun createDefaultBoardIfNone() {
        viewModelScope.launch {
            val boards = repository.getBoards().first()
            if (boards.isEmpty()) {
                val newBoardId = repository.saveBoard(
                    Board(title = "Daily Habits", cadence = BoardCadence.DAILY)
                )
                repository.saveTask(Task(boardId = newBoardId, title = "Exercise 30 mins", sortOrder = 0))
                repository.saveTask(Task(boardId = newBoardId, title = "Drink 2L Water", sortOrder = 1))
                repository.saveTask(Task(boardId = newBoardId, title = "Read 15 Pages", sortOrder = 2))
                repository.saveTask(Task(boardId = newBoardId, title = "Meditate 10 mins", sortOrder = 3))
                _selectedBoardId.value = newBoardId
            }
        }
    }

    fun toggleCell(taskId: Long, periodKey: String) {
        viewModelScope.launch {
            val currentEntry = uiState.value.cells[Pair(taskId, periodKey)]
            val currentStatus = currentEntry?.status ?: CellStatus.EMPTY
            repository.toggleCellStatus(taskId, periodKey, currentStatus)
        }
    }

    fun setCellNote(taskId: Long, periodKey: String, note: String) {
        viewModelScope.launch {
            val currentEntry = uiState.value.cells[Pair(taskId, periodKey)]
            val currentStatus = currentEntry?.status ?: CellStatus.DONE
            repository.setCellStatus(taskId, periodKey, currentStatus, note.ifBlank { null })
        }
    }

    fun previousPeriod() {
        val board = uiState.value.currentBoard ?: return
        when (board.cadence) {
            BoardCadence.DAILY -> _selectedYearMonth.value = _selectedYearMonth.value.minusMonths(1)
            BoardCadence.WEEKLY, BoardCadence.MONTHLY -> _selectedYear.value = _selectedYear.value - 1
        }
    }

    fun nextPeriod() {
        val board = uiState.value.currentBoard ?: return
        when (board.cadence) {
            BoardCadence.DAILY -> _selectedYearMonth.value = _selectedYearMonth.value.plusMonths(1)
            BoardCadence.WEEKLY, BoardCadence.MONTHLY -> _selectedYear.value = _selectedYear.value + 1
        }
    }

    fun goToCurrentPeriod() {
        _selectedYearMonth.value = YearMonth.now()
        _selectedYear.value = LocalDate.now().year
    }

    fun addNewTask(title: String) {
        val board = uiState.value.currentBoard ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            val currentTasks = uiState.value.tasks
            val nextSortOrder = if (currentTasks.isEmpty()) 0 else currentTasks.maxOf { it.sortOrder } + 1
            repository.saveTask(
                Task(boardId = board.id, title = title.trim(), sortOrder = nextSortOrder)
            )
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        val currentTasks = uiState.value.tasks.toMutableList()
        if (fromIndex !in currentTasks.indices || toIndex !in currentTasks.indices) return
        val movedTask = currentTasks.removeAt(fromIndex)
        currentTasks.add(toIndex, movedTask)

        viewModelScope.launch {
            repository.updateTaskOrder(currentTasks)
        }
    }

    fun exportCurrentBoardAsCsv(context: android.content.Context) {
        val board = uiState.value.currentBoard ?: return
        val csvText = com.tracker.matrixtracker.util.MatrixExportUtils.generateCsvForBoard(
            board = board,
            tasks = uiState.value.tasks,
            periods = uiState.value.periods,
            cells = uiState.value.cells
        )
        val sanitizedTitle = board.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val filename = "${sanitizedTitle}_matrix.csv"
        com.tracker.matrixtracker.util.ShareUtils.shareTextAsFile(context, filename, csvText, "text/csv")
    }

    fun createFullBackup(context: android.content.Context) {
        viewModelScope.launch {
            val boards = repository.getBoards().first()
            val allTasks = mutableListOf<Task>()
            boards.forEach { b ->
                allTasks.addAll(repository.getTasksForBoard(b.id).first())
            }
            val allEntries = repository.getAllCellEntries().first()
            val jsonText = com.tracker.matrixtracker.util.MatrixExportUtils.exportFullBackupJson(boards, allTasks, allEntries)
            com.tracker.matrixtracker.util.ShareUtils.shareTextAsFile(context, "matrix_tracker_backup.json", jsonText, "application/json")
        }
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean {
        val bundle = com.tracker.matrixtracker.util.MatrixExportUtils.parseBackupJson(jsonString) ?: return false
        return try {
            bundle.boards.forEach { board ->
                repository.saveBoard(board)
            }
            bundle.tasks.forEach { task ->
                repository.saveTask(task)
            }
            bundle.entries.forEach { entry ->
                repository.setCellStatus(entry.taskId, entry.periodKey, entry.status, entry.note)
            }
            if (bundle.boards.isNotEmpty()) {
                _selectedBoardId.value = bundle.boards.first().id
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun calculateTaskCompletion(
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Map<Long, Float> {
        if (periods.isEmpty()) return emptyMap()
        val totalPeriods = periods.size.toFloat()
        return tasks.associate { task ->
            val doneCount = periods.count { period ->
                cells[Pair(task.id, period.key)]?.status == CellStatus.DONE
            }
            task.id to ((doneCount / totalPeriods) * 100f)
        }
    }

    private fun calculatePeriodCompletion(
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Map<String, Float> {
        if (tasks.isEmpty()) return emptyMap()
        val totalTasks = tasks.size.toFloat()
        return periods.associate { period ->
            val doneCount = tasks.count { task ->
                cells[Pair(task.id, period.key)]?.status == CellStatus.DONE
            }
            period.key to ((doneCount / totalTasks) * 100f)
        }
    }
}
