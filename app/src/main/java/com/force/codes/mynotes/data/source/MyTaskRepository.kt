package com.force.codes.mynotes.data.source

import com.force.codes.mynotes.data.Task
import com.force.codes.mynotes.data.db.MyTaskDao
import com.force.codes.mynotes.data.db.MyTaskDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

interface MyTaskRepository {
    suspend fun invalidateCache(tasks: List<Task>)
    suspend fun addTask(task: Task)
    suspend fun completeTask(task: Task)
    suspend fun activeTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getActiveTask(date: String): Flow<List<Task>>
    suspend fun getCompleteTask(date: String): Flow<List<Task>>
    suspend fun queryTask(taskId: String): Flow<Task>
}

class MyTaskRepositoryImpl private constructor(
    private val notesDao: MyTaskDao,
    private val ioDispatcher: CoroutineDispatcher
) : MyTaskRepository {

    override suspend fun getActiveTask(date: String): Flow<List<Task>> = getTask(false, date)

    override suspend fun getCompleteTask(date: String): Flow<List<Task>> = getTask(true, date)

    override suspend fun addTask(task: Task) {
        withContext(ioDispatcher) {
            notesDao.addTask(task)
        }
    }

    override suspend fun completeTask(task: Task) {
        task.isComplete = true
        addTask(task)
    }

    override suspend fun activeTask(task: Task) {
        task.isComplete = false
        addTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        withContext(ioDispatcher) {
            notesDao.deleteTask(task)
        }
    }

    override suspend fun queryTask(taskId: String): Flow<Task> {
        return withContext(ioDispatcher) {
            notesDao.getTask(taskId)
        }
    }

    override suspend fun invalidateCache(tasks: List<Task>) {
        withContext(ioDispatcher) {
            tasks.forEachIndexed { index, task ->
                // update task indices based on task position dragged by user
                task.position = index
                notesDao.addTask(task)
            }
        }
    }

    private fun getTask(active: Boolean, date: String): Flow<List<Task>> = notesDao.getTask(active, date)

    companion object {

        private var INSTANCE: MyTaskRepositoryImpl? = null

        fun getInstance(): MyTaskRepositoryImpl {
            return INSTANCE ?: MyTaskRepositoryImpl(
                MyTaskDatabase.getInstance().notesDao,
                Dispatchers.IO
            ).also { INSTANCE = it }
        }
    }
}