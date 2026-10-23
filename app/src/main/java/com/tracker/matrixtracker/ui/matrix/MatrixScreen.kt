package com.tracker.matrixtracker.ui.matrix

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import com.tracker.matrixtracker.ui.components.core.MatrixBottomSheet
import com.tracker.matrixtracker.ui.components.core.MatrixConfirmDialog
import com.tracker.matrixtracker.ui.components.core.MatrixTextField
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.components.BoardSelectionBottomSheet
import com.tracker.matrixtracker.ui.components.CellNoteDialog
import com.tracker.matrixtracker.ui.components.ConsistencyDashboard
import com.tracker.matrixtracker.ui.components.CreateBoardDialog
import com.tracker.matrixtracker.ui.components.EditTaskDialog
import com.tracker.matrixtracker.ui.components.SettingsDialog
import com.tracker.matrixtracker.ui.components.TaskManagementDialog
import com.tracker.matrixtracker.ui.matrix.components.MatrixGrid
import com.tracker.matrixtracker.ui.matrix.components.PeriodNavigationBar
import kotlinx.coroutines.launch

private val ScreenRotationIcon: ImageVector
    get() {
        if (_screenRotationIcon != null) return _screenRotationIcon!!
        _screenRotationIcon = ImageVector.Builder(
            name = "ScreenRotation",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Unspecified),
                stroke = SolidColor(Color.Unspecified),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16.48f, 2.52f)
                lineTo(12.73f, 6.27f)
                lineTo(14.14f, 7.68f)
                lineTo(16.0f, 5.83f)
                verticalLineTo(12.0f)
                horizontalLineTo(18.0f)
                verticalLineTo(5.83f)
                lineTo(19.86f, 7.68f)
                lineTo(21.27f, 6.27f)
                lineTo(17.52f, 2.52f)
                close()
                moveTo(10.27f, 21.48f)
                lineTo(14.02f, 17.73f)
                lineTo(12.61f, 16.32f)
                lineTo(10.75f, 18.17f)
                verticalLineTo(12.0f)
                horizontalLineTo(8.75f)
                verticalLineTo(18.17f)
                lineTo(6.89f, 16.32f)
                lineTo(5.48f, 17.73f)
                lineTo(9.23f, 21.48f)
                close()
            }
        }.build()
        return _screenRotationIcon!!
    }

