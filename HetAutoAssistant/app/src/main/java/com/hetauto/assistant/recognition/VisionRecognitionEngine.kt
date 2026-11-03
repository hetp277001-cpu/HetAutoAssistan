package com.hetauto.assistant.recognition

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class VisionRecognitionEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): List<UiElementModel> =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val results = mutableListOf<UiElementModel>()
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val box = line.boundingBox ?: Rect()
                                val element = UiElementModel(
                                    text = line.text,
                                    bounds = box,
                                    confidence = line.confidence ?: 0.9f,
                                    elementType = determineVisionElementType(line.text),
                                    isClickable = true
                                )
                                results.add(element)
                            }
                        }
                        continuation.resume(results)
                    }
                    .addOnFailureListener {
                        continuation.resume(emptyList())
                    }
            } catch (e: Exception) {
                continuation.resume(emptyList())
            }
        }

    private fun determineVisionElementType(text: String): ElementType {
        val lower = text.lowercase().trim()
        val buttonWords = listOf("ok", "submit", "confirm", "next", "continue", "start", "stop", "menu", "cancel")
        return if (buttonWords.contains(lower)) {
            ElementType.BUTTON
        } else {
            ElementType.TEXT_VIEW
        }
    }

    fun close() {
        try {
            recognizer.close()
        } catch (_: Exception) {}
    }
}
