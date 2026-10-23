package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.domain.model.Task

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatrixTaskRowHeader(
    task: Task,
    progressPercentage: Float,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onRenameTask: (() -> Unit)? = null,
    onMarkPast7DaysDone: (() -> Unit)? = null,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = modifier
                .width(155.dp)
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (onRenameTask != null || onMarkPast7DaysDone != null) {
                            isMenuExpanded = true
                        } else {
                            onLongClick?.invoke()
                        }
                    }
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${progressPercentage.toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            if (onRenameTask != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_task)) },
                    onClick = {
                        isMenuExpanded = false
                        onRenameTask()
                    }
                )
            }
            if (onMarkPast7DaysDone != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.mark_past_7_days)) },
                    onClick = {
                        isMenuExpanded = false
                        onMarkPast7DaysDone()
                    }
                )
            }
            if (onLongClick != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.manage_task_options)) },
                    onClick = {
                        isMenuExpanded = false
                        onLongClick()
                    }
                )
            }
        }
    }
}
