package com.tracker.matrixtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tracker.matrixtracker.data.local.dao.BoardDao
import com.tracker.matrixtracker.data.local.dao.CellEntryDao
import com.tracker.matrixtracker.data.local.dao.TaskDao
import com.tracker.matrixtracker.data.local.entity.BoardEntity
import com.tracker.matrixtracker.data.local.entity.CellEntryEntity
import com.tracker.matrixtracker.data.local.entity.TaskEntity

@Database(
    entities = [
        BoardEntity::class,
        TaskEntity::class,
        CellEntryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun taskDao(): TaskDao
    abstract fun cellEntryDao(): CellEntryDao
}
