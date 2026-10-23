package com.tracker.matrixtracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.LocaleListCompat
import androidx.room.Room
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.AppSettings
import com.tracker.matrixtracker.data.repository.SettingsRepository
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.ui.matrix.MatrixScreen
import com.tracker.matrixtracker.ui.matrix.MatrixViewModel
import com.tracker.matrixtracker.ui.theme.MatrixTrackerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "matrix_tracker.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

        val repository = TrackerRepositoryImpl(
            boardDao = database.boardDao(),
            taskDao = database.taskDao(),
            cellEntryDao = database.cellEntryDao()
        )
        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val settingsState by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
            val appLanguage = settingsState.appLanguage.ifBlank { "en" }

            LaunchedEffect(appLanguage) {
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                if (currentLocales.toLanguageTags() != appLanguage) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(appLanguage)
                    )
                }
            }

            val locale = remember(appLanguage) { Locale.forLanguageTag(appLanguage) }
            val currentConfig = LocalConfiguration.current
            val updatedConfig = remember(currentConfig, locale) {
                Configuration(currentConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }
            val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalConfiguration provides updatedConfig,
                LocalLayoutDirection provides layoutDirection
            ) {
                MatrixTrackerTheme(themeMode = settingsState.themeMode) {
                    val viewModel = remember { MatrixViewModel(repository, settingsRepository) }
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MatrixScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
