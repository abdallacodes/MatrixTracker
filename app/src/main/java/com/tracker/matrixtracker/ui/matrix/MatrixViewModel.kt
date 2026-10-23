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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

import com.tracker.matrixtracker.data.repository.AppSettings
import com.tracker.matrixtracker.data.repository.SettingsRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MatrixViewModel(
    private val repository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateAutoBackupEnabled(enabled: Boolean, context: android.content.Context) {
        viewModelScope.launch {
            settingsRepository.updateAutoBackupEnabled(enabled)
            val interval = settingsState.value.autoBackupIntervalDays
            com.tracker.matrixtracker.util.AutoBackupWorker.scheduleOrCancel(context, enabled, interval)
        }
    }

    fun updateAutoBackupIntervalDays(intervalDays: Int, context: android.content.Context) {
        viewModelScope.launch {
            settingsRepository.updateAutoBackupIntervalDays(intervalDays)
            val enabled = settingsState.value.autoBackupEnabled
            com.tracker.matrixtracker.util.AutoBackupWorker.scheduleOrCancel(context, enabled, intervalDays)
        }
    }

    fun updateDragGestureEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDragGestureEnabled(enabled)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updateAppLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.updateAppLanguage(language)
            val locales = if (language == "SYSTEM") {
                androidx.core.os.LocaleListCompat.getEmptyLocaleList()
            } else {
                androidx.core.os.LocaleListCompat.forLanguageTags(language)
            }
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales)
        }
    }

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

                val elapsedPeriods = getElapsedPeriods(periods)
                val overallProgress = calculateOverallCompletion(tasks, elapsedPeriods, cellsMap)
                val bestHabit = getBestPerformingHabit(tasks, taskProgress)
                val streak = calculateMaxStreak(tasks, periods, cellsMap)

                val taskStreaksMap = tasks.associate { task ->
                    task.id to com.tracker.matrixtracker.util.AnalyticsUtils.calculateCurrentStreak(task.id, periods, cellsMap)
                }
                val overallConsistency = com.tracker.matrixtracker.util.AnalyticsUtils.calculateOverallConsistency(tasks, periods, cellsMap)
                val bestDay = com.tracker.matrixtracker.util.AnalyticsUtils.calculateBestDayOfWeek(tasks, periods, cellsMap)

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
                    periodCompletionPercentage = periodProgress,
                    overallCompletionPercentage = overallProgress,
                    bestPerformingHabit = bestHabit,
                    currentStreakDays = streak,
                    taskStreaks = taskStreaksMap,
                    overallConsistencyPercent = overallConsistency,
                    bestDayOfWeek = bestDay
                )
            }
        }
    }.flatMapLatest { it }
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
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

    fun deleteCellNote(taskId: Long, periodKey: String) {
        viewModelScope.launch {
            repository.clearCellNote(taskId, periodKey)
        }
    }

    fun batchMarkCells(taskId: Long, periodKeys: List<String>, status: CellStatus) {
        if (periodKeys.isEmpty()) return
        viewModelScope.launch {
            repository.batchSetCellStatus(taskId, periodKeys, status)
        }
    }

    fun markAllHabitsForPeriod(periodKey: String, status: CellStatus) {
        viewModelScope.launch {
            repository.markAllHabitsForPeriod(periodKey, status, uiState.value.tasks)
        }
    }

    fun clearPeriod(periodKey: String) {
        viewModelScope.launch {
            repository.clearPeriod(periodKey)
        }
    }

    fun markTaskRecentDays(taskId: Long, daysCount: Int, status: CellStatus) {
        viewModelScope.launch {
            val periods = uiState.value.periods
            if (periods.isEmpty()) return@launch
            val currentIndex = periods.indexOfFirst { it.isCurrent }
            val endIndex = if (currentIndex >= 0) currentIndex else periods.lastIndex
            val startIndex = (endIndex - (daysCount - 1)).coerceAtLeast(0)
            val selectedKeys = periods.subList(startIndex, endIndex + 1).map { it.key }
            repository.batchSetCellStatus(taskId, selectedKeys, status)
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

    fun renameTask(task: Task, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.saveTask(task.copy(title = newTitle.trim()))
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

    fun printCurrentBoard(context: android.content.Context) {
        val board = uiState.value.currentBoard ?: return
        val periodTitle = getFormattedPeriodTitle()
        com.tracker.matrixtracker.util.PdfExportUtils.printBoard(
            context = context,
            board = board,
            tasks = uiState.value.tasks,
            periods = uiState.value.periods,
            cells = uiState.value.cells,
            taskCompletionPercentage = uiState.value.taskCompletionPercentage,
            periodCompletionPercentage = uiState.value.periodCompletionPercentage,
            overallCompletionPercentage = uiState.value.overallCompletionPercentage,
            periodTitle = periodTitle
        )
    }

    fun shareBoardAsPdf(context: android.content.Context) {
        val board = uiState.value.currentBoard ?: return
        val periodTitle = getFormattedPeriodTitle()
        val pdfBytes = com.tracker.matrixtracker.util.PdfExportUtils.exportPdfToBytes(
            board = board,
            tasks = uiState.value.tasks,
            periods = uiState.value.periods,
            cells = uiState.value.cells,
            taskCompletionPercentage = uiState.value.taskCompletionPercentage,
            periodCompletionPercentage = uiState.value.periodCompletionPercentage,
            overallCompletionPercentage = uiState.value.overallCompletionPercentage,
            periodTitle = periodTitle
        )
        val sanitizedTitle = board.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val filename = "${sanitizedTitle}_matrix.pdf"
        com.tracker.matrixtracker.util.ShareUtils.shareFile(context, filename, pdfBytes, "application/pdf")
    }

    private fun getFormattedPeriodTitle(): String {
        val board = uiState.value.currentBoard ?: return ""
        return when (board.cadence) {
            BoardCadence.DAILY -> _selectedYearMonth.value.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.US))
            BoardCadence.WEEKLY, BoardCadence.MONTHLY -> "${_selectedYear.value}"
        }
    }

    fun performBackupNow(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val boards = repository.getBoards().first()
                val allTasks = mutableListOf<Task>()
                boards.forEach { b ->
                    allTasks.addAll(repository.getTasksForBoard(b.id).first())
                }
                val allEntries = repository.getAllCellEntries().first()
                val jsonText = com.tracker.matrixtracker.util.MatrixExportUtils.exportFullBackupJson(boards, allTasks, allEntries)

                val targetDir = context.getExternalFilesDir(null) ?: context.filesDir
                val backupFile = java.io.File(targetDir, com.tracker.matrixtracker.util.AutoBackupWorker.BACKUP_FILE_NAME)
                backupFile.writeText(jsonText)

                val now = System.currentTimeMillis()
                settingsRepository.updateLastBackupTime(now)

                android.widget.Toast.makeText(context, context.getString(com.tracker.matrixtracker.R.string.backup_exported_success), android.widget.Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                android.widget.Toast.makeText(context, context.getString(com.tracker.matrixtracker.R.string.invalid_backup_file), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
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

    suspend fun exportDataToJson(outputStream: java.io.OutputStream): Result<Unit> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val boards = repository.getBoards().first()
                val allTasks = mutableListOf<Task>()
                boards.forEach { b ->
                    allTasks.addAll(repository.getTasksForBoard(b.id).first())
                }
                val allEntries = repository.getAllCellEntries().first()

                val jsonText = com.tracker.matrixtracker.util.MatrixExportUtils.exportFullBackupJson(boards, allTasks, allEntries)
                outputStream.use { stream ->
                    stream.write(jsonText.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    suspend fun importDataFromJson(inputStream: java.io.InputStream): Result<Unit> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val bundle = com.tracker.matrixtracker.util.MatrixExportUtils.parseBackupJson(jsonString)
                    ?: throw IllegalArgumentException("Invalid backup format")

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
            }
        }
    }

    fun exportBackupToUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val result = exportDataToJson(outputStream)
                if (result.isSuccess) {
                    android.widget.Toast.makeText(context, context.getString(com.tracker.matrixtracker.R.string.backup_exported_success), android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, context.getString(com.tracker.matrixtracker.R.string.invalid_backup_file), android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun restoreBackupFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val result = importDataFromJson(inputStream)
                if (result.isSuccess) {
                    android.widget.Toast.makeText(context, context.getString(com.tracker.matrixtracker.R.string.data_restored_success), android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, context.getString(com.tracker.matrixtracker.R.string.invalid_backup_file), android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getElapsedPeriods(periods: List<PeriodHeader>): List<PeriodHeader> {
        if (periods.isEmpty()) return emptyList()
        val currentIndex = periods.indexOfFirst { it.isCurrent }
        return if (currentIndex >= 0) {
            periods.take(currentIndex + 1)
        } else {
            periods
        }
    }

    private fun calculateTaskCompletion(
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Map<Long, Float> {
        val elapsed = getElapsedPeriods(periods)
        if (elapsed.isEmpty()) return emptyMap()
        val totalElapsed = elapsed.size.toFloat()
        return tasks.associate { task ->
            val doneCount = elapsed.count { period ->
                cells[Pair(task.id, period.key)]?.status == CellStatus.DONE
            }
            task.id to ((doneCount / totalElapsed) * 100f)
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

    private fun calculateOverallCompletion(
        tasks: List<Task>,
        elapsedPeriods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Float {
        if (tasks.isEmpty() || elapsedPeriods.isEmpty()) return 0f
        val totalPossible = (tasks.size * elapsedPeriods.size).toFloat()
        val doneCount = tasks.sumOf { task ->
            elapsedPeriods.count { period ->
                cells[Pair(task.id, period.key)]?.status == CellStatus.DONE
            }
        }
        return (doneCount / totalPossible) * 100f
    }

    private fun getBestPerformingHabit(
        tasks: List<Task>,
        taskCompletion: Map<Long, Float>
    ): String? {
        if (tasks.isEmpty()) return null
        val bestTask = tasks.maxByOrNull { taskCompletion[it.id] ?: 0f } ?: return null
        val bestRate = taskCompletion[bestTask.id] ?: 0f
        return if (bestRate > 0f) bestTask.title else null
    }

    private fun calculateMaxStreak(
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): Int {
        if (tasks.isEmpty() || periods.isEmpty()) return 0
        val currentIndex = periods.indexOfFirst { it.isCurrent }
        val relevantPeriods = if (currentIndex >= 0) {
            periods.take(currentIndex + 1).reversed()
        } else {
            periods.reversed()
        }

        return tasks.maxOfOrNull { task ->
            var streak = 0
            for (period in relevantPeriods) {
                if (cells[Pair(task.id, period.key)]?.status == CellStatus.DONE) {
                    streak++
                } else {
                    break
                }
            }
            streak
        } ?: 0
    }
}
