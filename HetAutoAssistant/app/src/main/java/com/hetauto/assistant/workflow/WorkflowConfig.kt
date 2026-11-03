package com.hetauto.assistant.workflow

import com.hetauto.assistant.recognition.ElementSelector

data class StageDefinition(
    val stage: WorkflowStage,
    val stageName: String,
    val entryConditionDescription: String,
    val expectedScreenName: String,
    val elementSelector: ElementSelector? = null,
    val permittedAction: PermittedAction,
    val targetBoxIndex: Int = 0,
    val completionConditionDescription: String,
    val timeoutMs: Long = 5000L,
    val maxRetries: Int = 3,
    val errorRecoveryStrategy: ErrorRecoveryStrategy = ErrorRecoveryStrategy.RETRY_CURRENT_STAGE
)

data class WorkflowDefinition(
    val id: String,
    val name: String,
    val description: String,
    val targetPackage: String,
    val stages: List<StageDefinition>
)
