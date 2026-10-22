package com.tracker.matrixtracker.ui.matrix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.matrix.components.MatrixGrid
import com.tracker.matrixtracker.ui.matrix.components.PeriodNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    viewModel: MatrixViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    var selectedTaskForAction by remember { mutableStateOf<Task?>(null) }

    var selectedCellForNote by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var noteText by remember { mutableStateOf("") }

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
                availableBoards = uiState.availableBoards,
                selectedYearMonth = uiState.selectedYearMonth,
                selectedYear = uiState.selectedYear,
                onSelectBoard = viewModel::selectBoard,
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
