package com.hetauto.assistant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hetauto.assistant.data.entity.ErrorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ErrorLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertError(error: ErrorLogEntity): Long

    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC")
    fun getAllErrors(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getErrorsForSession(sessionId: Long): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestError(): Flow<ErrorLogEntity?>

    @Query("DELETE FROM error_logs")
    suspend fun clearAll()
}
