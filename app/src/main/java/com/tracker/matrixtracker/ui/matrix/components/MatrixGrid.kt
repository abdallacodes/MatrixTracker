package com.tracker.matrixtracker.ui.matrix.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.components.MatrixHeaderCell
import com.tracker.matrixtracker.ui.components.MatrixStatusCell
import com.tracker.matrixtracker.ui.components.MatrixTaskRowHeader
import com.tracker.matrixtracker.ui.matrix.PeriodHeader

@Composable
fun MatrixGrid(
    tasks: List<Task>,
    periods: List<PeriodHeader>,
    cells: Map<Pair<Long, String>, CellEntry>,
    taskCompletionPercentage: Map<Long, Float>,
    periodCompletionPercentage: Map<String, Float>,
    onToggleCell: (taskId: Long, periodKey: String) -> Unit,
    onBatchMarkCells: (taskId: Long, periodKeys: List<String>, status: CellStatus) -> Unit,
    onMarkAllHabitsForPeriod: (periodKey: String, status: CellStatus) -> Unit,
    onClearPeriod: (periodKey: String) -> Unit,
    onMarkTaskRecentDays: (taskId: Long, daysCount: Int, status: CellStatus) -> Unit,
    onCellLongClick: (taskId: Long, periodKey: String) -> Unit,
    onTaskLongClick: (task: Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val cellWidthPx = with(density) { 48.dp.toPx() }
    var draggedTaskId by remember { mutableStateOf<Long?>(null) }
    var dragStartIndex by remember { mutableIntStateOf(0) }
    var dragEndIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(periods) {
        val currentIndex = periods.indexOfFirst { it.isCurrent }
        if (currentIndex >= 0) {
            val targetScrollPx = ((currentIndex - 2) * cellWidthPx).coerceAtLeast(0f).toInt()
            horizontalScrollState.animateScrollTo(targetScrollPx)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .width(155.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Habit / Task",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(horizontalScrollState)
            ) {
                periods.forEach { periodHeader ->
                    MatrixHeaderCell(
                        header = periodHeader,
                        onMarkAllDone = { onMarkAllHabitsForPeriod(periodHeader.key, CellStatus.DONE) },
                        onClearAll = { onClearPeriod(periodHeader.key) }
                    )
                }

                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.verticalScroll(verticalScrollState)
            ) {
                tasks.forEach { task ->
                    val progress = taskCompletionPercentage[task.id] ?: 0f
                    MatrixTaskRowHeader(
                        task = task,
                        progressPercentage = progress,
                        onLongClick = { onTaskLongClick(task) },
                        onMarkPast7DaysDone = { onMarkTaskRecentDays(task.id, 7, CellStatus.DONE) }
                    )
                }

                Box(
                    modifier = Modifier
                        .width(155.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Daily Total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
            ) {
                Column {
                    tasks.forEach { task ->
                        Row(
                            modifier = Modifier.pointerInput(task.id, periods) {
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        draggedTaskId = task.id
                                        val col = (offset.x / cellWidthPx).toInt().coerceIn(0, periods.lastIndex)
                                        dragStartIndex = col
                                        dragEndIndex = col
                                    },
                                    onHorizontalDrag = { change, _ ->
                                        if (draggedTaskId == task.id) {
                                            val currentX = change.position.x
                                            val col = (currentX / cellWidthPx).toInt().coerceIn(0, periods.lastIndex)
                                            if (col != dragEndIndex) {
                                                dragEndIndex = col
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (draggedTaskId == task.id) {
                                            val start = minOf(dragStartIndex, dragEndIndex)
                                            val end = maxOf(dragStartIndex, dragEndIndex)
                                            val keys = (start..end).map { periods[it].key }
                                            onBatchMarkCells(task.id, keys, CellStatus.DONE)
                                        }
                                        draggedTaskId = null
                                    },
                                    onDragCancel = {
                                        draggedTaskId = null
                                    }
                                )
                            }
                        ) {
                            periods.forEachIndexed { colIndex, period ->
                                val isHighlighted = (draggedTaskId == task.id) &&
                                        (colIndex in minOf(dragStartIndex, dragEndIndex)..maxOf(dragStartIndex, dragEndIndex))
                                val entry = cells[Pair(task.id, period.key)]

                                Box {
                                    MatrixStatusCell(
                                        entry = entry,
                                        onToggle = { onToggleCell(task.id, period.key) },
                                        onLongClick = { onCellLongClick(task.id, period.key) }
                                    )

                                    if (isHighlighted) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }

                            val progress = taskCompletionPercentage[task.id] ?: 0f
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 44.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${progress.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        periods.forEach { period ->
                            val percentage = periodCompletionPercentage[period.key] ?: 0f
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 36.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${percentage.toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 36.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}
