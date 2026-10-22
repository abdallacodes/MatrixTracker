package com.tracker.matrixtracker.domain.model

data class Task(
    val id: Long = 0,
    val boardId: Long,
    val title: String,
    val sortOrder: Int,
    val isActive: Boolean = true
)
