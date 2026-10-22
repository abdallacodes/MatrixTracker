package com.tracker.matrixtracker.domain.repository

import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    fun getBoards(): Flow<List<Board>>
    suspend fun getBoard(id: Long): Board?
    suspend fun saveBoard(board: Board): Long
    suspend fun deleteBoard(board: Board)

    fun getTasksForBoard(boardId: Long): Flow<List<Task>>
    suspend fun saveTask(task: Task): Long
    suspend fun deleteTask(task: Task)
    suspend fun updateTaskOrder(tasks: List<Task>)

    fun getCellEntries(taskIds: List<Long>, periodKeys: List<String>): Flow<List<CellEntry>>
    fun getAllCellEntries(): Flow<List<CellEntry>>
    suspend fun setCellStatus(
        taskId: Long,
        periodKey: String,
        status: CellStatus,
        note: String? = null
    )
    suspend fun clearCellNote(taskId: Long, periodKey: String)
    suspend fun batchSetCellStatus(
        taskId: Long,
        periodKeys: List<String>,
        status: CellStatus
    )
    suspend fun markAllHabitsForPeriod(
        periodKey: String,
        status: CellStatus,
        tasks: List<Task>
    )
    suspend fun clearPeriod(periodKey: String)
    suspend fun toggleCellStatus(
        taskId: Long,
        periodKey: String,
        currentStatus: CellStatus
    )
}
