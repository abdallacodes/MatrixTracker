package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.components.core.MatrixConfirmDialog

@Composable
fun TaskManagementDialog(
    tasks: List<Task>,
    onReorderTasks: (fromIndex: Int, toIndex: Int) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_tasks)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (tasks.isEmpty()) {
                    Text(
                        text = "No tasks added yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                            val canMoveUp = index > 0
                            val canMoveDown = index < tasks.size - 1

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    IconButton(
                                        onClick = { onReorderTasks(index, index - 1) },
                                        enabled = canMoveUp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = if (canMoveUp) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onReorderTasks(index, index + 1) },
                                        enabled = canMoveDown
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = if (canMoveDown) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    }

                                    IconButton(
                                        onClick = { taskToDelete = task }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_task),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        },
        modifier = modifier
    )

    taskToDelete?.let { task ->
        MatrixConfirmDialog(
            title = stringResource(R.string.delete_task),
            message = stringResource(R.string.delete_task_warning, task.title),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                onDeleteTask(task)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null },
            icon = Icons.Default.Delete,
            isDestructive = true
        )
    }
}
