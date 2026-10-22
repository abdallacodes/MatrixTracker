package com.tracker.matrixtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tracker.matrixtracker.data.local.entity.BoardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity): Long

    @Update
    suspend fun updateBoard(board: BoardEntity)

    @Delete
    suspend fun deleteBoard(board: BoardEntity)

    @Query("SELECT * FROM boards WHERE id = :id")
    suspend fun getBoardById(id: Long): BoardEntity?

    @Query("SELECT * FROM boards ORDER BY createdAt DESC")
    fun getBoards(): Flow<List<BoardEntity>>
}
