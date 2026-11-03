package com.hetauto.assistant.screenshots

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.hetauto.assistant.data.repository.HetAutomationRepository
import com.hetauto.assistant.service.HetAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class ScreenshotManager(
    private val context: Context,
    private val repository: HetAutomationRepository
) {

    suspend fun captureAndSave(sessionId: Long, stageName: String): Result<String> = withContext(Dispatchers.IO) {
        val service = HetAccessibilityService.getInstance()
            ?: return@withContext Result.failure(IllegalStateException("Accessibility service not connected"))

        val bitmapResult: Pair<Bitmap?, String?> = suspendCancellableCoroutine { cont ->
            val executor = Dispatchers.Main.asExecutor()
            service.takeSystemScreenshot(executor) { bitmap, err ->
                if (cont.isActive) {
                    cont.resume(Pair(bitmap, err))
                }
            }
        }

        val bitmap = bitmapResult.first
        if (bitmap == null) {
            val errorMsg = bitmapResult.second ?: "Failed to acquire screen buffer"
            return@withContext Result.failure(Exception(errorMsg))
        }

        try {
            val sessionDir = File(context.filesDir, "screenshots/session_$sessionId")
            if (!sessionDir.exists()) {
                sessionDir.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            val cleanStage = stageName.replace(" ", "_").replace(":", "").lowercase()
            val file = File(sessionDir, "cap_${timestamp}_$cleanStage.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            val fileSize = file.length()
            val width = bitmap.width
            val height = bitmap.height

            repository.recordScreenshot(
                sessionId = sessionId,
                filePath = file.absolutePath,
                stageName = stageName,
                width = width,
                height = height,
                fileSize = fileSize,
                note = "Captured during $stageName"
            )

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createShareIntent(filePath: String): Intent? {
        val file = File(filePath)
        if (!file.exists()) return null

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    suspend fun deleteScreenshot(id: Long, filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            repository.deleteScreenshot(id)
            true
        } catch (_: Exception) {
            false
        }
    }
}
