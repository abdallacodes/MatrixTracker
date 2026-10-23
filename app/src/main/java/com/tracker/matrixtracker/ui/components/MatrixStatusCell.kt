package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.ui.theme.LocalThemeMode

@Composable
fun MatrixStatusCell(
    entry: CellEntry?,
    onToggle: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val themeMode = LocalThemeMode.current
    val status = entry?.status ?: CellStatus.EMPTY
    val hasNote = !entry?.note.isNullOrBlank()

    val (bgColor, borderColor, textColor, textSymbol) = when (status) {
        CellStatus.EMPTY -> when (themeMode) {
            "AMOLED" -> Quadruple(
                Color(0xFF000000),
                Color(0xFF00FF87).copy(alpha = 0.25f),
                Color(0xFF00FF87).copy(alpha = 0.3f),
                ""
            )
            "LIGHT" -> Quadruple(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                ""
            )
            else -> Quadruple(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                ""
            )
        }
        CellStatus.DONE -> when (themeMode) {
            "AMOLED" -> Quadruple(
                Color(0xFF002214),
                Color(0xFF00FF87),
                Color(0xFF00FF87),
                "✓"
            )
            "LIGHT" -> Quadruple(
                Color(0xFFE8F5E9),
                Color(0xFFA5D6A7),
                Color(0xFF2E7D32),
                "✓"
            )
            else -> Quadruple(
                Color(0xFF133820),
                Color(0xFF2E7D32).copy(alpha = 0.5f),
                Color(0xFF81C784),
                "✓"
            )
        }
        CellStatus.FAILED -> when (themeMode) {
            "AMOLED" -> Quadruple(
                Color(0xFF2B0008),
                Color(0xFFFF1744),
                Color(0xFFFF5252),
                "✕"
            )
            "LIGHT" -> Quadruple(
                Color(0xFFFFEBEE),
                Color(0xFFEF9A9A),
                Color(0xFFC62828),
                "✕"
            )
            else -> Quadruple(
                Color(0xFF381515),
                Color(0xFFC62828).copy(alpha = 0.5f),
                Color(0xFFE57373),
                "✕"
            )
        }
        CellStatus.SKIPPED -> when (themeMode) {
            "AMOLED" -> Quadruple(
                Color(0xFF001F2D),
                Color(0xFF00E5FF).copy(alpha = 0.6f),
                Color(0xFF80D8FF),
                "—"
            )
            "LIGHT" -> Quadruple(
                Color(0xFFECEFF1),
                Color(0xFFCFD8DC),
                Color(0xFF607D8B),
                "—"
            )
            else -> Quadruple(
                Color(0xFF1E2836),
                Color(0xFF37474F).copy(alpha = 0.5f),
                Color(0xFF90A4AE),
                "—"
            )
        }
    }

    Box(
        modifier = modifier
            .size(width = 48.dp, height = 44.dp)
            .background(bgColor)
            .border(0.5.dp, borderColor)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (textSymbol.isNotEmpty()) {
            Text(
                text = textSymbol,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (hasNote) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
