package com.hetauto.assistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long?,
    val timestamp: Long,
    val stageName: String?,
    val errorCode: String,
    val errorMessage: String,
    val stackTrace: String? = null,
    val screenshotPath: String? = null
)
