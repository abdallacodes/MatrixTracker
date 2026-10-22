package com.tracker.matrixtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tracker.matrixtracker.data.local.entity.CellEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CellEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCellEntry(entry: CellEntryEntity): Long

    @Query("SELECT * FROM cell_entries WHERE taskId IN (:taskIds) AND periodKey IN (:periodKeys)")
    fun getCellEntriesForTasksAndPeriods(
        taskIds: List<Long>,
        periodKeys: List<String>
    ): Flow<List<CellEntryEntity>>

    @Query("SELECT * FROM cell_entries")
    fun getAllCellEntries(): Flow<List<CellEntryEntity>>

    @Query("DELETE FROM cell_entries WHERE taskId = :taskId AND periodKey = :periodKey")
    suspend fun deleteCellEntry(taskId: Long, periodKey: String)
}
