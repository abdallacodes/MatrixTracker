package com.tracker.matrixtracker.ui.matrix.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
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
    dragGestureEnabled: Boolean = false,
    scrollToTodayTrigger: Int = 0,
    onToggleCell: (taskId: Long, periodKey: String) -> Unit,
    onBatchMarkCells: (taskId: Long, periodKeys: List<String>, status: CellStatus) -> Unit,
    onMarkAllHabitsForPeriod: (periodKey: String, status: CellStatus) -> Unit,
    onClearPeriod: (periodKey: String) -> Unit,
    onMarkTaskRecentDays: (taskId: Long, daysCount: Int, status: CellStatus) -> Unit,
    onCellLongClick: (taskId: Long, periodKey: String) -> Unit,
    onTaskLongClick: (task: Task) -> Unit,
    onRenameTask: ((Task) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val cellWidthPx = with(density) { 48.dp.toPx() }
    var draggedTaskId by remember { mutableStateOf<Long?>(null) }
    var dragStartIndex by remember { mutableIntStateOf(0) }
    var dragEndIndex by remember { mutableIntStateOf(0) }

    val todayIndex = periods.indexOfFirst { it.isCurrent }

    LaunchedEffect(periods) {
        if (todayIndex >= 0) {
            val targetScrollPx = ((todayIndex - 2) * cellWidthPx).coerceAtLeast(0f).toInt()
            horizontalScrollState.animateScrollTo(targetScrollPx)
        }
    }

    LaunchedEffect(scrollToTodayTrigger) {
        if (scrollToTodayTrigger > 0 && todayIndex >= 0) {
            val targetScrollPx = ((todayIndex - 2) * cellWidthPx).coerceAtLeast(0f).toInt()
            horizontalScrollState.animateScrollTo(targetScrollPx)
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed Top Date Header Row
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .width(155.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.habit_or_task),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
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
                            .width(2.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )

                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
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

            // Body Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        val progress = taskCompletionPercentage[task.id] ?: 0f
                        MatrixTaskRowHeader(
                            task = task,
                            progressPercentage = progress,
                            onLongClick = { onTaskLongClick(task) },
                            onRenameTask = { onRenameTask?.invoke(task) },
                            onMarkPast7DaysDone = { onMarkTaskRecentDays(task.id, 7, CellStatus.DONE) }
                        )

                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(horizontalScrollState)
                                .then(
                                    if (dragGestureEnabled) {
                                        Modifier.pointerInput(task.id, periods) {
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
                                                        val validKeys = (start..end)
                                                            .map { periods[it] }
                                                            .filter { !it.isFuture }
                                                            .map { it.key }
                                                        if (validKeys.isNotEmpty()) {
                                                            onBatchMarkCells(task.id, validKeys, CellStatus.DONE)
                                                        }
                                                    }
                                                    draggedTaskId = null
                                                },
                                                onDragCancel = {
                                                    draggedTaskId = null
                                                }
                                            )
                                        }
                                    } else Modifier
                                )
                        ) {
                            periods.forEachIndexed { colIndex, period ->
                                val isHighlighted = (draggedTaskId == task.id) &&
                                        (colIndex in minOf(dragStartIndex, dragEndIndex)..maxOf(dragStartIndex, dragEndIndex))
                                val entry = cells[Pair(task.id, period.key)]

                                Box {
                                    MatrixStatusCell(
                                        entry = entry,
                                        onToggle = { onToggleCell(task.id, period.key) },
                                        onLongClick = { onCellLongClick(task.id, period.key) },
                                        isFuture = period.isFuture,
                                        colIndex = colIndex
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

                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            )

                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
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
                }

                // Daily total summary row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .height(36.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.daily_total),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        periods.forEach { period ->
                            val percentage = periodCompletionPercentage[period.key] ?: 0f
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 36.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
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
                                .width(2.dp)
                                .height(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        )

                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        )
                    }
                }
            }
        }
    }
}
