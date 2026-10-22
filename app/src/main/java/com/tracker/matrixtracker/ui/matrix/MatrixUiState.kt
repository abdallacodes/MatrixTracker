package com.tracker.matrixtracker.ui.matrix

import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.Task
import java.time.YearMonth

data class PeriodHeader(
    val key: String,
    val label: String,
    val subLabel: String? = null,
    val isCurrent: Boolean = false,
)

data class MatrixUiState(
    val isLoading: Boolean = true,
    val currentBoard: Board? = null,
    val availableBoards: List<Board> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val periods: List<PeriodHeader> = emptyList(),
    val cells: Map<Pair<Long, String>, CellEntry> = emptyMap(),
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val selectedYear: Int = YearMonth.now().year,
    val taskCompletionPercentage: Map<Long, Float> = emptyMap(),
    val periodCompletionPercentage: Map<String, Float> = emptyMap(),
)
