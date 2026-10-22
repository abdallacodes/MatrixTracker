package com.tracker.matrixtracker.ui.matrix

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.components.BoardSelectionBottomSheet
import com.tracker.matrixtracker.ui.components.CreateBoardDialog
import com.tracker.matrixtracker.ui.components.TaskManagementDialog
import com.tracker.matrixtracker.ui.matrix.components.MatrixGrid
import com.tracker.matrixtracker.ui.matrix.components.PeriodNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    viewModel: MatrixViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var isOverflowMenuExpanded by remember { mutableStateOf(false) }

    var showBoardSelectionBottomSheet by remember { mutableStateOf(false) }
    var showCreateBoardDialog by remember { mutableStateOf(false) }

    var selectedBoardForEdit by remember { mutableStateOf<Board?>(null) }
    var editBoardTitle by remember { mutableStateOf("") }

    var selectedBoardForDelete by remember { mutableStateOf<Board?>(null) }

    var showTaskManagementDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Matrix Habit Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
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
                                text = { Text("Export Board as CSV") },
                                onClick = {
                                    isOverflowMenuExpanded = false
                                    viewModel.exportCurrentBoardAsCsv(context)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Backup All Data (JSON)") },
                                onClick = {
                                    isOverflowMenuExpanded = false
                                    viewModel.createFullBackup(context)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Restore from Backup") },
                                onClick = {
                                    isOverflowMenuExpanded = false
                                    restoreFileLauncher.launch("*/*")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Manage Tasks") },
                                onClick = {
                                    isOverflowMenuExpanded = false
                                    showTaskManagementDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                MatrixGrid(
                    tasks = uiState.tasks,
                    periods = uiState.periods,
                    cells = uiState.cells,
                    taskCompletionPercentage = uiState.taskCompletionPercentage,
                    periodCompletionPercentage = uiState.periodCompletionPercentage,
                    onToggleCell = viewModel::toggleCell,
                    onCellLongClick = { taskId, periodKey ->
                        val existingNote = uiState.cells[Pair(taskId, periodKey)]?.note ?: ""
                        selectedCellForNote = Pair(taskId, periodKey)
                        noteText = existingNote
                    },
                    onTaskLongClick = { task ->
                        selectedTaskForAction = task
                    }
                )
            }
        }
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
        AlertDialog(
            onDismissRequest = { selectedBoardForEdit = null },
            title = { Text("Edit Board Title") },
            text = {
                OutlinedTextField(
                    value = editBoardTitle,
                    onValueChange = { editBoardTitle = it },
                    label = { Text("Board Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editBoardTitle.isNotBlank()) {
                            viewModel.updateBoard(board, editBoardTitle)
                            selectedBoardForEdit = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBoardForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedBoardForDelete?.let { board ->
        AlertDialog(
            onDismissRequest = { selectedBoardForDelete = null },
            title = { Text("Delete Board") },
            text = { Text("Are you sure you want to delete \"${board.title}\"? All its tasks and matrix entries will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBoard(board)
                        selectedBoardForDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBoardForDelete = null }) {
                    Text("Cancel")
                }
            }
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
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add New Habit / Task") },
            text = {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addNewTask(newTaskTitle)
                            newTaskTitle = ""
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedTaskForAction?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTaskForAction = null },
            title = { Text("Manage Task") },
            text = { Text("Do you want to delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(task)
                        selectedTaskForAction = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTaskForAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedCellForNote?.let { (taskId, periodKey) ->
        AlertDialog(
            onDismissRequest = { selectedCellForNote = null },
            title = { Text("Cell Note ($periodKey)") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setCellNote(taskId, periodKey, noteText)
                        selectedCellForNote = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCellForNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
