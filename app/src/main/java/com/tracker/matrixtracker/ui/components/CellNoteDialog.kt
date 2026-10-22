package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.tracker.matrixtracker.R

@Composable
fun CellNoteDialog(
    periodKey: String,
    initialNote: String,
    onSaveNote: (note: String) -> Unit,
    onDeleteNote: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var note by remember { mutableStateOf(initialNote) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val dialogTitle = if (initialNote.isBlank()) {
        stringResource(R.string.add_note)
    } else {
        stringResource(R.string.edit_note)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$dialogTitle ($periodKey)") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.cell_note)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveNote(note)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            if (initialNote.isNotBlank()) {
                TextButton(
                    onClick = {
                        onDeleteNote()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_note),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
        modifier = modifier
    )
}
