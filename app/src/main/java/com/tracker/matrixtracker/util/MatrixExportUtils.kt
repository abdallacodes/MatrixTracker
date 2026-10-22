package com.tracker.matrixtracker.util

import com.tracker.matrixtracker.domain.model.Board
import com.tracker.matrixtracker.domain.model.BoardCadence
import com.tracker.matrixtracker.domain.model.CellEntry
import com.tracker.matrixtracker.domain.model.CellStatus
import com.tracker.matrixtracker.domain.model.Task
import com.tracker.matrixtracker.ui.matrix.PeriodHeader
import org.json.JSONArray
import org.json.JSONObject

data class BackupDataBundle(
    val boards: List<Board>,
    val tasks: List<Task>,
    val entries: List<CellEntry>
)

object MatrixExportUtils {

    fun generateCsvForBoard(
        board: Board,
        tasks: List<Task>,
        periods: List<PeriodHeader>,
        cells: Map<Pair<Long, String>, CellEntry>
    ): String {
        val sb = StringBuilder()

        sb.append("\"Task / Habit\"")
        periods.forEach { p ->
            sb.append(",\"").append(p.label.replace("\"", "\"\"")).append("\"")
        }
        sb.append(",\"Completion %\"\n")

        tasks.forEach { task ->
            sb.append("\"").append(task.title.replace("\"", "\"\"")).append("\"")
            var doneCount = 0
            periods.forEach { period ->
                val entry = cells[Pair(task.id, period.key)]
                val symbol = when (entry?.status) {
                    CellStatus.DONE -> {
                        doneCount++
                        "V"
                    }
                    CellStatus.FAILED -> "X"
                    CellStatus.SKIPPED -> "-"
                    CellStatus.EMPTY, null -> ""
                }
                sb.append(",\"").append(symbol).append("\"")
            }

            val rowPercentage = if (periods.isNotEmpty()) {
                (doneCount.toFloat() / periods.size.toFloat() * 100f).toInt()
            } else 0
            sb.append(",\"").append(rowPercentage).append("%\"\n")
        }

        sb.append("\"Period Completion %\"")
        periods.forEach { period ->
            val totalTasks = tasks.size
            val doneCount = tasks.count { t ->
                cells[Pair(t.id, period.key)]?.status == CellStatus.DONE
            }
            val colPercentage = if (totalTasks > 0) {
                (doneCount.toFloat() / totalTasks.toFloat() * 100f).toInt()
            } else 0
            sb.append(",\"").append(colPercentage).append("%\"")
        }
        sb.append(",\"\"\n")

        return sb.toString()
    }

    fun exportFullBackupJson(
        boards: List<Board>,
        tasks: List<Task>,
        entries: List<CellEntry>
    ): String {
        val rootObj = JSONObject()
        rootObj.put("version", 1)

        val boardsArr = JSONArray()
        boards.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("title", b.title)
            bObj.put("cadence", b.cadence.name)
            bObj.put("createdAt", b.createdAt)
            boardsArr.put(bObj)
        }
        rootObj.put("boards", boardsArr)

        val tasksArr = JSONArray()
        tasks.forEach { t ->
            val tObj = JSONObject()
            tObj.put("id", t.id)
            tObj.put("boardId", t.boardId)
            tObj.put("title", t.title)
            tObj.put("sortOrder", t.sortOrder)
            tObj.put("isActive", t.isActive)
            tasksArr.put(tObj)
        }
        rootObj.put("tasks", tasksArr)

        val entriesArr = JSONArray()
        entries.forEach { e ->
            val eObj = JSONObject()
            eObj.put("id", e.id)
            eObj.put("taskId", e.taskId)
            eObj.put("periodKey", e.periodKey)
            eObj.put("status", e.status.name)
            eObj.put("note", e.note ?: JSONObject.NULL)
            eObj.put("updatedAt", e.updatedAt)
            entriesArr.put(eObj)
        }
        rootObj.put("entries", entriesArr)

        return rootObj.toString(2)
    }

    fun parseBackupJson(jsonString: String): BackupDataBundle? {
        return runCatching {
            val rootObj = JSONObject(jsonString)
            val boardsArr = rootObj.getJSONArray("boards")
            val boardsList = mutableListOf<Board>()
            for (i in 0 until boardsArr.length()) {
                val bObj = boardsArr.getJSONObject(i)
                boardsList.add(
                    Board(
                        id = bObj.getLong("id"),
                        title = bObj.getString("title"),
                        cadence = BoardCadence.valueOf(bObj.getString("cadence")),
                        createdAt = bObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val tasksArr = rootObj.getJSONArray("tasks")
            val tasksList = mutableListOf<Task>()
            for (i in 0 until tasksArr.length()) {
                val tObj = tasksArr.getJSONObject(i)
                tasksList.add(
                    Task(
                        id = tObj.getLong("id"),
                        boardId = tObj.getLong("boardId"),
                        title = tObj.getString("title"),
                        sortOrder = tObj.getInt("sortOrder"),
                        isActive = tObj.optBoolean("isActive", true)
                    )
                )
            }

            val entriesArr = rootObj.getJSONArray("entries")
            val entriesList = mutableListOf<CellEntry>()
            for (i in 0 until entriesArr.length()) {
                val eObj = entriesArr.getJSONObject(i)
                val noteVal = if (eObj.isNull("note")) null else eObj.getString("note")
                entriesList.add(
                    CellEntry(
                        id = eObj.optLong("id", 0),
                        taskId = eObj.getLong("taskId"),
                        periodKey = eObj.getString("periodKey"),
                        status = CellStatus.valueOf(eObj.getString("status")),
                        note = noteVal,
                        updatedAt = eObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            BackupDataBundle(
                boards = boardsList,
                tasks = tasksList,
                entries = entriesList
            )
        }.getOrNull()
    }
}
