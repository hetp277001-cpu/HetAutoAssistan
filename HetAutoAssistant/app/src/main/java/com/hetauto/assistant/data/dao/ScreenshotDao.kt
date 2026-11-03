package com.hetauto.assistant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hetauto.assistant.data.entity.ScreenshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity): Long

    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getScreenshotsForSession(sessionId: Long): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAllScreenshots(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE id = :id LIMIT 1")
    suspend fun getScreenshotById(id: Long): ScreenshotEntity?

    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteScreenshot(id: Long)

    @Query("DELETE FROM screenshots WHERE sessionId = :sessionId")
    suspend fun deleteScreenshotsForSession(sessionId: Long)

    @Query("DELETE FROM screenshots")
    suspend fun clearAll()
}
