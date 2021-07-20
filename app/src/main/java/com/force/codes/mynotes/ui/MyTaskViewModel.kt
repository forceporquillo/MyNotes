package com.force.codes.mynotes.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.codes.mynotes.core.util.DateTimeParser
import com.force.codes.mynotes.data.Task
import com.force.codes.mynotes.data.source.MyTaskRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class MyTaskViewModel : BaseTaskViewModel() {

    var activeTaskAngle = 0f
    var completedTaskAngle = 0f

    val uiAddedState = mUiAddedEventState
    val uiRemovedEventState = mUiRemovedEventState
    val uiUpdatedEventState = mUiUpdatedEventState
    val uiCompleteEventState = mUiCompleteEventState

    val uiActiveTask = mUiActiveTask
    val uiCompletedTask = mUiCompletedTask

    private var emptyStateObserver = booleanArrayOf(false, false)

    val uiEmptyState = mUiEmptyState

    var task: Task? = null
        private set

    var selectedDay: String = DateTimeParser.getTodayDate()

    private var modifiedDataSet = Array<List<Task>>(2) { emptyList() }
    private var isModified = false

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskRepository.addTask(mutateAndClear(task))
        }
        setTaskUiState(uiAddedState)
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.completeTask(task)
        }
        setTaskUiState(uiCompleteEventState)
    }

    fun activeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.activeTask(task)
        }
        setTaskUiState(mUiUpdatedEventState)
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
        setTaskUiState(mUiRemovedEventState)
    }

    fun moveTask(index: Int, dataSet: List<Task>) {
        modifiedDataSet[index] = dataSet
        isModified = true
    }

    fun queryTask(taskId: String) = flow {
        emitAll(taskRepository.queryTask(taskId))
    }.map { return@map it.also { this.task = it } }

    private fun mutateAndClear(task: Task): Task {
        this.task?.let { t ->
            task.taskId = t.taskId
            this.task = null

            // also notify UI with the update
            setTaskUiState(uiUpdatedEventState)
        }
        task.today = this.selectedDay
        return task
    }

    // invokes whenever the adapter detects
    // drag position or data set changes.
    fun dispatchedToCache() {
        for (dataSet in modifiedDataSet) {
            if (dataSet.isNotEmpty() && isModified) {
                invalidateCache(dataSet)
            }
        }
        isModified = false
    }

    // cache to local database
    // runs in dispatcher.main.immediate
    private fun invalidateCache(dataSet: List<Task>) {
        viewModelScope.launch {
            // reorders the data by new index in the repository.
            taskRepository.invalidateCache(dataSet)
        }
    }

    // update the underlying state flow to notify
    // the delegate observer in the UI
    private fun setTaskUiState(
        uiState: MutableStateFlow<TaskUiState>
    ) {
        uiState.value = TaskUiState.Success(true)
        viewModelScope.launch {
            // we set the state to idle after 2.5 seconds
            delay(2500L)
            uiState.value = TaskUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        dispatchedToCache()
    }

    fun checkEmptyState() {
        val isEmpty = emptyStateObserver[ACTIVE_TASK] && emptyStateObserver[COMPLETED_TASK]
        val state = TaskUiState.Success(isEmpty)
        mUiEmptyState.value = state
    }

    fun queryAllTask(date: String? = null) {
        viewModelScope.launch {
            val activeTaskJob = launch {
                taskRepository.getActiveTask(date ?: selectedDay).collect {
                    Log.e(null, "${it.size}")
                    if (it.isNullOrEmpty().also { e -> setEmpty(ACTIVE_TASK, e) }) {
                        mUiActiveTask.value = TaskUiState.Empty
                    } else {
                        mUiActiveTask.value = TaskUiState.Success(it)
                    }
                }
            }

            val completeTaskJob = launch {
                taskRepository.getCompleteTask(date ?: selectedDay).collect {
                    if (it.isEmpty()) {
                        uiCompletedTask.value = TaskUiState.Empty
                    } else {
                        uiCompletedTask.value = TaskUiState.Success(it)
                    }
                    setEmpty(COMPLETED_TASK, it.isEmpty())
                }
            }

            // parallel work
            joinAll(activeTaskJob, completeTaskJob)
        }
    }

    init {
        queryAllTask(null)
    }

    private fun setEmpty(
        index: Int,
        isEmpty: Boolean
    ) {
        emptyStateObserver[index] = isEmpty
    }
}

abstract class BaseTaskViewModel : ViewModel() {

    protected val taskRepository = MyTaskRepositoryImpl.getInstance()

    // emit initial value -> loading
    protected val mUiAddedEventState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    protected val mUiRemovedEventState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    protected val mUiUpdatedEventState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    protected val mUiCompleteEventState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)

    protected val mUiActiveTask = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    protected val mUiCompletedTask = MutableStateFlow<TaskUiState>(TaskUiState.Loading)

    protected val mUiEmptyState = MutableStateFlow<TaskUiState>(TaskUiState.Idle)

    sealed class TaskUiState {
        data class Success<T : Any>(val state: T) : TaskUiState()
        object Empty : TaskUiState()
        object Loading : TaskUiState()
        object Idle : TaskUiState()
    }
}