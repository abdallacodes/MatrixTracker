package com.tracker.matrixtracker.ui.matrix.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.BoardCadence
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PeriodNavigationBar(
    currentBoard: Board?,
    selectedYearMonth: YearMonth,
    selectedYear: Int,
    onOpenBoardSelection: () -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onGoToCurrent: () -> Unit,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodTitle = when (currentBoard?.cadence) {
        BoardCadence.DAILY -> selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
        BoardCadence.WEEKLY, BoardCadence.MONTHLY -> "$selectedYear"
        null -> ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onOpenBoardSelection
            ) {
                Text(
                    text = currentBoard?.title ?: "Select Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = onPreviousPeriod) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Period"
                )
            }

            Text(
                text = periodTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onNextPeriod) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Period"
                )
            }

            AssistChip(
                onClick = onGoToCurrent,
                label = { Text("Today", fontSize = 11.sp) },
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        IconButton(onClick = onAddNewTask) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
