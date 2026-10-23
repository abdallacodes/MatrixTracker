package com.tracker.matrixtracker.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.matrixtracker.R
import com.tracker.matrixtracker.data.repository.AppSettings
import com.tracker.matrixtracker.ui.components.core.MatrixBottomSheet
import com.tracker.matrixtracker.ui.components.core.MatrixConfirmDialog
import com.tracker.matrixtracker.ui.components.core.SettingsRow

@OptIn(ExperimentalMaterial3Api::class)
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
    onOpenHelp: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showThemeSelectionSheet by remember { mutableStateOf(false) }
    var showLanguageSelectionSheet by remember { mutableStateOf(false) }
    var showIntervalSelectionSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val backupFile = remember(settings.lastBackupTime) {
        val targetDir = context.getExternalFilesDir(null) ?: context.filesDir
        java.io.File(targetDir, com.tracker.matrixtracker.util.AutoBackupWorker.BACKUP_FILE_NAME)
    }
    val fileExists = backupFile.exists()
    val lastModified = if (fileExists) backupFile.lastModified() else 0L
    val effectiveBackupTime = maxOf(settings.lastBackupTime, lastModified)
    val hasValidBackup = fileExists || effectiveBackupTime > 0L

    val backupStatusText = if (hasValidBackup) {
        val formattedDate = remember(effectiveBackupTime) {
            val instant = java.time.Instant.ofEpochMilli(effectiveBackupTime)
            val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
            zonedDateTime.format(
                java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a")
            )
        }
        stringResource(R.string.last_backup, formattedDate)
    } else {
        stringResource(R.string.no_backup_yet)
    }

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

    val themeSubtitle = when (settings.themeMode) {
        "LIGHT" -> stringResource(R.string.light_mode)
        "AMOLED" -> stringResource(R.string.amoled_accent)
        else -> stringResource(R.string.dark_mode)
    }

    val languageSubtitle = when (settings.appLanguage) {
        "ar" -> stringResource(R.string.arabic)
        else -> stringResource(R.string.english)
    }

    val intervalSubtitle = when (settings.autoBackupIntervalDays) {
        3 -> stringResource(R.string.every_3_days)
        7 -> stringResource(R.string.weekly_7d)
        else -> stringResource(R.string.daily_24h)
    }

    MatrixBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.settings),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Section 1: Help & Guide
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SettingsRow(
                    title = stringResource(R.string.how_to_use),
                    subtitle = "Learn core gestures, notes, and quick actions",
                    icon = Icons.Default.Settings,
                    onClick = onOpenHelp
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Section 2: Backup & Storage
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.backup_and_storage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                SettingsRow(
                    title = stringResource(R.string.enable_auto_backup),
                    subtitle = backupStatusText,
                    icon = Icons.Default.Refresh,
                    trailingContent = {
                        Switch(
                            checked = settings.autoBackupEnabled,
                            onCheckedChange = onUpdateAutoBackupEnabled
                        )
                    }
                )

                if (settings.autoBackupEnabled) {
                    SettingsRow(
                        title = stringResource(R.string.backup_interval),
                        subtitle = intervalSubtitle,
                        icon = Icons.Default.Settings,
                        onClick = { showIntervalSelectionSheet = true }
                    )
                }

                SettingsRow(
                    title = stringResource(R.string.backup_now),
                    subtitle = "Create a fresh backup file now",
                    icon = Icons.Default.Refresh,
                    onClick = onBackupNow
                )

                SettingsRow(
                    title = stringResource(R.string.export_backup_file),
                    subtitle = "Save backup JSON file to storage",
                    icon = Icons.Default.Share,
                    onClick = { exportLauncher.launch("MatrixTracker_Backup_$todayStr.json") }
                )

                SettingsRow(
                    title = stringResource(R.string.restore_backup_file),
                    subtitle = "Import habits data from JSON backup",
                    icon = Icons.Default.Share,
                    onClick = { restoreLauncher.launch(arrayOf("application/json", "*/*")) }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Section 3: Appearance & Theme
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.appearance_and_theme),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                SettingsRow(
                    title = stringResource(R.string.theme_mode),
                    subtitle = themeSubtitle,
                    icon = Icons.Default.Settings,
                    onClick = { showThemeSelectionSheet = true }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Section 4: Language & Localization
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.language_and_locale),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                SettingsRow(
                    title = stringResource(R.string.language),
                    subtitle = languageSubtitle,
                    icon = Icons.Default.Settings,
                    onClick = { showLanguageSelectionSheet = true }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Section 5: Gestures & Controls
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.gestures),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                SettingsRow(
                    title = stringResource(R.string.enable_drag_gesture),
                    subtitle = "Multi-cell horizontal drag marking",
                    icon = Icons.Default.Settings,
                    trailingContent = {
                        Switch(
                            checked = settings.dragGestureEnabled,
                            onCheckedChange = onUpdateDragGestureEnabled
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showThemeSelectionSheet) {
        MatrixBottomSheet(
            onDismissRequest = { showThemeSelectionSheet = false },
            title = stringResource(R.string.theme_mode)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "DARK" to stringResource(R.string.dark_mode),
                    "LIGHT" to stringResource(R.string.light_mode),
                    "AMOLED" to stringResource(R.string.amoled_accent)
                ).forEach { (modeKey, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdateThemeMode(modeKey)
                                showThemeSelectionSheet = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        RadioButton(
                            selected = (settings.themeMode == modeKey),
                            onClick = {
                                onUpdateThemeMode(modeKey)
                                showThemeSelectionSheet = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showLanguageSelectionSheet) {
        MatrixBottomSheet(
            onDismissRequest = { showLanguageSelectionSheet = false },
            title = stringResource(R.string.language)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "en" to stringResource(R.string.english),
                    "ar" to stringResource(R.string.arabic)
                ).forEach { (langKey, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdateAppLanguage(langKey)
                                showLanguageSelectionSheet = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        RadioButton(
                            selected = (settings.appLanguage == langKey),
                            onClick = {
                                onUpdateAppLanguage(langKey)
                                showLanguageSelectionSheet = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showIntervalSelectionSheet) {
        MatrixBottomSheet(
            onDismissRequest = { showIntervalSelectionSheet = false },
            title = stringResource(R.string.backup_interval)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    1 to stringResource(R.string.daily_24h),
                    3 to stringResource(R.string.every_3_days),
                    7 to stringResource(R.string.weekly_7d)
                ).forEach { (days, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdateAutoBackupIntervalDays(days)
                                showIntervalSelectionSheet = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        RadioButton(
                            selected = (settings.autoBackupIntervalDays == days),
                            onClick = {
                                onUpdateAutoBackupIntervalDays(days)
                                showIntervalSelectionSheet = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    pendingRestoreUri?.let { uri ->
        MatrixConfirmDialog(
            title = stringResource(R.string.restore_confirmation),
            message = stringResource(R.string.restore_warning),
            confirmLabel = stringResource(R.string.restore_backup_file),
            onConfirm = {
                onRestoreBackupUri(uri)
                pendingRestoreUri = null
            },
            onDismiss = { pendingRestoreUri = null },
            isDestructive = false
        )
    }
}
