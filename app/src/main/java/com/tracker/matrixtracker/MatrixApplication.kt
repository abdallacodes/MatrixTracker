package com.tracker.matrixtracker

import android.app.Application
import com.tracker.matrixtracker.data.repository.SettingsRepository
import com.tracker.matrixtracker.util.AutoBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MatrixApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val settingsRepository = SettingsRepository(this)
        CoroutineScope(Dispatchers.Default).launch {
            val settings = settingsRepository.settingsFlow.first()
            AutoBackupWorker.scheduleOrCancel(
                context = applicationContext,
                enabled = settings.autoBackupEnabled,
                intervalDays = settings.autoBackupIntervalDays
            )
        }
    }
}
