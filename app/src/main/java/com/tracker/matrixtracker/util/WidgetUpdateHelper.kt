package com.tracker.matrixtracker.util

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.tracker.matrixtracker.ui.widget.MatrixWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateHelper {
    var disabledForTesting: Boolean = false
    private val scope = CoroutineScope(Dispatchers.IO)

    fun notifyWidgetDataChanged(context: Context) {
        if (disabledForTesting) return
        val appContext = context.applicationContext
        scope.launch {
            runCatching {
                MatrixWidget().updateAll(appContext)
            }
        }
    }
}
