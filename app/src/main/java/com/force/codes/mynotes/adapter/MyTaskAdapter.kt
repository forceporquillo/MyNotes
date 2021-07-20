package com.force.codes.mynotes.adapter

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.force.codes.R
import com.force.codes.databinding.TaskItemBinding
import com.force.codes.mynotes.core.widget.swipeadater.DragDropSwipeAdapter
import com.force.codes.mynotes.data.Task
import com.force.codes.mynotes.ui.ACTIVE_TASK
import com.force.codes.mynotes.ui.COMPLETED_TASK

/**
 * Abstraction of custom adapter which extends default
 * RecyclerView to support gestures like drag & drop and swipe.
 *
 * @see DragDropSwipeAdapter
 */

class MyTaskAdapter(
    private val changeListener: ItemTaskChangeListener
) : DragDropSwipeAdapter<Task, MyTaskAdapter.MyTaskViewHolder>() {

    interface ItemTaskChangeListener {

        /**
         * Invoked when a selected task is clicked.
         */
        fun onClickTask(taskId: String)

        /**
         * Invoked everytime a task item is drag.
         * @param indexAdapter the delegate adapter, either COMPLETE_TASK or ACTIVE_TASK
         */

        fun onTaskDataIsMoved(indexAdapter: Int)
    }

    fun addTag(isTask: Boolean) {
        super.isActiveTask = isTask
    }

    private var lastPosition = -1

    class MyTaskViewHolder(
        private val binding: TaskItemBinding,
        private val changeListener: ItemTaskChangeListener
    ) : DragDropSwipeAdapter.ViewHolder(binding.root) {

        override fun clearAnimation() {
            binding.root.clearAnimation()
        }

        fun bindData(item: Task) {
            with(binding) {
                title.text = item.title
                description.text = item.description
                dateTime.text = item.timeInMillis

                root.setOnClickListener { changeListener.onClickTask(item.taskId) }
            }
        }

        val dragItem: ImageView
            get() = binding.dragItem
    }

    override fun getViewHolder(itemView: View): MyTaskViewHolder {
        return MyTaskViewHolder(TaskItemBinding.bind(itemView), changeListener)
    }

    override fun onBindViewHolder(
        item: Task,
        viewHolder: MyTaskViewHolder,
        position: Int
    ) {
        viewHolder.bindData(item)
        setAnimation(viewHolder.itemView, position)
    }

    override fun getViewToTouchToStartDraggingItem(
        item: Task,
        viewHolder: MyTaskViewHolder,
        position: Int
    ): View = viewHolder.dragItem

    override fun canBeDragged(
        item: Task,
        viewHolder: MyTaskViewHolder,
        position: Int
    ) = true

    override fun canBeSwiped(
        item: Task,
        viewHolder: MyTaskViewHolder,
        position: Int
    ) = true

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation: Animation =
                AnimationUtils.loadAnimation(viewToAnimate.context, R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun onDragFinished(item: Task, viewHolder: MyTaskViewHolder) {
        if (isActiveTask == null) {
            return
        }

        val indexAdapter = if (isActiveTask!!) ACTIVE_TASK else COMPLETED_TASK
        changeListener.onTaskDataIsMoved(indexAdapter)
    }

    override fun onViewDetachedFromWindow(holder: MyTaskViewHolder) {
        holder.clearAnimation()
    }
}