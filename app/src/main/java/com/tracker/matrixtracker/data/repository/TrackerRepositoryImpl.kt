package com.tracker.matrixtracker.data.repository

import com.tracker.matrixtracker.data.local.dao.BoardDao
import com.tracker.matrixtracker.data.local.dao.CellEntryDao
import com.tracker.matrixtracker.data.local.dao.TaskDao
import com.tracker.matrixtracker.data.local.entity.CellEntryEntity
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TrackerRepositoryImpl(
    private val boardDao: BoardDao,
    private val taskDao: TaskDao,
    private val cellEntryDao: CellEntryDao,
) : TrackerRepository {

    override fun getBoards(): Flow<List<Board>> {
        return boardDao.getBoards().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBoard(id: Long): Board? {
        return boardDao.getBoardById(id)?.toDomain()
    }

    override suspend fun saveBoard(board: Board): Long {
        return boardDao.insertBoard(board.toEntity())
    }

    override suspend fun deleteBoard(board: Board) {
        boardDao.deleteBoard(board.toEntity())
    }

    override fun getTasksForBoard(boardId: Long): Flow<List<Task>> {
        return taskDao.getTasksByBoardId(boardId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun updateTaskOrder(tasks: List<Task>) {
        tasks.forEachIndexed { index, task ->
            taskDao.updateTask(task.copy(sortOrder = index).toEntity())
        }
    }

    override fun getCellEntries(
        taskIds: List<Long>,
        periodKeys: List<String>
    ): Flow<List<CellEntry>> {
        if (taskIds.isEmpty() || periodKeys.isEmpty()) {
            return flowOf(emptyList())
        }
        return cellEntryDao.getCellEntriesForTasksAndPeriods(taskIds, periodKeys).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun setCellStatus(
        taskId: Long,
        periodKey: String,
        status: CellStatus,
        note: String?
    ) {
        if ((status == CellStatus.EMPTY) && note.isNullOrBlank()) {
            cellEntryDao.deleteCellEntry(taskId, periodKey)
        } else {
            val entity = CellEntryEntity(
                taskId = taskId,
                periodKey = periodKey,
                status = status,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
            cellEntryDao.upsertCellEntry(entity)
        }
    }

    override suspend fun toggleCellStatus(
        taskId: Long,
        periodKey: String,
        currentStatus: CellStatus
    ) {
        val nextStatus = when (currentStatus) {
            CellStatus.EMPTY -> CellStatus.DONE
            CellStatus.DONE -> CellStatus.FAILED
            CellStatus.FAILED -> CellStatus.SKIPPED
            CellStatus.SKIPPED -> CellStatus.EMPTY
        }
        setCellStatus(taskId, periodKey, nextStatus)
    }
}
