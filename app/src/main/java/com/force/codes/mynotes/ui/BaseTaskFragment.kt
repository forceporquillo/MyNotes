package com.force.codes.mynotes.ui

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.force.codes.mynotes.core.base.BaseFragment
import com.force.codes.mynotes.core.widget.calendar.CollapsibleCalendar
import com.force.codes.mynotes.core.widget.calendar.Day

abstract class BaseTaskFragment<T: ViewBinding> : BaseFragment<T>(), MyTaskListeners, CollapsibleCalendar.CalendarListener {

    protected  val viewModel by activityViewModels<MyTaskViewModel>()

    override fun onViewCompletedTask() {}

    override fun onViewActiveTask() {}

    override fun onViewTask() {}

    override fun onAddTask() {}

    abstract fun observeStateChanges()

    // must explicitly call super.setupView() by the sub class.
    override fun setupView() {
        observeStateChanges()
    }

    override fun onDaySelect(selectedDay: Day?) {}

    override fun onItemClick(v: View) {}

    override fun onDataUpdate() {}

    override fun onMonthChange() {}

    override fun onWeekChange(position: Int) {}

    override fun onClickListener() {}

    override fun onDayChanged() {}

    override fun onShowTime() {}
}