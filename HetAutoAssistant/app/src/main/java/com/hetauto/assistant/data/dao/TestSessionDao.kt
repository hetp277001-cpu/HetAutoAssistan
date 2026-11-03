package com.hetauto.assistant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hetauto.assistant.data.entity.TestSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TestSessionEntity): Long

    @Update
    suspend fun updateSession(session: TestSessionEntity)

    @Query("SELECT * FROM test_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM test_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): TestSessionEntity?

    @Query("SELECT * FROM test_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): TestSessionEntity?

    @Query("DELETE FROM test_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM test_sessions")
    suspend fun clearAll()
}
