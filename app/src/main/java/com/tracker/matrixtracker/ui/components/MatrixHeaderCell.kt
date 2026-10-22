package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.ui.matrix.PeriodHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatrixHeaderCell(
    header: PeriodHeader,
    modifier: Modifier = Modifier,
    onMarkAllDone: (() -> Unit)? = null,
    onClearAll: (() -> Unit)? = null,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val bgColor = if (header.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val borderColor = if (header.isCurrent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Box(contentAlignment = Alignment.Center) {
        Column(
            modifier = modifier
                .size(width = 48.dp, height = 48.dp)
                .background(bgColor)
                .border(
                    width = if (header.isCurrent) 1.5.dp else 0.5.dp,
                    color = borderColor
                )
                .then(
                    if (onMarkAllDone != null || onClearAll != null) {
                        Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { isMenuExpanded = true }
                        )
                    } else Modifier
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (header.isCurrent) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(16.dp)
                        .size(3.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }

            Text(
                text = header.label,
                fontSize = 11.sp,
                fontWeight = if (header.isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (header.isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (!header.subLabel.isNullOrBlank()) {
                Text(
                    text = header.subLabel,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            if (onMarkAllDone != null) {
                DropdownMenuItem(
                    text = { Text("Mark All Done") },
                    onClick = {
                        isMenuExpanded = false
                        onMarkAllDone()
                    }
                )
            }
            if (onClearAll != null) {
                DropdownMenuItem(
                    text = { Text("Clear All") },
                    onClick = {
                        isMenuExpanded = false
                        onClearAll()
                    }
                )
            }
        }
    }
}
