package com.hetauto.assistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val targetPackage: String,
    val stagesJson: String,
    val createdAt: Long,
    val isDefault: Boolean = false
)
