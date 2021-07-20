package com.force.codes.mynotes.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.force.codes.R
import com.force.codes.databinding.FragmentMyNotesBinding
import com.force.codes.mynotes.adapter.MyTaskAdapter
import com.force.codes.mynotes.core.base.BindingInflater
import com.force.codes.mynotes.core.extensions.*
import com.force.codes.mynotes.core.util.DateTimeParser
import com.force.codes.mynotes.core.widget.calendar.Day
import com.force.codes.mynotes.core.widget.swipeadater.DragDropSwipeRecyclerView
import com.force.codes.mynotes.core.widget.swipeadater.listener.OnItemSwipeListener
import com.force.codes.mynotes.data.Task
import com.force.codes.mynotes.extensions.showSnackBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

internal const val ACTIVE_TASK = 0
internal const val COMPLETED_TASK = 1

class MyTaskFragment : BaseTaskFragment<FragmentMyNotesBinding>(),
    MyTaskAdapter.ItemTaskChangeListener {

    private val taskAdapters = Array(2) { MyTaskAdapter(this) }

    override val bindingInflater: BindingInflater<FragmentMyNotesBinding>
        get() = FragmentMyNotesBinding::inflate

    private val onItemActiveSwipeListener = object : OnItemSwipeListener<Task> {
        override fun onItemSwiped(
            position: Int,
            direction: OnItemSwipeListener.SwipeDirection,
            item: Task,
            isActiveTask: Boolean?
        ): Boolean {
            when (direction) {
                OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT -> {
                    isActiveTask?.let { activeTask ->
                        if (activeTask) {
                            viewModel.completeTask(item)
                        } else {
                            viewModel.activeTask(item)
                        }
                    }
                }
                OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT -> {
                    viewModel.deleteTask(item)
                }
                else -> {}
            }
            return false
        }
    }

    override fun onClickTask(taskId: String) {
        navigateToCreateTask(taskId)
    }

    override fun onTaskDataIsMoved(indexAdapter: Int) {
        viewModel.moveTask(indexAdapter, taskAdapters[indexAdapter].dataSet)
    }

    override fun setupView() {
        super.setupView()

        taskAdapters[ACTIVE_TASK].addTag(true)
        taskAdapters[COMPLETED_TASK].addTag(false)

        requireBindingSelf {

            included.calendar.setCalendarListener(this@MyTaskFragment)

            activeAdapter.apply {
                adapter = this@MyTaskFragment.taskAdapters[ACTIVE_TASK]
                swipeListener = onItemActiveSwipeListener
                behindSwipedItemLayoutId = R.layout.swipe_complete
                behindSwipedItemSecondaryLayoutId = R.layout.swipe_delete_item
            }

            completeAdapter.apply {
                adapter = this@MyTaskFragment.taskAdapters[COMPLETED_TASK]
                swipeListener = onItemActiveSwipeListener
                behindSwipedItemLayoutId = R.layout.swipe_active
                behindSwipedItemSecondaryLayoutId = R.layout.swipe_delete_item
            }

            setListeners()
            observeCompleteTask()
            observeActiveTask()
        }
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).post {
            requireBinding().included.calendar.apply {
                expanded = true
                expand(400)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val today = DateTimeParser.getTodayDate()
        viewModel.queryAllTask(today)
    }

    private fun setListeners() {
        requireBindingSelf {
            addTaskFab.setOnClickListener(this@MyTaskFragment)
            activeArrowButton.setOnClickListener(this@MyTaskFragment)
            completeArrowButton.setOnClickListener(this@MyTaskFragment)
        }
    }

    private fun observeActiveTask() {
        repeatOnLifecycleKt {
            viewModel.uiActiveTask.collect { uiState ->
                uiStateDataFromCache(ACTIVE_TASK, uiState)
            }
        }
    }

    private fun observeCompleteTask() {
        repeatOnLifecycleKt {
            viewModel.uiCompletedTask.collect { uiState ->
                uiStateDataFromCache(COMPLETED_TASK, uiState)
            }
        }
    }

    // each flow is scope to separate coroutine
    override fun observeStateChanges() {
        repeatOnLifecycleKt {
            viewModel.uiAddedState.collect { uiState ->
                uiStateChanges(uiState) {
                    showSnackBar(getString(R.string.added_task))
                }
            }
        }

        repeatOnLifecycleKt {
            viewModel.uiRemovedEventState.collect { uiState ->
                uiStateChanges(uiState) {
                    showSnackBar(getString(R.string.deleted_task))
                }
            }
        }

        repeatOnLifecycleKt {
            viewModel.uiUpdatedEventState.collect { uiState ->
                uiStateChanges(uiState) {
                    showSnackBar(getString(R.string.task_updated))
                }
            }
        }

        repeatOnLifecycleKt {
            viewModel.uiCompleteEventState.collect { uiState ->
                uiStateChanges(uiState) {
                    showSnackBar(getString(R.string.task_completed))
                }
            }
        }

        repeatOnLifecycleKt {
            viewModel.uiEmptyState.collect { uiSate ->
                if (uiSate is BaseTaskViewModel.TaskUiState.Success<*>) {
                    emptyState(uiSate.state as Boolean)
                }
            }
        }
    }

    private fun uiStateChanges(
        uiState: BaseTaskViewModel.TaskUiState,
        block: () -> Unit
    ) {
        if (uiState is BaseTaskViewModel.TaskUiState.Success<*>) {
            if (!(uiState.state as Boolean)) return
            block()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun uiStateDataFromCache(
        index: Int,
        uiState: BaseTaskViewModel.TaskUiState
    ) {
        var hide = false
        when (uiState) {
            is BaseTaskViewModel.TaskUiState.Success<*> -> {
                progressBar(false)
                Log.e(null , "Success...")
                emptyState(false)
                taskAdapters[index].dataSet = uiState.state as List<Task>
            }
            is BaseTaskViewModel.TaskUiState.Empty -> {
                progressBar(false)
                Log.e(null , "Empty...")
                hide = true
            }
            is BaseTaskViewModel.TaskUiState.Loading -> {
                Log.e(null , "Loading...")
                progressBar(true)
                hide = true
            }
            else -> {}
        }

        viewModel.checkEmptyState()
        hideFrame(index, hide)
    }

    private fun progressBar(visible: Boolean) {
        val container = requireBinding().progressCircularContainer
        container.isVisible = visible
    }

    private fun hideFrame(index: Int, hide: Boolean) {
        requireBindingSelf {
            if (index == ACTIVE_TASK) {
                activeTaskFrame.isVisible = !hide
                taskDelegateContainer.isVisible = !hide
            }

            if (index == COMPLETED_TASK) {
                completeTaskDelegate.isVisible = !hide
                completeTaskFrame.isVisible = !hide
            }
        }
    }

    private fun emptyState(visible: Boolean) {
        requireBindingSelf {
            emptyState.isVisible = visible
        }
    }

    private fun animateChanges(
        adapter: DragDropSwipeRecyclerView,
        views: Array<ViewGroup>,
        button: CardView,
        angle: Float,
    ): Float = angleRotation(angle).run {
        adapter.invertVisibility()
        views.forEach { it.animateRoot() }
        button.rotateAnim(this)
        this
    }

    override fun onViewCompletedTask() {
        var angle = viewModel.completedTaskAngle

        requireBindingSelf {
            angle = animateChanges(
                completeAdapter,
                arrayOf(completeTaskDelegate, separator),
                completeArrowButton,
                angle
            )
        }

        viewModel.completedTaskAngle = angle
    }

    override fun onViewActiveTask() {
        var angle = viewModel.activeTaskAngle

        requireBindingSelf {
            angle = animateChanges(
                activeAdapter,
                arrayOf(
                    activeTaskFrame,
                    completeTaskDelegate,
                    separator,
                    // we should also animate the adapter to avoid the
                    // odd looking glitch when animating the root view.
                    completeAdapter
                ),
                activeArrowButton,
                angle
            )
        }

        viewModel.activeTaskAngle = angle
    }

    override fun onAddTask() {
        navigateToCreateTask()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.dispatchedToCache()
    }

    private fun navigateToCreateTask(taskId: String? = null) {
        val bundle = Bundle()

        taskId?.let { id ->
            bundle.putString("taskId", id)
        } ?: bundle.putBoolean("isNewTask", true)

        navigateTo(R.id.createTaskFragmentAction, bundle)
    }

    private fun angleRotation(angle: Float) = if (angle == 0f) -90f else 0f

    override fun onDaySelect(selectedDay: Day?) {
        super.onDaySelect(selectedDay)
        makeQuery(DateTimeParser.parseDay(selectedDay!!))
    }

    private fun makeQuery(date: String? = null) {
        viewModel.queryAllTask(date)
    }
}
