package com.hetauto.assistant.recognition

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object NodeHierarchyParser {

    fun parse(rootNode: AccessibilityNodeInfo?): ScreenSnapshot {
        if (rootNode == null) {
            return ScreenSnapshot()
        }

        val allElements = mutableListOf<UiElementModel>()
        val scrollables = mutableListOf<UiElementModel>()
        var isDialog = false
        var isLoading = false
        var pkgName = ""

        try {
            pkgName = rootNode.packageName?.toString().orEmpty()
        } catch (_: Exception) {}

        traverseNode(rootNode, allElements, scrollables)

        isLoading = checkIsLoadingScreen(allElements)
        isDialog = checkIsConfirmationDialog(allElements)
        val fourBoxes = detectFourBoxLayout(allElements)
        val hierarchyHash = calculateHierarchyHash(allElements)

        return ScreenSnapshot(
            timestamp = System.currentTimeMillis(),
            packageName = pkgName,
            allElements = allElements,
            isConfirmationDialogPresent = isDialog,
            isLoadingScreen = isLoading,
            fourBoxElements = fourBoxes,
            scrollableAreas = scrollables,
            hierarchyHash = hierarchyHash
        )
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo,
        outElements: MutableList<UiElementModel>,
        outScrollables: MutableList<UiElementModel>
    ) {
        val rect = Rect()
        node.getBoundsInScreen(rect)

        val text = node.text?.toString().orEmpty()
        val desc = node.contentDescription?.toString().orEmpty()
        val className = node.className?.toString().orEmpty()
        val viewId = node.viewIdResourceName.orEmpty()
        val isClickable = node.isClickable
        val isScrollable = node.isScrollable
        val isEditable = node.isEditable
        val isEnabled = node.isEnabled
        val isVisible = node.isVisibleToUser

        val elementType = determineElementType(className, text, desc, isClickable, isScrollable)

        val element = UiElementModel(
            id = viewId,
            text = text,
            contentDescription = desc,
            className = className,
            packageName = node.packageName?.toString().orEmpty(),
            bounds = rect,
            isClickable = isClickable,
            isScrollable = isScrollable,
            isEditable = isEditable,
            isEnabled = isEnabled,
            isVisibleToUser = isVisible,
            confidence = 1.0f,
            elementType = elementType
        )

        if (isVisible && (rect.width() > 0 && rect.height() > 0)) {
            outElements.add(element)
        }

        if (isScrollable) {
            outScrollables.add(element)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, outElements, outScrollables)
            try {
                child.recycle()
            } catch (_: Exception) {}
        }
    }

    private fun determineElementType(
        className: String,
        text: String,
        desc: String,
        isClickable: Boolean,
        isScrollable: Boolean
    ): ElementType {
        val lowerClass = className.lowercase()

        return when {
            isScrollable || lowerClass.contains("scroll") || lowerClass.contains("recycler") || lowerClass.contains("listview") -> {
                ElementType.SCROLLABLE_CONTAINER
            }
            lowerClass.contains("progress") || lowerClass.contains("loading") || lowerClass.contains("spinner") -> {
                ElementType.PROGRESS_BAR
            }
            lowerClass.contains("edittext") -> {
                ElementType.EDIT_TEXT
            }
            lowerClass.contains("button") || (isClickable && (text.isNotBlank() || desc.isNotBlank())) -> {
                ElementType.BUTTON
            }
            lowerClass.contains("image") || lowerClass.contains("icon") -> {
                ElementType.IMAGE_OR_ICON
            }
            text.isNotBlank() -> {
                ElementType.TEXT_VIEW
            }
            else -> ElementType.UNKNOWN
        }
    }

    private fun checkIsLoadingScreen(elements: List<UiElementModel>): Boolean {
        for (el in elements) {
            if (el.elementType == ElementType.PROGRESS_BAR) return true
            val t = "${el.text} ${el.contentDescription}".lowercase()
            if (t.contains("loading") || t.contains("please wait") || t.contains("buffering") || t.contains("processing...")) {
                return true
            }
        }
        return false
    }

    private fun checkIsConfirmationDialog(elements: List<UiElementModel>): Boolean {
        var hasDialogText = false
        var hasPositiveAction = false

        val positiveTriggers = setOf("confirm", "ok", "yes", "accept", "submit", "proceed", "continue", "verify")
        val dialogIndicators = setOf("are you sure", "confirmation", "warning", "confirm action", "dialog")

        for (el in elements) {
            val txt = "${el.text} ${el.contentDescription}".lowercase()
            if (dialogIndicators.any { txt.contains(it) }) {
                hasDialogText = true
            }
            if (el.isClickable && positiveTriggers.any { txt.contains(it) }) {
                hasPositiveAction = true
            }
        }

        return hasDialogText && hasPositiveAction
    }

    fun detectFourBoxLayout(elements: List<UiElementModel>): List<UiElementModel> {
        val boxLabeled = elements.filter {
            val label = it.label.lowercase()
            label.contains("box") || label.matches(Regex(".*(?:option|box|item|tile)\\s*[1-4a-d].*", RegexOption.IGNORE_CASE))
        }
        if (boxLabeled.size == 4) {
            return boxLabeled.sortedWith(compareBy({ it.bounds.top }, { it.bounds.left }))
        }

        val candidateBoxes = elements.filter {
            it.isClickable &&
            it.bounds.width() in 100..800 &&
            it.bounds.height() in 80..800 &&
            it.bounds.width() > 50 &&
            it.bounds.height() > 50
        }

        if (candidateBoxes.size == 4) {
            return candidateBoxes.sortedWith(compareBy({ it.bounds.top }, { it.bounds.left }))
        }

        return emptyList()
    }

    private fun calculateHierarchyHash(elements: List<UiElementModel>): Int {
        var hash = 17
        for (el in elements) {
            hash = 31 * hash + el.label.hashCode()
            hash = 31 * hash + el.bounds.hashCode()
        }
        return hash
    }
}
