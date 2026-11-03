package com.hetauto.assistant.workflow

enum class WorkflowStage(val displayName: String) {
    INITIAL_SCREEN("State 1: Initial Screen"),
    MENU_NAVIGATION("State 2: Menu Navigation"),
    DEMO_FOUR_BOX_TEST("State 3: Demo Four-Box Test"),
    TEST_CONFIRMATION("State 4: Test Confirmation"),
    SCREENSHOT("State 5: Screenshot Capture"),
    COMPLETION("State 6: Completion"),
    ERROR_RECOVERY("Error Recovery")
}

enum class PermittedAction {
    CLICK_ELEMENT,
    SCROLL_FORWARD,
    SCROLL_BACKWARD,
    READ_AND_ASSERT_TEXT,
    SELECT_FOUR_BOX,
    HANDLE_CONFIRMATION_DIALOG,
    TAKE_SCREENSHOT,
    NAVIGATE_BACK,
    IDLE_WAIT
}

enum class ErrorRecoveryStrategy {
    RETRY_CURRENT_STAGE,
    DISMISS_DIALOG_AND_RETRY,
    NAVIGATE_BACK_TO_DASHBOARD,
    ABORT_SESSION
}
