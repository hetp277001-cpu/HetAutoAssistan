package com.hetauto.assistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val filePath: String,
    val stageName: String,
    val width: Int,
    val height: Int,
    val fileSize: Long,
    val note: String? = null
)
