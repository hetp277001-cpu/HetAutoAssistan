package com.hetauto.assistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_sessions")
data class TestSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workflowName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val targetRepetitions: Int,
    val completedRepetitions: Int = 0,
    val totalActions: Int = 0,
    val status: String,
    val failureReason: String? = null
)
