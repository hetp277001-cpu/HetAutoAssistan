package com.hetauto.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor

class HetAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "HetAccessibilityService"

        @Volatile
        private var instance: HetAccessibilityService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _recentEvents = MutableSharedFlow<AccessibilityEvent>(extraBufferCapacity = 50)
        val recentEvents: SharedFlow<AccessibilityEvent> = _recentEvents.asSharedFlow()

        fun getInstance(): HetAccessibilityService? = instance

        fun isRunning(): Boolean = instance != null && _isServiceConnected.value
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.i(TAG, "Het Accessibility Service Connected successfully.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        _recentEvents.tryEmit(AccessibilityEvent.obtain(event))
    }

    override fun onInterrupt() {
        Log.w(TAG, "Het Accessibility Service Interrupted.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        _isServiceConnected.value = false
        Log.i(TAG, "Het Accessibility Service Unbound.")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
    }

    fun getSafeRootInActiveWindow(): AccessibilityNodeInfo? {
        return try {
            rootInActiveWindow
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining rootInActiveWindow: ${e.message}")
            null
        }
    }

    fun clickCoordinates(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null) {
        val clickPath = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(clickPath, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (!success) {
            val parent = node.parent
            if (parent != null) {
                success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                try { parent.recycle() } catch (_: Exception) {}
            }
        }
        if (!success) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (bounds.width() > 0 && bounds.height() > 0) {
                clickCoordinates(bounds.centerX().toFloat(), bounds.centerY().toFloat())
                success = true
            }
        }
        return success
    }

    fun performScroll(forward: Boolean, onComplete: ((Boolean) -> Unit)? = null) {
        val displayMetrics = resources.displayMetrics
        val startX = displayMetrics.widthPixels / 2f
        val startY = if (forward) displayMetrics.heightPixels * 0.75f else displayMetrics.heightPixels * 0.25f
        val endY = if (forward) displayMetrics.heightPixels * 0.25f else displayMetrics.heightPixels * 0.75f

        val scrollPath = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(scrollPath, 0, 300)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun performBackAction(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performHomeAction(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun takeSystemScreenshot(executor: Executor, callback: (Bitmap?, String?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                executor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        try {
                            val hardwareBuffer = screenshotResult.hardwareBuffer
                            val bitmap = Bitmap.wrapHardwareBuffer(
                                hardwareBuffer,
                                screenshotResult.colorSpace
                            )?.copy(Bitmap.Config.ARGB_8888, false)
                            hardwareBuffer.close()
                            callback(bitmap, null)
                        } catch (e: Exception) {
                            callback(null, "Bitmap wrap error: ${e.message}")
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        callback(null, "Accessibility takeScreenshot failed with code $errorCode")
                    }
                }
            )
        } else {
            callback(null, "takeScreenshot requires Android 11 (API 30)+. Current API: ${Build.VERSION.SDK_INT}")
        }
    }
}
