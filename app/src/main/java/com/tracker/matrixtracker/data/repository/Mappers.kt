package com.tracker.matrixtracker.data.repository

import com.tracker.matrixtracker.data.local.entity.BoardEntity
import com.tracker.matrixtracker.data.local.entity.CellEntryEntity
import com.tracker.matrixtracker.data.local.entity.TaskEntity
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.Task

fun BoardEntity.toDomain(): Board = Board(
    id = id,
    title = title,
    cadence = cadence,
    createdAt = createdAt
)

fun Board.toEntity(): BoardEntity = BoardEntity(
    id = id,
    title = title,
    cadence = cadence,
    createdAt = createdAt
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    boardId = boardId,
    title = title,
    sortOrder = sortOrder,
    isActive = isActive
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    boardId = boardId,
    title = title,
    sortOrder = sortOrder,
    isActive = isActive
)

fun CellEntryEntity.toDomain(): CellEntry = CellEntry(
    id = id,
    taskId = taskId,
    periodKey = periodKey,
    status = status,
    note = note,
    updatedAt = updatedAt
)

fun CellEntry.toEntity(): CellEntryEntity = CellEntryEntity(
    id = id,
    taskId = taskId,
    periodKey = periodKey,
    status = status,
    note = note,
    updatedAt = updatedAt
)
