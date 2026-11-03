package com.hetauto.assistant.workflow

import android.util.Log
import com.hetauto.assistant.data.repository.HetAutomationRepository
import com.hetauto.assistant.recognition.ScreenRecognitionEngine
import com.hetauto.assistant.recognition.ScreenSnapshot
import com.hetauto.assistant.recognition.UiElementModel
import com.hetauto.assistant.screenshots.ScreenshotManager
import com.hetauto.assistant.service.HetAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkflowEngine(
    private val repository: HetAutomationRepository,
    private val recognitionEngine: ScreenRecognitionEngine,
    private val screenshotManager: ScreenshotManager
) {

    companion object {
        private const val TAG = "WorkflowEngine"
    }

    private val _currentStage = MutableStateFlow<WorkflowStage?>(null)
    val currentStage: StateFlow<WorkflowStage?> = _currentStage.asStateFlow()

    private val _lastMessage = MutableStateFlow<String>("Engine ready")
    val lastMessage: StateFlow<String> = _lastMessage.asStateFlow()

    suspend fun executeStage(
        sessionId: Long,
        definition: StageDefinition,
        service: HetAccessibilityService
    ): StageExecutionResult {
        _currentStage.value = definition.stage
        _lastMessage.value = "Executing ${definition.stageName}..."
        val startTime = System.currentTimeMillis()

        var attempts = 0
        while (attempts < definition.maxRetries) {
            attempts++
            try {
                val rootNode = service.getSafeRootInActiveWindow()
                val snapshot = recognitionEngine.analyzeCurrentScreen(rootNode)

                if (snapshot.isConfirmationDialogPresent && definition.stage != WorkflowStage.TEST_CONFIRMATION) {
                    Log.w(TAG, "Unexpected dialog detected during ${definition.stageName}. Attempting auto-dismiss.")
                    handleUnexpectedDialog(snapshot, service)
                    delay(500)
                }

                val actionResult = performStageAction(sessionId, definition, snapshot, service)

                if (actionResult.isSuccess) {
                    val duration = System.currentTimeMillis() - startTime
                    repository.logAction(
                        sessionId = sessionId,
                        stageName = definition.stageName,
                        actionType = definition.permittedAction.name,
                        targetElement = actionResult.targetElementDescription,
                        status = "SUCCESS",
                        durationMs = duration,
                        message = "Completed in attempt $attempts: ${actionResult.message}"
                    )
                    _lastMessage.value = "${definition.stageName} Succeeded"
                    return StageExecutionResult.Success(actionResult.message)
                } else {
                    Log.w(TAG, "Stage ${definition.stageName} attempt $attempts failed: ${actionResult.message}")
                    delay(800)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during stage ${definition.stageName} attempt $attempts", e)
                repository.logError(
                    sessionId = sessionId,
                    stageName = definition.stageName,
                    errorCode = "STAGE_EXECUTION_EXCEPTION",
                    errorMessage = e.message ?: "Unknown error",
                    stackTrace = e.stackTraceToString()
                )
            }
        }

        val failureDuration = System.currentTimeMillis() - startTime
        repository.logAction(
            sessionId = sessionId,
            stageName = definition.stageName,
            actionType = definition.permittedAction.name,
            targetElement = definition.stageName,
            status = "FAILURE",
            durationMs = failureDuration,
            message = "Max retries (${definition.maxRetries}) exceeded."
        )

        applyRecoveryStrategy(definition.errorRecoveryStrategy, service)
        _lastMessage.value = "${definition.stageName} Failed after ${definition.maxRetries} retries"
        return StageExecutionResult.Failure("Stage ${definition.stageName} failed after ${definition.maxRetries} attempts")
    }

    private suspend fun performStageAction(
        sessionId: Long,
        definition: StageDefinition,
        snapshot: ScreenSnapshot,
        service: HetAccessibilityService
    ): ActionResult {
        return when (definition.permittedAction) {
            PermittedAction.READ_AND_ASSERT_TEXT -> {
                val balance = recognitionEngine.extractBalance(snapshot)
                if (balance != null) {
                    ActionResult.Success("Target text verified: $balance", "Balance Text: $balance")
                } else {
                    val selector = definition.elementSelector
                    val el = if (selector != null) recognitionEngine.findElement(snapshot, selector) else null
                    if (el != null) {
                        ActionResult.Success("Verified element: ${el.label}", el.label)
                    } else {
                        ActionResult.Failure("Balance or target element not found on screen")
                    }
                }
            }

            PermittedAction.CLICK_ELEMENT -> {
                val selector = definition.elementSelector
                    ?: return ActionResult.Failure("ElementSelector not specified for CLICK")
                val element = recognitionEngine.findElement(snapshot, selector)
                    ?: return ActionResult.Failure("Element matching selector not found")

                val clicked = clickElement(element, service)
                if (clicked) {
                    delay(1000)
                    ActionResult.Success("Clicked element '${element.label}'", element.label)
                } else {
                    ActionResult.Failure("Failed to dispatch click on '${element.label}'")
                }
            }

            PermittedAction.SELECT_FOUR_BOX -> {
                val fourBoxes = snapshot.fourBoxElements
                if (fourBoxes.size >= 4) {
                    val targetIndex = definition.targetBoxIndex.coerceIn(0, fourBoxes.size - 1)
                    val targetBox = fourBoxes[targetIndex]
                    val clicked = clickElement(targetBox, service)
                    if (clicked) {
                        delay(800)
                        ActionResult.Success("Selected Four-Box Item #$targetIndex (${targetBox.label})", targetBox.label)
                    } else {
                        ActionResult.Failure("Failed to click Four-Box Item #$targetIndex")
                    }
                } else {
                    val fallback = snapshot.allElements.firstOrNull { it.label.lowercase().contains("box") && it.isClickable }
                    if (fallback != null) {
                        clickElement(fallback, service)
                        ActionResult.Success("Clicked Box element '${fallback.label}'", fallback.label)
                    } else {
                        ActionResult.Failure("Four-box layout not recognized. Found ${fourBoxes.size} boxes.")
                    }
                }
            }

            PermittedAction.HANDLE_CONFIRMATION_DIALOG -> {
                val selector = definition.elementSelector
                val confirmButton = if (selector != null) {
                    recognitionEngine.findElement(snapshot, selector)
                } else {
                    snapshot.allElements.firstOrNull {
                        val txt = it.label.lowercase()
                        (txt.contains("confirm") || txt.contains("ok") || txt.contains("yes") || txt.contains("proceed")) && it.isClickable
                    }
                }

                if (confirmButton != null) {
                    clickElement(confirmButton, service)
                    delay(1000)
                    ActionResult.Success("Confirmed action via '${confirmButton.label}'", confirmButton.label)
                } else {
                    ActionResult.Failure("Confirmation button not found")
                }
            }

            PermittedAction.TAKE_SCREENSHOT -> {
                val result = screenshotManager.captureAndSave(sessionId, definition.stageName)
                if (result.isSuccess) {
                    ActionResult.Success("Screenshot captured and saved to ${result.getOrNull()}", "Screen Capture")
                } else {
                    ActionResult.Failure("Screenshot failed: ${result.exceptionOrNull()?.message}")
                }
            }

            PermittedAction.SCROLL_FORWARD -> {
                var completed = false
                service.performScroll(forward = true) { completed = it }
                delay(800)
                ActionResult.Success("Scrolled forward", "Scroll Gesture")
            }

            PermittedAction.SCROLL_BACKWARD -> {
                var completed = false
                service.performScroll(forward = false) { completed = it }
                delay(800)
                ActionResult.Success("Scrolled backward", "Scroll Gesture")
            }

            PermittedAction.NAVIGATE_BACK -> {
                val backPerformed = service.performBackAction()
                delay(800)
                ActionResult.Success("Back navigation executed (result: $backPerformed)", "Global Action Back")
            }

            PermittedAction.IDLE_WAIT -> {
                delay(definition.timeoutMs.coerceAtMost(3000L))
                ActionResult.Success("Wait completed", "Wait")
            }
        }
    }

    private fun clickElement(element: UiElementModel, service: HetAccessibilityService): Boolean {
        service.clickCoordinates(element.centerX.toFloat(), element.centerY.toFloat())
        return true
    }

    private fun handleUnexpectedDialog(snapshot: ScreenSnapshot, service: HetAccessibilityService) {
        val dismissButton = snapshot.allElements.firstOrNull {
            val t = it.label.lowercase()
            (t.contains("cancel") || t.contains("close") || t.contains("dismiss") || t.contains("ok")) && it.isClickable
        }
        if (dismissButton != null) {
            clickElement(dismissButton, service)
        } else {
            service.performBackAction()
        }
    }

    private fun applyRecoveryStrategy(strategy: ErrorRecoveryStrategy, service: HetAccessibilityService) {
        when (strategy) {
            ErrorRecoveryStrategy.DISMISS_DIALOG_AND_RETRY,
            ErrorRecoveryStrategy.NAVIGATE_BACK_TO_DASHBOARD -> {
                service.performBackAction()
            }
            ErrorRecoveryStrategy.ABORT_SESSION -> {}
            ErrorRecoveryStrategy.RETRY_CURRENT_STAGE -> {}
        }
    }
}

sealed class StageExecutionResult {
    data class Success(val message: String) : StageExecutionResult()
    data class Failure(val reason: String) : StageExecutionResult()
}

sealed class ActionResult(val isSuccess: Boolean, val message: String, val targetElementDescription: String) {
    class Success(message: String, target: String) : ActionResult(true, message, target)
    class Failure(message: String) : ActionResult(false, message, "None")
}
