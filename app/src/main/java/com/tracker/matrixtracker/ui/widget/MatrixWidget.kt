package com.tracker.matrixtracker.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.room.Room
import com.tracker.matrixtracker.MainActivity
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val taskIdKey = ActionParameters.Key<Long>("taskId")
val currentStatusKey = ActionParameters.Key<String>("currentStatus")

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[taskIdKey] ?: return
        val currentStatusStr = parameters[currentStatusKey] ?: CellStatus.EMPTY.name
        val currentStatus = runCatching { CellStatus.valueOf(currentStatusStr) }.getOrDefault(CellStatus.EMPTY)

        val nextStatus = if (currentStatus == CellStatus.DONE) CellStatus.EMPTY else CellStatus.DONE
        val todayKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val db = AppDatabase.getInstance(context)

        val repository = TrackerRepositoryImpl(
            boardDao = db.boardDao(),
            taskDao = db.taskDao(),
            cellEntryDao = db.cellEntryDao()
        )

        repository.setCellStatus(taskId, todayKey, nextStatus)

        MatrixWidget().updateAll(context)
    }
}

class MatrixWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)

        val repository = TrackerRepositoryImpl(
            boardDao = db.boardDao(),
            taskDao = db.taskDao(),
            cellEntryDao = db.cellEntryDao()
        )

        val boards = repository.getBoards().first()
        val board = boards.firstOrNull()

        val tasks = if (board != null) {
            repository.getTasksForBoard(board.id).first()
        } else emptyList()

        val todayKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val taskIds = tasks.map { it.id }
        val todayEntries = if (taskIds.isNotEmpty()) {
            repository.getCellEntries(taskIds, listOf(todayKey)).first()
        } else emptyList()

        val cellsMap = todayEntries.associateBy { it.taskId }
        val doneCount = tasks.count { cellsMap[it.id]?.status == CellStatus.DONE }

        provideContent {
            WidgetContent(
                context = context,
                board = board,
                tasks = tasks,
                cellsMap = cellsMap,
                doneCount = doneCount,
                todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))
            )
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        board: Board?,
        tasks: List<Task>,
        cellsMap: Map<Long, CellEntry>,
        doneCount: Int,
        todayStr: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF1E2836), night = androidx.compose.ui.graphics.Color(0xFF121824)))
                .padding(12.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(openAppIntent)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = board?.title ?: "Matrix Tracker",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = todayStr,
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF90A4AE), night = androidx.compose.ui.graphics.Color(0xFF90A4AE)),
                            fontSize = 11.sp
                        )
                    )
                }

                if (tasks.isNotEmpty()) {
                    Text(
                        text = "$doneCount/${tasks.size} Done",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF81C784), night = androidx.compose.ui.graphics.Color(0xFF81C784)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.padding(vertical = 4.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits created yet",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF90A4AE), night = androidx.compose.ui.graphics.Color(0xFF90A4AE)),
                            fontSize = 12.sp
                        )
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(tasks) { task ->
                        val entry = cellsMap[task.id]
                        val isDone = entry?.status == CellStatus.DONE

                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.title,
                                style = TextStyle(
                                    color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                                    fontSize = 13.sp
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Box(
                                modifier = GlanceModifier
                                    .background(
                                        if (isDone) ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF2E7D32), night = androidx.compose.ui.graphics.Color(0xFF133820))
                                        else ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF37474F), night = androidx.compose.ui.graphics.Color(0xFF263238))
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .clickable(
                                        actionRunCallback<ToggleTaskAction>(
                                            actionParametersOf(
                                                taskIdKey to task.id,
                                                currentStatusKey to (entry?.status?.name ?: CellStatus.EMPTY.name)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDone) "✓ Done" else "Mark",
                                    style = TextStyle(
                                        color = ColorProvider(
                                            day = if (isDone) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color.White,
                                            night = if (isDone) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color.White
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class MatrixWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MatrixWidget()
}
