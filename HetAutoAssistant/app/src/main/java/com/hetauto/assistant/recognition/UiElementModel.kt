package com.hetauto.assistant.recognition

import android.graphics.Rect

data class UiElementModel(
    val id: String = "",
    val text: String = "",
    val contentDescription: String = "",
    val className: String = "",
    val packageName: String = "",
    val bounds: Rect = Rect(),
    val isClickable: Boolean = false,
    val isScrollable: Boolean = false,
    val isEditable: Boolean = false,
    val isEnabled: Boolean = true,
    val isVisibleToUser: Boolean = true,
    val confidence: Float = 1.0f,
    val elementType: ElementType = ElementType.UNKNOWN
) {
    val centerX: Int get() = bounds.centerX()
    val centerY: Int get() = bounds.centerY()

    val label: String
        get() = when {
            text.isNotBlank() -> text.trim()
            contentDescription.isNotBlank() -> contentDescription.trim()
            id.isNotBlank() -> id.substringAfter(":id/")
            else -> className.substringAfterLast('.')
        }
}

enum class ElementType {
    BUTTON,
    TEXT_VIEW,
    EDIT_TEXT,
    IMAGE_OR_ICON,
    SCROLLABLE_CONTAINER,
    DIALOG,
    PROGRESS_BAR,
    FOUR_BOX_ITEM,
    UNKNOWN
}

data class ScreenSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String = "",
    val activityTitle: String = "",
    val allElements: List<UiElementModel> = emptyList(),
    val isConfirmationDialogPresent: Boolean = false,
    val isLoadingScreen: Boolean = false,
    val fourBoxElements: List<UiElementModel> = emptyList(),
    val scrollableAreas: List<UiElementModel> = emptyList(),
    val hierarchyHash: Int = 0
)
