package com.tracker.matrixtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.ui.components.core.MatrixBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    MatrixBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.how_to_use),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Feature 1: Single Tap
            HelpItemRow(
                badgeText = "👆",
                title = stringResource(R.string.guide_single_tap_title),
                description = stringResource(R.string.guide_single_tap_desc)
            )

            // Feature 2: Long Press
            HelpItemRow(
                badgeText = "👆+",
                title = stringResource(R.string.guide_long_press_title),
                description = stringResource(R.string.guide_long_press_desc)
            )

            // Feature 3: Horizontal Drag
            HelpItemRow(
                badgeText = "↔️",
                title = stringResource(R.string.guide_swipe_title),
                description = stringResource(R.string.guide_swipe_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HelpItemRow(
    badgeText: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        ) {
            Text(
                text = badgeText,
                fontSize = 18.sp
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
