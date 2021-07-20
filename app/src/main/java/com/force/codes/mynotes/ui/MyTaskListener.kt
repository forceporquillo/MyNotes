package com.force.codes.mynotes.ui

import android.view.View
import com.force.codes.R

internal interface MyTaskListeners : View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_task_fab -> onAddTask()
            R.id.active_arrow_button -> onViewActiveTask()
            R.id.complete_arrow_button -> onViewCompletedTask()
            R.id.add_button -> onAddTask()
            R.id.time_container -> onShowTime()
        }
    }

    fun onShowTime()
    fun onViewCompletedTask()
    fun onViewActiveTask()
    fun onViewTask()
    fun onAddTask()
}