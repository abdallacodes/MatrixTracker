package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.ui.components.core.MatrixBottomSheet
import com.tracker.matrixtracker.ui.components.core.MatrixTextField

@OptIn(ExperimentalMaterial3Api::class)
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

    MatrixBottomSheet(
        onDismissRequest = onDismiss,
        title = "$dialogTitle ($periodKey)",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MatrixTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = stringResource(R.string.cell_note),
                singleLine = false,
                maxLines = 4,
                focusRequester = focusRequester
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (initialNote.isNotBlank()) {
                    TextButton(
                        onClick = {
                            onDeleteNote()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_note),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        onSaveNote(note)
                        onDismiss()
                    },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
