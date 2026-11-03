package com.hetauto.assistant.data.repository

import com.hetauto.assistant.data.HetDatabase
import com.hetauto.assistant.data.entity.ActionLogEntity
import com.hetauto.assistant.data.entity.ErrorLogEntity
import com.hetauto.assistant.data.entity.ScreenshotEntity
import com.hetauto.assistant.data.entity.TestSessionEntity
import com.hetauto.assistant.data.entity.WorkflowEntity
import kotlinx.coroutines.flow.Flow

class HetAutomationRepository(private val database: HetDatabase) {

    val allSessions: Flow<List<TestSessionEntity>> = database.testSessionDao().getAllSessions()

    suspend fun createSession(workflowName: String, targetReps: Int): Long {
        val session = TestSessionEntity(
            workflowName = workflowName,
            startTime = System.currentTimeMillis(),
            targetRepetitions = targetReps,
            completedRepetitions = 0,
            totalActions = 0,
            status = "RUNNING"
        )
        return database.testSessionDao().insertSession(session)
    }

    suspend fun updateSessionProgress(sessionId: Long, completedReps: Int, totalActions: Int, status: String) {
        val current = database.testSessionDao().getSessionById(sessionId) ?: return
        val updated = current.copy(
            completedRepetitions = completedReps,
            totalActions = totalActions,
            status = status
        )
        database.testSessionDao().updateSession(updated)
    }

    suspend fun finishSession(sessionId: Long, status: String, failureReason: String? = null) {
        val current = database.testSessionDao().getSessionById(sessionId) ?: return
        val updated = current.copy(
            endTime = System.currentTimeMillis(),
            status = status,
            failureReason = failureReason
        )
        database.testSessionDao().updateSession(updated)
    }

    suspend fun deleteSession(sessionId: Long) {
        database.testSessionDao().deleteSession(sessionId)
        database.actionLogDao().deleteLogsForSession(sessionId)
        database.screenshotDao().deleteScreenshotsForSession(sessionId)
    }

    fun getRecentActionLogs(limit: Int = 100): Flow<List<ActionLogEntity>> =
        database.actionLogDao().getRecentLogs(limit)

    suspend fun logAction(
        sessionId: Long,
        stageName: String,
        actionType: String,
        targetElement: String,
        status: String,
        durationMs: Long,
        message: String? = null
    ): Long {
        val log = ActionLogEntity(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            stageName = stageName,
            actionType = actionType,
            targetElementDescription = targetElement,
            status = status,
            durationMs = durationMs,
            message = message
        )
        return database.actionLogDao().insertLog(log)
    }

    val allScreenshots: Flow<List<ScreenshotEntity>> = database.screenshotDao().getAllScreenshots()

    suspend fun recordScreenshot(
        sessionId: Long,
        filePath: String,
        stageName: String,
        width: Int,
        height: Int,
        fileSize: Long,
        note: String? = null
    ): Long {
        val entity = ScreenshotEntity(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            filePath = filePath,
            stageName = stageName,
            width = width,
            height = height,
            fileSize = fileSize,
            note = note
        )
        return database.screenshotDao().insertScreenshot(entity)
    }

    suspend fun deleteScreenshot(id: Long) {
        database.screenshotDao().deleteScreenshot(id)
    }

    suspend fun logError(
        sessionId: Long?,
        stageName: String?,
        errorCode: String,
        errorMessage: String,
        stackTrace: String? = null,
        screenshotPath: String? = null
    ): Long {
        val error = ErrorLogEntity(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            stageName = stageName,
            errorCode = errorCode,
            errorMessage = errorMessage,
            stackTrace = stackTrace,
            screenshotPath = screenshotPath
        )
        return database.errorLogDao().insertError(error)
    }

    suspend fun saveWorkflow(workflow: WorkflowEntity) {
        database.workflowDao().insertWorkflow(workflow)
    }

    suspend fun getDefaultWorkflow(): WorkflowEntity? {
        return database.workflowDao().getDefaultWorkflow()
    }

    suspend fun clearHistory() {
        database.testSessionDao().clearAll()
        database.actionLogDao().clearAll()
        database.screenshotDao().clearAll()
        database.errorLogDao().clearAll()
    }
}