private var _screenRotationIcon: ImageVector? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    viewModel: MatrixViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun toggleOrientation() {
        if (activity != null) {
            if (isLandscape) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    var isOverflowMenuExpanded by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showBoardSelectionBottomSheet by remember { mutableStateOf(false) }
    var showCreateBoardDialog by remember { mutableStateOf(false) }

    var selectedBoardForEdit by remember { mutableStateOf<Board?>(null) }
    var editBoardTitle by remember { mutableStateOf("") }

    var selectedBoardForDelete by remember { mutableStateOf<Board?>(null) }

    var showTaskManagementDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    var selectedTaskForEdit by remember { mutableStateOf<Task?>(null) }
    var selectedTaskForAction by remember { mutableStateOf<Task?>(null) }

    var selectedCellForNote by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var noteText by remember { mutableStateOf("") }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            runCatching {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonString = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (!jsonString.isNullOrBlank()) {
                    scope.launch {
                        viewModel.restoreFromBackupJson(jsonString)
                    }
                }
            }
        }
    }

    if (isLandscape) {
        Box(modifier = modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                MatrixGrid(
                    tasks = uiState.tasks,
                    periods = uiState.periods,
                    cells = uiState.cells,
                    taskCompletionPercentage = uiState.taskCompletionPercentage,
                    periodCompletionPercentage = uiState.periodCompletionPercentage,
                    dragGestureEnabled = settingsState.dragGestureEnabled,
                    onToggleCell = { taskId, periodKey ->
                        val existingNote = uiState.cells[Pair(taskId, periodKey)]?.note
                        if (!existingNote.isNullOrBlank()) {
                            android.widget.Toast.makeText(context, context.resources.getString(R.string.note_prefix, existingNote), android.widget.Toast.LENGTH_SHORT).show()
                        }
                        viewModel.toggleCell(taskId, periodKey)
                    },
                    onBatchMarkCells = viewModel::batchMarkCells,
                    onMarkAllHabitsForPeriod = viewModel::markAllHabitsForPeriod,
                    onClearPeriod = viewModel::clearPeriod,
                    onMarkTaskRecentDays = viewModel::markTaskRecentDays,
                    onCellLongClick = { taskId, periodKey ->
                        val existingNote = uiState.cells[Pair(taskId, periodKey)]?.note ?: ""
                        selectedCellForNote = Pair(taskId, periodKey)
                        noteText = existingNote
                    },
                    onTaskLongClick = { task ->
                        selectedTaskForAction = task
                    },
                    onRenameTask = { task ->
                        selectedTaskForEdit = task
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { toggleOrientation() },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = ScreenRotationIcon,
                        contentDescription = "Exit Landscape"
                    )
                }

                FloatingActionButton(
                    onClick = { showSettingsDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { toggleOrientation() }) {
                            Icon(
                                imageVector = ScreenRotationIcon,
                                contentDescription = "Switch Orientation"
                            )
                        }

                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }

                        Box {
                            IconButton(onClick = { isOverflowMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options"
                                )
                            }

                            DropdownMenu(
                                expanded = isOverflowMenuExpanded,
                                onDismissRequest = { isOverflowMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_csv)) },
                                    onClick = {
                                        isOverflowMenuExpanded = false
                                        viewModel.exportCurrentBoardAsCsv(context)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.print_pdf)) },
                                    onClick = {
                                        isOverflowMenuExpanded = false
                                        viewModel.printCurrentBoard(context)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share_pdf)) },
                                    onClick = {
                                        isOverflowMenuExpanded = false
                                        viewModel.shareBoardAsPdf(context)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.backup_json)) },
                                    onClick = {
                                        isOverflowMenuExpanded = false
                                        viewModel.createFullBackup(context)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.restore_backup)) },
                                    onClick = {
                                        isOverflowMenuExpanded = false
                                        restoreFileLauncher.launch("*/*")
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.manage_tasks)) },
                                    onClick = {
                                        isOverflowMenuExpanded = false
                                        showTaskManagementDialog = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                PeriodNavigationBar(
                    currentBoard = uiState.currentBoard,
                    selectedYearMonth = uiState.selectedYearMonth,
                    selectedYear = uiState.selectedYear,
                    onOpenBoardSelection = { showBoardSelectionBottomSheet = true },
                    onPreviousPeriod = viewModel::previousPeriod,
                    onNextPeriod = viewModel::nextPeriod,
                    onGoToCurrent = viewModel::goToCurrentPeriod,
                    onAddNewTask = { showAddTaskDialog = true }
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MatrixGrid(
                            tasks = uiState.tasks,
                            periods = uiState.periods,
                            cells = uiState.cells,
                            taskCompletionPercentage = uiState.taskCompletionPercentage,
                            periodCompletionPercentage = uiState.periodCompletionPercentage,
                            dragGestureEnabled = settingsState.dragGestureEnabled,
                            onToggleCell = { taskId, periodKey ->
                                val existingNote = uiState.cells[Pair(taskId, periodKey)]?.note
                                if (!existingNote.isNullOrBlank()) {
                                    android.widget.Toast.makeText(context, context.resources.getString(R.string.note_prefix, existingNote), android.widget.Toast.LENGTH_SHORT).show()
                                }
                                viewModel.toggleCell(taskId, periodKey)
                            },
                            onBatchMarkCells = viewModel::batchMarkCells,
                            onMarkAllHabitsForPeriod = viewModel::markAllHabitsForPeriod,
                            onClearPeriod = viewModel::clearPeriod,
                            onMarkTaskRecentDays = viewModel::markTaskRecentDays,
                            onCellLongClick = { taskId, periodKey ->
                                val existingNote = uiState.cells[Pair(taskId, periodKey)]?.note ?: ""
                                selectedCellForNote = Pair(taskId, periodKey)
                                noteText = existingNote
                            },
                            onTaskLongClick = { task ->
                                selectedTaskForAction = task
                            },
                            onRenameTask = { task ->
                                selectedTaskForEdit = task
                            },
                            modifier = Modifier.weight(1f)
                        )

                        ConsistencyDashboard(
                            overallConsistencyPercent = uiState.overallConsistencyPercent,
                            bestDayOfWeek = uiState.bestDayOfWeek,
                            tasks = uiState.tasks,
                            taskStreaks = uiState.taskStreaks
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settingsState,
            onUpdateAutoBackupEnabled = { enabled -> viewModel.updateAutoBackupEnabled(enabled, context) },
            onUpdateAutoBackupIntervalDays = { interval -> viewModel.updateAutoBackupIntervalDays(interval, context) },
            onUpdateDragGestureEnabled = viewModel::updateDragGestureEnabled,
            onUpdateThemeMode = viewModel::updateThemeMode,
            onUpdateAppLanguage = viewModel::updateAppLanguage,
            onBackupNow = { viewModel.performBackupNow(context) },
            onExportBackupUri = { uri -> viewModel.exportBackupToUri(context, uri) },
            onRestoreBackupUri = { uri -> viewModel.restoreBackupFromUri(context, uri) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showBoardSelectionBottomSheet) {
        BoardSelectionBottomSheet(
            currentBoard = uiState.currentBoard,
            availableBoards = uiState.availableBoards,
            onSelectBoard = { board -> viewModel.selectBoard(board.id) },
            onEditBoard = { board ->
                selectedBoardForEdit = board
                editBoardTitle = board.title
            },
            onDeleteBoard = { board -> selectedBoardForDelete = board },
            onCreateNewBoard = { showCreateBoardDialog = true },
            onDismiss = { showBoardSelectionBottomSheet = false }
        )
    }

    if (showCreateBoardDialog) {
        CreateBoardDialog(
            onDismiss = { showCreateBoardDialog = false },
            onCreateBoard = { title, cadence ->
                viewModel.createBoard(title, cadence)
            }
        )
    }

    selectedBoardForEdit?.let { board ->
        val editBoardFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            editBoardFocusRequester.requestFocus()
        }

        MatrixBottomSheet(
            onDismissRequest = { selectedBoardForEdit = null },
            title = stringResource(R.string.edit_board_title)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MatrixTextField(
                    value = editBoardTitle,
                    onValueChange = { editBoardTitle = it },
                    placeholder = stringResource(R.string.board_title),
                    singleLine = true,
                    focusRequester = editBoardFocusRequester
                )

                Button(
                    onClick = {
                        if (editBoardTitle.isNotBlank()) {
                            viewModel.updateBoard(board, editBoardTitle.trim())
                            selectedBoardForEdit = null
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.save), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    selectedBoardForDelete?.let { board ->
        MatrixConfirmDialog(
            title = stringResource(R.string.delete_board),
            message = stringResource(R.string.delete_board_warning, board.title),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteBoard(board)
                selectedBoardForDelete = null
            },
            onDismiss = { selectedBoardForDelete = null },
            icon = androidx.compose.material.icons.Icons.Default.Delete,
            isDestructive = true
        )
    }

    selectedTaskForEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onSave = { newTitle ->
                viewModel.renameTask(task, newTitle)
                selectedTaskForEdit = null
            },
            onDismiss = { selectedTaskForEdit = null }
        )
    }

    if (showTaskManagementDialog) {
        TaskManagementDialog(
            tasks = uiState.tasks,
            onReorderTasks = viewModel::reorderTasks,
            onDeleteTask = viewModel::deleteTask,
            onDismiss = { showTaskManagementDialog = false }
        )
    }

    if (showAddTaskDialog) {
        val addTaskFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            addTaskFocusRequester.requestFocus()
        }

        MatrixBottomSheet(
            onDismissRequest = { showAddTaskDialog = false },
            title = stringResource(R.string.add_task)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MatrixTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = stringResource(R.string.task_name),
                    singleLine = true,
                    focusRequester = addTaskFocusRequester
                )

                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addNewTask(newTaskTitle.trim())
                            newTaskTitle = ""
                            showAddTaskDialog = false
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.add_task),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    selectedTaskForAction?.let { task ->
        MatrixConfirmDialog(
            title = stringResource(R.string.delete_task),
            message = stringResource(R.string.delete_task_warning, task.title),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteTask(task)
                selectedTaskForAction = null
            },
            onDismiss = { selectedTaskForAction = null },
            icon = androidx.compose.material.icons.Icons.Default.Delete,
            isDestructive = true
        )
    }

    selectedCellForNote?.let { (taskId, periodKey) ->
        val existingNote = uiState.cells[Pair(taskId, periodKey)]?.note ?: ""
        CellNoteDialog(
            periodKey = periodKey,
            initialNote = existingNote,
            onSaveNote = { note ->
                viewModel.setCellNote(taskId, periodKey, note)
            },
            onDeleteNote = {
                viewModel.deleteCellNote(taskId, periodKey)
            },
            onDismiss = { selectedCellForNote = null }
        )
    }
}
