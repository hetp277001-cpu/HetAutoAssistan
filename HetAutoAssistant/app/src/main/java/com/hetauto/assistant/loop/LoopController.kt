package com.hetauto.assistant.loop

import android.util.Log
import com.hetauto.assistant.data.repository.HetAutomationRepository
import com.hetauto.assistant.service.HetAccessibilityService
import com.hetauto.assistant.workflow.StageExecutionResult
import com.hetauto.assistant.workflow.WorkflowDefinition
import com.hetauto.assistant.workflow.WorkflowEngine
import com.hetauto.assistant.workflow.WorkflowStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LoopController(
    private val repository: HetAutomationRepository,
    private val workflowEngine: WorkflowEngine
) {

    companion object {
        private const val TAG = "LoopController"
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var automationJob: Job? = null
    private var timerJob: Job? = null

    private val _automationState = MutableStateFlow(AutomationState.IDLE)
    val automationState: StateFlow<AutomationState> = _automationState.asStateFlow()

    private val _currentRepetition = MutableStateFlow(0)
    val currentRepetition: StateFlow<Int> = _currentRepetition.asStateFlow()

    private val _targetRepetitions = MutableStateFlow(1)
    val targetRepetitions: StateFlow<Int> = _targetRepetitions.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(false)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    private val _completedActions = MutableStateFlow(0)
    val completedActions: StateFlow<Int> = _completedActions.asStateFlow()

    private val _remainingActions = MutableStateFlow(0)
    val remainingActions: StateFlow<Int> = _remainingActions.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(0L)
    val sessionDurationSeconds: StateFlow<Long> = _sessionDurationSeconds.asStateFlow()

    private val _currentStage = MutableStateFlow<WorkflowStage?>(null)
    val currentStage: StateFlow<WorkflowStage?> = _currentStage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentSessionId: Long = 0
    private var sessionStartTime: Long = 0
    @Volatile private var isPaused = false

    fun start(workflow: WorkflowDefinition, targetReps: Int, continuous: Boolean) {
        if (_automationState.value == AutomationState.RUNNING) return

        val service = HetAccessibilityService.getInstance()
        if (service == null) {
            _errorMessage.value = "Accessibility Service is not active. Please enable it in Settings."
            _automationState.value = AutomationState.ERROR
            return
        }

        _targetRepetitions.value = if (continuous) Int.MAX_VALUE else targetReps
        _isContinuousMode.value = continuous
        _currentRepetition.value = 0
        _completedActions.value = 0
        val totalExpected = if (continuous) 0 else targetReps * workflow.stages.size
        _remainingActions.value = totalExpected
        _errorMessage.value = null
        _automationState.value = AutomationState.RUNNING
        isPaused = false
        sessionStartTime = System.currentTimeMillis()

        startTimer()

        automationJob = scope.launch {
            try {
                currentSessionId = repository.createSession(
                    workflowName = workflow.name,
                    targetReps = if (continuous) -1 else targetReps
                )

                runAutomationLoop(workflow, service)

            } catch (e: Exception) {
                Log.e(TAG, "Fatal loop error", e)
                _errorMessage.value = "Execution error: ${e.message}"
                _automationState.value = AutomationState.ERROR
                repository.finishSession(currentSessionId, "FAILED", e.message)
            } finally {
                stopTimer()
            }
        }
    }

    private suspend fun runAutomationLoop(workflow: WorkflowDefinition, service: HetAccessibilityService) {
        while (coroutineContext.isActive &&
            (_isContinuousMode.value || _currentRepetition.value < _targetRepetitions.value)
        ) {
            while (isPaused && coroutineContext.isActive) {
                delay(200)
            }
            if (!coroutineContext.isActive || _automationState.value == AutomationState.STOPPED) {
                break
            }

            val repIndex = _currentRepetition.value + 1
            _currentRepetition.value = repIndex

            for (stageDef in workflow.stages) {
                while (isPaused && coroutineContext.isActive) {
                    delay(200)
                }
                if (!coroutineContext.isActive || _automationState.value == AutomationState.STOPPED) {
                    break
                }

                _currentStage.value = stageDef.stage

                val stageResult = workflowEngine.executeStage(currentSessionId, stageDef, service)

                if (stageResult is StageExecutionResult.Success) {
                    _completedActions.value = _completedActions.value + 1
                    if (!_isContinuousMode.value && _remainingActions.value > 0) {
                        _remainingActions.value = _remainingActions.value - 1
                    }
                    repository.updateSessionProgress(
                        currentSessionId,
                        _currentRepetition.value,
                        _completedActions.value,
                        "RUNNING"
                    )
                } else if (stageResult is StageExecutionResult.Failure) {
                    _errorMessage.value = stageResult.reason
                }

                delay(500)
            }
        }

        if (_automationState.value == AutomationState.RUNNING) {
            _automationState.value = AutomationState.COMPLETED
            _currentStage.value = WorkflowStage.COMPLETION
            repository.finishSession(currentSessionId, "COMPLETED")
        }
    }

    fun pause() {
        if (_automationState.value == AutomationState.RUNNING) {
            isPaused = true
            _automationState.value = AutomationState.PAUSED
        }
    }

    fun resume() {
        if (_automationState.value == AutomationState.PAUSED) {
            isPaused = false
            _automationState.value = AutomationState.RUNNING
        }
    }

    fun stop() {
        isPaused = false
        _automationState.value = AutomationState.STOPPED
        automationJob?.cancel()
        automationJob = null
        stopTimer()
        _currentStage.value = null

        scope.launch {
            if (currentSessionId != 0L) {
                repository.finishSession(currentSessionId, "STOPPED")
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        _sessionDurationSeconds.value = 0L
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                if (_automationState.value == AutomationState.RUNNING) {
                    _sessionDurationSeconds.value = _sessionDurationSeconds.value + 1
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetState() {
        stop()
        _automationState.value = AutomationState.IDLE
        _currentRepetition.value = 0
        _completedActions.value = 0
        _remainingActions.value = 0
        _sessionDurationSeconds.value = 0L
        _currentStage.value = null
        _errorMessage.value = null
    }
}
