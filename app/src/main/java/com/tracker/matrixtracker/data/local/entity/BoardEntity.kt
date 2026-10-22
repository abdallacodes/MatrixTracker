package com.tracker.matrixtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tracker.matrixtracker.domain.model.BoardCadence

@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val cadence: BoardCadence,
    val createdAt: Long = System.currentTimeMillis()
)
