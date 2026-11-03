package com.hetauto.assistant.workflow

import com.hetauto.assistant.recognition.ElementSelector
import com.hetauto.assistant.recognition.ElementType

object DefaultFourBoxWorkflow {

    const val WORKFLOW_ID = "default_four_box_test"
    const val WORKFLOW_NAME = "Demo Four-Box Automated Test"
    const val DEFAULT_TARGET_PACKAGE = "com.hetauto.assistant"

    fun create(): WorkflowDefinition {
        return WorkflowDefinition(
            id = WORKFLOW_ID,
            name = WORKFLOW_NAME,
            description = "Standard 6-stage automated workflow for authorized test screens with 4-box layouts, balance validation, and confirmation dialogs.",
            targetPackage = DEFAULT_TARGET_PACKAGE,
            stages = listOf(
                StageDefinition(
                    stage = WorkflowStage.INITIAL_SCREEN,
                    stageName = "State 1: Initial Screen & Balance Check",
                    entryConditionDescription = "Verify authorized test app is in foreground and dashboard is displayed",
                    expectedScreenName = "Test Dashboard",
                    elementSelector = ElementSelector(
                        textContains = "balance",
                        targetType = ElementType.TEXT_VIEW
                    ),
                    permittedAction = PermittedAction.READ_AND_ASSERT_TEXT,
                    completionConditionDescription = "Valid test balance value read successfully",
                    timeoutMs = 6000L,
                    maxRetries = 3,
                    errorRecoveryStrategy = ErrorRecoveryStrategy.RETRY_CURRENT_STAGE
                ),
                StageDefinition(
                    stage = WorkflowStage.MENU_NAVIGATION,
                    stageName = "State 2: Menu Navigation",
                    entryConditionDescription = "Dashboard active; locate test menu entry",
                    expectedScreenName = "Menu / Navigation View",
                    elementSelector = ElementSelector(
                        textContains = "Four-Box Test",
                        isClickableOnly = true
                    ),
                    permittedAction = PermittedAction.CLICK_ELEMENT,
                    completionConditionDescription = "Navigated to Four-Box Test sandbox view",
                    timeoutMs = 5000L,
                    maxRetries = 3,
                    errorRecoveryStrategy = ErrorRecoveryStrategy.DISMISS_DIALOG_AND_RETRY
                ),
                StageDefinition(
                    stage = WorkflowStage.DEMO_FOUR_BOX_TEST,
                    stageName = "State 3: Four-Box Selection & Verification",
                    entryConditionDescription = "Four interactive boxes present on screen",
                    expectedScreenName = "Four-Box Grid View",
                    elementSelector = null,
                    permittedAction = PermittedAction.SELECT_FOUR_BOX,
                    targetBoxIndex = 0,
                    completionConditionDescription = "Target test box clicked and updated state recorded",
                    timeoutMs = 7000L,
                    maxRetries = 3,
                    errorRecoveryStrategy = ErrorRecoveryStrategy.RETRY_CURRENT_STAGE
                ),
                StageDefinition(
                    stage = WorkflowStage.TEST_CONFIRMATION,
                    stageName = "State 4: Test Confirmation",
                    entryConditionDescription = "Confirmation dialog or submit prompt appears",
                    expectedScreenName = "Confirmation Dialog",
                    elementSelector = ElementSelector(
                        textRegex = "(?i)(confirm|proceed|ok|submit|yes)",
                        isClickableOnly = true
                    ),
                    permittedAction = PermittedAction.HANDLE_CONFIRMATION_DIALOG,
                    completionConditionDescription = "Confirmation confirmed and verification state recorded",
                    timeoutMs = 6000L,
                    maxRetries = 2,
                    errorRecoveryStrategy = ErrorRecoveryStrategy.DISMISS_DIALOG_AND_RETRY
                ),
                StageDefinition(
                    stage = WorkflowStage.SCREENSHOT,
                    stageName = "State 5: Screenshot Capture",
                    entryConditionDescription = "Test outcome displayed on screen",
                    expectedScreenName = "Post-Test Outcome View",
                    elementSelector = null,
                    permittedAction = PermittedAction.TAKE_SCREENSHOT,
                    completionConditionDescription = "Screenshot saved locally with session metadata",
                    timeoutMs = 5000L,
                    maxRetries = 2,
                    errorRecoveryStrategy = ErrorRecoveryStrategy.RETRY_CURRENT_STAGE
                ),
                StageDefinition(
                    stage = WorkflowStage.COMPLETION,
                    stageName = "State 6: Completion & Next Iteration",
                    entryConditionDescription = "All test actions in cycle executed",
                    expectedScreenName = "Cycle Finalized",
                    elementSelector = null,
                    permittedAction = PermittedAction.NAVIGATE_BACK,
                    completionConditionDescription = "Counter incremented and returned to dashboard if next cycle scheduled",
                    timeoutMs = 4000L,
                    maxRetries = 2,
                    errorRecoveryStrategy = ErrorRecoveryStrategy.NAVIGATE_BACK_TO_DASHBOARD
                )
            )
        )
    }
}
