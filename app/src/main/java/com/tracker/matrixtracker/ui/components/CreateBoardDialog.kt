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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.ui.components.core.MatrixBottomSheet
import com.tracker.matrixtracker.ui.components.core.MatrixTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBoardDialog(
    onDismiss: () -> Unit,
    onCreateBoard: (title: String, cadence: BoardCadence) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var selectedCadence by remember { mutableStateOf(BoardCadence.DAILY) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    MatrixBottomSheet(
        onDismissRequest = onDismiss,
        title = "Create New Board",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MatrixTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Board Title",
                singleLine = true,
                focusRequester = focusRequester
            )

            Text(
                text = "Cadence Frequency:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BoardCadence.entries.forEach { cadence ->
                    FilterChip(
                        selected = (selectedCadence == cadence),
                        onClick = { selectedCadence = cadence },
                        label = { Text(cadence.name, fontSize = 12.sp) }
                    )
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateBoard(title.trim(), selectedCadence)
                        onDismiss()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = "Create",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
