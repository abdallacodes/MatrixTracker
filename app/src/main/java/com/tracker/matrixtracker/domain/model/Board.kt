package com.tracker.matrixtracker.domain.model

data class Board(
    val id: Long = 0,
    val title: String,
    val cadence: BoardCadence,
    val createdAt: Long = System.currentTimeMillis()
)
