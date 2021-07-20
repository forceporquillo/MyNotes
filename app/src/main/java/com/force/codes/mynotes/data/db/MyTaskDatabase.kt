package com.force.codes.mynotes.data.db

import android.content.Context
import androidx.room.*
import com.force.codes.mynotes.data.Task
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction layer on top of SQLite Database
 */

@Database(
    entities = [Task::class],
    version = 2,
    exportSchema = false
)
abstract class MyTaskDatabase : RoomDatabase() {

    abstract val notesDao: MyTaskDao

    companion object {

        @Volatile
        var INSTANCE: MyTaskDatabase? = null
            private set

        fun getInstance() = INSTANCE!!

        fun createInstance(context: Context) {
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                MyTaskDatabase::class.java,
                "MyNotes.db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}

@Dao
interface MyTaskDao {

    @Query("SELECT * FROM task WHERE isComplete =:active AND today =:date ORDER BY position ASC")
    fun getTask(active: Boolean, date: String): Flow<List<Task>>

    // either insert or update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task where taskId =:taskId")
    fun getTask(taskId: String): Flow<Task>
//
//    @ExperimentalCoroutinesApi
//    fun getTaskDistinctUntilChange(task: Task)
//
//    @Query("SELECT * FROM task WHERE ti")
//    fun getTask(name: String): Flow<List<Task>>
}