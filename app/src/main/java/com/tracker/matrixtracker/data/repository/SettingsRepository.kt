package com.tracker.matrixtracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val autoBackupEnabled: Boolean = true,
    val autoBackupIntervalDays: Int = 1,
    val dragGestureEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
    val appLanguage: String = "SYSTEM"
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_INTERVAL_DAYS = intPreferencesKey("auto_backup_interval_days")
        val DRAG_GESTURE_ENABLED = booleanPreferencesKey("drag_gesture_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            autoBackupEnabled = prefs[Keys.AUTO_BACKUP_ENABLED] ?: true,
            autoBackupIntervalDays = prefs[Keys.AUTO_BACKUP_INTERVAL_DAYS] ?: 1,
            dragGestureEnabled = prefs[Keys.DRAG_GESTURE_ENABLED] ?: true,
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            appLanguage = prefs[Keys.APP_LANGUAGE] ?: "SYSTEM"
        )
    }

    suspend fun updateAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun updateAutoBackupIntervalDays(intervalDays: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_BACKUP_INTERVAL_DAYS] = intervalDays
        }
    }

    suspend fun updateDragGestureEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DRAG_GESTURE_ENABLED] = enabled
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun updateAppLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = language
        }
    }
}
