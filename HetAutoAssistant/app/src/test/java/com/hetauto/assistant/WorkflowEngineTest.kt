package com.hetauto.assistant

import com.hetauto.assistant.recognition.ElementSelector
import com.hetauto.assistant.recognition.ElementType
import com.hetauto.assistant.recognition.NodeHierarchyParser
import com.hetauto.assistant.recognition.ScreenRecognitionEngine
import com.hetauto.assistant.recognition.ScreenSnapshot
import com.hetauto.assistant.recognition.UiElementModel
import com.hetauto.assistant.workflow.DefaultFourBoxWorkflow
import com.hetauto.assistant.workflow.WorkflowStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkflowEngineTest {

    @Test
    fun testDefaultFourBoxWorkflow_hasExactSixStages() {
        val workflow = DefaultFourBoxWorkflow.create()
        assertEquals(DefaultFourBoxWorkflow.WORKFLOW_ID, workflow.id)
        assertEquals(6, workflow.stages.size)

        assertEquals(WorkflowStage.INITIAL_SCREEN, workflow.stages[0].stage)
        assertEquals(WorkflowStage.MENU_NAVIGATION, workflow.stages[1].stage)
        assertEquals(WorkflowStage.DEMO_FOUR_BOX_TEST, workflow.stages[2].stage)
        assertEquals(WorkflowStage.TEST_CONFIRMATION, workflow.stages[3].stage)
        assertEquals(WorkflowStage.SCREENSHOT, workflow.stages[4].stage)
        assertEquals(WorkflowStage.COMPLETION, workflow.stages[5].stage)
    }

    @Test
    fun testElementSelector_textMatching() {
        val engine = ScreenRecognitionEngine()
        val elements = listOf(
            UiElementModel(
                text = "Test Balance: $1,250.00",
                isClickable = false,
                elementType = ElementType.TEXT_VIEW
            ),
            UiElementModel(
                text = "Four-Box Test",
                isClickable = true,
                elementType = ElementType.BUTTON
            )
        )
        val snapshot = ScreenSnapshot(allElements = elements)

        val balanceEl = engine.findElement(
            snapshot,
            ElementSelector(textContains = "balance")
        )
        assertNotNull(balanceEl)
        assertEquals("Test Balance: $1,250.00", balanceEl?.text)

        val buttonEl = engine.findElement(
            snapshot,
            ElementSelector(textContains = "four-box", isClickableOnly = true)
        )
        assertNotNull(buttonEl)
        assertTrue(buttonEl!!.isClickable)
    }

    @Test
    fun testScreenRecognition_balanceExtraction() {
        val engine = ScreenRecognitionEngine()
        val elements = listOf(
            UiElementModel(text = "Dashboard"),
            UiElementModel(text = "Balance: $1,250.00")
        )
        val snapshot = ScreenSnapshot(allElements = elements)
        val balance = engine.extractBalance(snapshot)
        assertNotNull(balance)
        assertTrue(balance!!.contains("1,250.00"))
    }

    @Test
    fun testFourBoxDetection_patternMatching() {
        val elements = listOf(
            UiElementModel(text = "Box 1: Network Check", isClickable = true),
            UiElementModel(text = "Box 2: Cache Refresh", isClickable = true),
            UiElementModel(text = "Box 3: Database Sync", isClickable = true),
            UiElementModel(text = "Box 4: Security Assert", isClickable = true)
        )

        val detectedBoxes = NodeHierarchyParser.detectFourBoxLayout(elements)
        assertEquals(4, detectedBoxes.size)
    }
}
