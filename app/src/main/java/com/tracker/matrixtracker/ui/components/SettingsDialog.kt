package com.tracker.matrixtracker.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.data.repository.AppSettings

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onUpdateAutoBackupEnabled: (Boolean) -> Unit,
    onUpdateAutoBackupIntervalDays: (Int) -> Unit,
    onUpdateDragGestureEnabled: (Boolean) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateAppLanguage: (String) -> Unit,
    onBackupNow: () -> Unit,
    onExportBackupUri: (Uri) -> Unit,
    onRestoreBackupUri: (Uri) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val todayStr = remember {
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { onExportBackupUri(it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { pendingRestoreUri = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.backup_and_storage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.enable_auto_backup),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = settings.autoBackupEnabled,
                            onCheckedChange = onUpdateAutoBackupEnabled
                        )
                    }

                    if (settings.autoBackupEnabled) {
                        Text(
                            text = stringResource(R.string.backup_interval),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = (settings.autoBackupIntervalDays == 1),
                                onClick = { onUpdateAutoBackupIntervalDays(1) },
                                label = { Text(stringResource(R.string.daily_24h), fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = (settings.autoBackupIntervalDays == 3),
                                onClick = { onUpdateAutoBackupIntervalDays(3) },
                                label = { Text(stringResource(R.string.every_3_days), fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = (settings.autoBackupIntervalDays == 7),
                                onClick = { onUpdateAutoBackupIntervalDays(7) },
                                label = { Text(stringResource(R.string.weekly_7d), fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onBackupNow,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text(stringResource(R.string.backup_now), fontSize = 12.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("MatrixTracker_Backup_$todayStr.json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.export_backup_file), fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { restoreLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.restore_backup_file), fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.gestures),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.enable_drag_gesture),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = settings.dragGestureEnabled,
                            onCheckedChange = onUpdateDragGestureEnabled
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.appearance_and_theme),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "SYSTEM" to stringResource(R.string.system_default),
                            "DARK" to stringResource(R.string.dark_mode),
                            "LIGHT" to stringResource(R.string.light_mode),
                            "AMOLED" to stringResource(R.string.amoled_accent)
                        ).forEach { (modeKey, label) ->
                            FilterChip(
                                selected = (settings.themeMode == modeKey),
                                onClick = { onUpdateThemeMode(modeKey) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.language_and_locale),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "SYSTEM" to stringResource(R.string.system_default),
                            "en" to stringResource(R.string.english),
                            "ar" to stringResource(R.string.arabic)
                        ).forEach { (langKey, label) ->
                            FilterChip(
                                selected = (settings.appLanguage == langKey),
                                onClick = { onUpdateAppLanguage(langKey) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        },
        modifier = modifier
    )

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text(stringResource(R.string.restore_confirmation)) },
            text = { Text(stringResource(R.string.restore_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreBackupUri(uri)
                        pendingRestoreUri = null
                    }
                ) {
                    Text(stringResource(R.string.restore_backup_file))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
