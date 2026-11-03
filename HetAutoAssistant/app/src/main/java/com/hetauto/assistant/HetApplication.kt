package com.hetauto.assistant

import android.app.Application
import com.hetauto.assistant.data.HetDatabase
import com.hetauto.assistant.data.entity.WorkflowEntity
import com.hetauto.assistant.data.repository.HetAutomationRepository
import com.hetauto.assistant.loop.LoopController
import com.hetauto.assistant.recognition.ScreenRecognitionEngine
import com.hetauto.assistant.recognition.VisionRecognitionEngine
import com.hetauto.assistant.screenshots.ScreenshotManager
import com.hetauto.assistant.workflow.DefaultFourBoxWorkflow
import com.hetauto.assistant.workflow.WorkflowEngine
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HetApplication : Application() {

    lateinit var database: HetDatabase
        private set

    lateinit var repository: HetAutomationRepository
        private set

    lateinit var recognitionEngine: ScreenRecognitionEngine
        private set

    lateinit var screenshotManager: ScreenshotManager
        private set

    lateinit var workflowEngine: WorkflowEngine
        private set

    lateinit var loopController: LoopController
        private set

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        database = HetDatabase.getInstance(this)
        repository = HetAutomationRepository(database)
        recognitionEngine = ScreenRecognitionEngine(VisionRecognitionEngine())
        screenshotManager = ScreenshotManager(this, repository)
        workflowEngine = WorkflowEngine(repository, recognitionEngine, screenshotManager)
        loopController = LoopController(repository, workflowEngine)

        applicationScope.launch {
            val existing = repository.getDefaultWorkflow()
            if (existing == null) {
                val defaultWorkflow = DefaultFourBoxWorkflow.create()
                val entity = WorkflowEntity(
                    id = defaultWorkflow.id,
                    name = defaultWorkflow.name,
                    description = defaultWorkflow.description,
                    targetPackage = defaultWorkflow.targetPackage,
                    stagesJson = Gson().toJson(defaultWorkflow.stages),
                    createdAt = System.currentTimeMillis(),
                    isDefault = true
                )
                repository.saveWorkflow(entity)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        recognitionEngine.cleanup()
    }
}
