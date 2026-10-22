package com.tracker.matrixtracker.data.local

import androidx.room.TypeConverter
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellStatus

class Converters {
    @TypeConverter
    fun fromBoardCadence(cadence: BoardCadence?): String? {
        return cadence?.name
    }

    @TypeConverter
    fun toBoardCadence(value: String?): BoardCadence? {
        return value?.let {
            runCatching { BoardCadence.valueOf(it) }.getOrNull()
        }
    }

    @TypeConverter
    fun fromCellStatus(status: CellStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toCellStatus(value: String?): CellStatus? {
        return value?.let {
            runCatching { CellStatus.valueOf(it) }.getOrNull()
        }
    }
}
