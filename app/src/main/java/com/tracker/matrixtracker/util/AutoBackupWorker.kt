package com.tracker.matrixtracker.util

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tracker.matrixtracker.data.local.AppDatabase
import com.tracker.matrixtracker.data.repository.SettingsRepository
import com.tracker.matrixtracker.data.repository.TrackerRepositoryImpl
import com.tracker.matrixtracker.domain.model.Task
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "matrix_tracker.db"
            ).fallbackToDestructiveMigration(dropAllTables = true).build()

            val repository = TrackerRepositoryImpl(
                boardDao = db.boardDao(),
                taskDao = db.taskDao(),
                cellEntryDao = db.cellEntryDao()
            )

            val boards = repository.getBoards().first()
            val allTasks = mutableListOf<Task>()
            boards.forEach { b ->
                allTasks.addAll(repository.getTasksForBoard(b.id).first())
            }
            val allEntries = repository.getAllCellEntries().first()

            val jsonText = MatrixExportUtils.exportFullBackupJson(boards, allTasks, allEntries)

            val targetDir = context.getExternalFilesDir(null) ?: context.filesDir
            val backupFile = File(targetDir, BACKUP_FILE_NAME)
            backupFile.writeText(jsonText)

            val settingsRepository = SettingsRepository(context.applicationContext)
            settingsRepository.updateLastBackupTime(System.currentTimeMillis())

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val BACKUP_FILE_NAME = "matrixtracker_auto_backup.json"
        const val WORK_NAME = "MatrixTrackerAutoBackupWork"

        fun scheduleOrCancel(context: Context, enabled: Boolean, intervalDays: Int) {
            val workManager = WorkManager.getInstance(context)
            if (!enabled) {
                workManager.cancelUniqueWork(WORK_NAME)
            } else {
                val repeatIntervalHours = (intervalDays.coerceAtLeast(1) * 24).toLong()
                val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                    repeatIntervalHours,
                    TimeUnit.HOURS
                ).build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            }
        }
    }
}
