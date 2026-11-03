package com.hetauto.assistant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hetauto.assistant.data.entity.ActionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActionLogEntity): Long

    @Query("SELECT * FROM action_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getLogsForSession(sessionId: Long): Flow<List<ActionLogEntity>>

    @Query("SELECT * FROM action_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<ActionLogEntity>>

    @Query("SELECT COUNT(*) FROM action_logs WHERE sessionId = :sessionId AND status = 'SUCCESS'")
    suspend fun getSuccessfulActionsCount(sessionId: Long): Int

    @Query("DELETE FROM action_logs WHERE sessionId = :sessionId")
    suspend fun deleteLogsForSession(sessionId: Long)

    @Query("DELETE FROM action_logs")
    suspend fun clearAll()
}
