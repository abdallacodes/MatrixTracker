package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.matrixtracker.domain.model.BoardCadence

@Composable
fun CreateBoardDialog(
    onDismiss: () -> Unit,
    onCreateBoard: (title: String, cadence: BoardCadence) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var selectedCadence by remember { mutableStateOf(BoardCadence.DAILY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Board") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Board Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Cadence Frequency:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BoardCadence.entries.forEach { cadence ->
                        FilterChip(
                            selected = (selectedCadence == cadence),
                            onClick = { selectedCadence = cadence },
                            label = { Text(cadence.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateBoard(title.trim(), selectedCadence)
                        onDismiss()
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}
