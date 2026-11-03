package com.hetauto.assistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_logs")
data class ActionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val stageName: String,
    val actionType: String,
    val targetElementDescription: String,
    val status: String,
    val durationMs: Long,
    val message: String? = null
)
