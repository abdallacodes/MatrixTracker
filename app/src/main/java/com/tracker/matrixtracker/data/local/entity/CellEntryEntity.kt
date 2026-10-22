package com.tracker.matrixtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tracker.matrixtracker.domain.model.CellStatus

@Entity(
    tableName = "cell_entries",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskId"),
        Index(value = ["taskId", "periodKey"], unique = true)
    ]
)
data class CellEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val periodKey: String,
    val status: CellStatus,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
