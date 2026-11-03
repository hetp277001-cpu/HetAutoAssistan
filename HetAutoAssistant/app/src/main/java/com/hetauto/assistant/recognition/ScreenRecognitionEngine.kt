package com.hetauto.assistant.recognition

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class ScreenRecognitionEngine(
    private val visionEngine: VisionRecognitionEngine = VisionRecognitionEngine()
) {

    private var previousSnapshot: ScreenSnapshot? = null

    suspend fun analyzeCurrentScreen(
        rootNode: AccessibilityNodeInfo?,
        fallbackBitmap: Bitmap? = null
    ): ScreenSnapshot {
        var snapshot = NodeHierarchyParser.parse(rootNode)

        if (snapshot.allElements.isEmpty() && fallbackBitmap != null) {
            val visionElements = visionEngine.recognizeTextFromBitmap(fallbackBitmap)
            val fourBoxes = NodeHierarchyParser.detectFourBoxLayout(visionElements)
            snapshot = snapshot.copy(
                allElements = visionElements,
                fourBoxElements = fourBoxes,
                hierarchyHash = visionElements.hashCode()
            )
        }

        previousSnapshot = snapshot
        return snapshot
    }

    fun hasScreenTransitionOccurred(newSnapshot: ScreenSnapshot): Boolean {
        val prev = previousSnapshot ?: return true
        if (prev.packageName != newSnapshot.packageName) return true
        if (prev.hierarchyHash != newSnapshot.hierarchyHash) return true
        return false
    }

    fun findElement(snapshot: ScreenSnapshot, selector: ElementSelector): UiElementModel? {
        return snapshot.allElements.firstOrNull { el ->
            matchesSelector(el, selector)
        }
    }

    private fun matchesSelector(el: UiElementModel, selector: ElementSelector): Boolean {
        if (!selector.textEquals.isNullOrBlank()) {
            if (!el.text.equals(selector.textEquals, ignoreCase = true) &&
                !el.contentDescription.equals(selector.textEquals, ignoreCase = true)) {
                return false
            }
        }

        if (!selector.textContains.isNullOrBlank()) {
            val combined = "${el.text} ${el.contentDescription}".lowercase()
            if (!combined.contains(selector.textContains.lowercase())) {
                return false
            }
        }

        if (!selector.textRegex.isNullOrBlank()) {
            val combined = "${el.text} ${el.contentDescription}"
            if (!Regex(selector.textRegex, RegexOption.IGNORE_CASE).containsMatchIn(combined)) {
                return false
            }
        }

        if (!selector.viewId.isNullOrBlank()) {
            if (!el.id.contains(selector.viewId, ignoreCase = true)) {
                return false
            }
        }

        if (selector.isClickableOnly && !el.isClickable) {
            return false
        }

        if (selector.targetType != null && el.elementType != selector.targetType) {
            return false
        }

        return true
    }

    fun extractBalance(snapshot: ScreenSnapshot): String? {
        val balanceRegex = Regex("(?:balance|test balance|usd|\\$)\\s*[:]?\\s*([\\$\\€\\£]?\\s*[0-9,]+(?:\\.[0-9]{1,2})?)", RegexOption.IGNORE_CASE)
        for (el in snapshot.allElements) {
            val match = balanceRegex.find("${el.text} ${el.contentDescription}")
            if (match != null) {
                return match.groupValues[1].ifBlank { match.value }
            }
        }
        val currencyRegex = Regex("^[\\$\\€\\£]\\s*[0-9,]+(?:\\.[0-9]{1,2})?$")
        for (el in snapshot.allElements) {
            if (currencyRegex.matches(el.text.trim())) {
                return el.text.trim()
            }
        }
        return null
    }

    fun cleanup() {
        visionEngine.close()
    }
}

data class ElementSelector(
    val textEquals: String? = null,
    val textContains: String? = null,
    val textRegex: String? = null,
    val viewId: String? = null,
    val isClickableOnly: Boolean = false,
    val targetType: ElementType? = null
)
