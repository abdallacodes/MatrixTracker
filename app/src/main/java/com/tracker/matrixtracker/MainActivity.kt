package com.tracker.matrixtracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.AppSettings
import com.tracker.matrixtracker.data.repository.SettingsRepository
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.ui.matrix.MatrixScreen
import com.tracker.matrixtracker.ui.matrix.MatrixViewModel
import com.tracker.matrixtracker.ui.theme.MatrixTrackerTheme
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)

        val repository = TrackerRepositoryImpl(
            boardDao = database.boardDao(),
            taskDao = database.taskDao(),
            cellEntryDao = database.cellEntryDao()
        )
        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val settingsState by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
            val appLanguage = settingsState.appLanguage.ifBlank { "en" }

            val localizedContext = remember(appLanguage) {
                val locale = Locale.forLanguageTag(appLanguage)
                Locale.setDefault(locale)
                val config = Configuration(resources.configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                createConfigurationContext(config)
            }

            val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalLayoutDirection provides layoutDirection,
                LocalActivityResultRegistryOwner provides this
            ) {
                MatrixTrackerTheme(themeMode = settingsState.themeMode) {
                    val viewModel = remember { MatrixViewModel(repository, settingsRepository) }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MatrixScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
