package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.ui.matrix.PeriodHeader

@Composable
fun MatrixHeaderCell(
    header: PeriodHeader,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (header.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val borderColor = if (header.isCurrent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Column(
        modifier = modifier
            .size(width = 48.dp, height = 48.dp)
            .background(bgColor)
            .border(
                width = if (header.isCurrent) 1.5.dp else 0.5.dp,
                color = borderColor
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
}
