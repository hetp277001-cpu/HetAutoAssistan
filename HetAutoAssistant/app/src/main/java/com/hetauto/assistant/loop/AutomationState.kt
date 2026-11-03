package com.hetauto.assistant.loop

enum class AutomationState(val label: String) {
    IDLE("Idle"),
    RUNNING("Running"),
    PAUSED("Paused"),
    STOPPED("Stopped"),
    COMPLETED("Completed"),
    ERROR("Error")
}
