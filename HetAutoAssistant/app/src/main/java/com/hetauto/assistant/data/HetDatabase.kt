package com.hetauto.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hetauto.assistant.data.dao.ActionLogDao
import com.hetauto.assistant.data.dao.ErrorLogDao
import com.hetauto.assistant.data.dao.ScreenshotDao
import com.hetauto.assistant.data.dao.TestSessionDao
import com.hetauto.assistant.data.dao.WorkflowDao
import com.hetauto.assistant.data.entity.ActionLogEntity
import com.hetauto.assistant.data.entity.ErrorLogEntity
import com.hetauto.assistant.data.entity.ScreenshotEntity
import com.hetauto.assistant.data.entity.TestSessionEntity
import com.hetauto.assistant.data.entity.WorkflowEntity

@Database(
    entities = [
        TestSessionEntity::class,
        ActionLogEntity::class,
        ScreenshotEntity::class,
        WorkflowEntity::class,
        ErrorLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HetDatabase : RoomDatabase() {

    abstract fun testSessionDao(): TestSessionDao
    abstract fun actionLogDao(): ActionLogDao
    abstract fun screenshotDao(): ScreenshotDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun errorLogDao(): ErrorLogDao

    companion object {
        @Volatile
        private var INSTANCE: HetDatabase? = null

        fun getInstance(context: Context): HetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HetDatabase::class.java,
                    "het_auto_assistant.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
