package com.hetauto.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hetauto.assistant.data.entity.ActionLogEntity
import com.hetauto.assistant.data.entity.ScreenshotEntity
import com.hetauto.assistant.data.entity.TestSessionEntity
import com.hetauto.assistant.data.repository.HetAutomationRepository
import com.hetauto.assistant.loop.AutomationState
import com.hetauto.assistant.loop.LoopController
import com.hetauto.assistant.screenshots.ScreenshotManager
import com.hetauto.assistant.service.HetAccessibilityService
import com.hetauto.assistant.workflow.DefaultFourBoxWorkflow
import com.hetauto.assistant.workflow.WorkflowStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: HetAutomationRepository,
    private val loopController: LoopController,
    private val screenshotManager: ScreenshotManager
) : ViewModel() {

    val automationState: StateFlow<AutomationState> = loopController.automationState
    val currentRepetition: StateFlow<Int> = loopController.currentRepetition
    val targetRepetitions: StateFlow<Int> = loopController.targetRepetitions
    val isContinuousMode: StateFlow<Boolean> = loopController.isContinuousMode
    val completedActions: StateFlow<Int> = loopController.completedActions
    val remainingActions: StateFlow<Int> = loopController.remainingActions
    val currentStage: StateFlow<WorkflowStage?> = loopController.currentStage
    val errorMessage: StateFlow<String?> = loopController.errorMessage

    val sessionDurationFormatted: StateFlow<String> = loopController.sessionDurationSeconds
        .map { totalSeconds ->
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00")

    val isAccessibilityActive: StateFlow<Boolean> = HetAccessibilityService.isServiceConnected

    val recentLogs: StateFlow<List<ActionLogEntity>> = repository.getRecentActionLogs(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<TestSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScreenshots: StateFlow<List<ScreenshotEntity>> = repository.allScreenshots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReps = MutableStateFlow(5)
    val selectedReps: StateFlow<Int> = _selectedReps.asStateFlow()

    private val _continuousToggle = MutableStateFlow(false)
    val continuousToggle: StateFlow<Boolean> = _continuousToggle.asStateFlow()

    fun setSelectedReps(reps: Int) {
        _selectedReps.value = reps
    }

    fun setContinuousMode(continuous: Boolean) {
        _continuousToggle.value = continuous
    }

    fun startAutomation() {
        val workflow = DefaultFourBoxWorkflow.create()
        loopController.start(
            workflow = workflow,
            targetReps = _selectedReps.value,
            continuous = _continuousToggle.value
        )
    }

    fun pauseAutomation() {
        loopController.pause()
    }

    fun resumeAutomation() {
        loopController.resume()
    }

    fun stopAutomation() {
        loopController.stop()
    }

    fun deleteScreenshot(id: Long, path: String) {
        viewModelScope.launch {
            screenshotManager.deleteScreenshot(id, path)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            loopController.resetState()
        }
    }
}
