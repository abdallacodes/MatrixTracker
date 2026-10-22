package com.tracker.matrixtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.ui.matrix.MatrixScreen
import com.tracker.matrixtracker.ui.matrix.MatrixViewModel
import com.tracker.matrixtracker.ui.theme.MatrixTrackerTheme

import androidx.compose.runtime.getValue
import com.tracker.matrixtracker.data.repository.AppSettings
import com.tracker.matrixtracker.data.repository.SettingsRepository
import androidx.compose.runtime.collectAsState

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
            val isDark = when (settingsState.themeMode) {
                "DARK", "AMOLED" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MatrixTrackerTheme(darkTheme = isDark) {
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
