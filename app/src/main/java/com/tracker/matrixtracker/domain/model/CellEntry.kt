package com.tracker.matrixtracker.domain.model

data class CellEntry(
    val id: Long = 0,
    val taskId: Long,
    val periodKey: String,
    val status: CellStatus,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
