package com.force.codes.mynotes.ui

import android.util.Log
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.force.codes.R
import com.force.codes.databinding.FragmentCreateTaskBinding
import com.force.codes.mynotes.core.base.BindingInflater
import com.force.codes.mynotes.core.extensions.onBackPress
import com.force.codes.mynotes.core.extensions.repeatOnLifecycleKt
import com.force.codes.mynotes.core.util.DateTimeParser
import com.force.codes.mynotes.core.widget.calendar.Day
import com.force.codes.mynotes.data.Task
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.coroutines.flow.collect

class CreateTaskFragment : BaseTaskFragment<FragmentCreateTaskBinding>(),
    TimePickerDialog.OnTimeSetListener {

    override val bindingInflater: BindingInflater<FragmentCreateTaskBinding>
        get() = FragmentCreateTaskBinding::inflate

    override fun setupView() {
        super.setupView()
        ViewCompat.setTranslationZ(requireView(), 100f)

        requireBindingSelf {
            included.calendar.setCalendarListener(this@CreateTaskFragment)
            addButton.setOnClickListener(this@CreateTaskFragment)
            timeContainer.setOnClickListener(this@CreateTaskFragment)

            // onBackPress ext.
            onBackPress(toolbar)

            // set initial time
            time.text = DateTimeParser.convertDate()
        }

        addButtonTitle()
    }

    private val timePicker by lazy {
        com.force.codes.mynotes.core.util.with(viewLifecycleOwner) {
            manager = childFragmentManager
            timeListener = this@CreateTaskFragment
        }.build()
    }

    override fun onTimeSet(
        view: TimePickerDialog?,
        hourOfDay: Int,
        minute: Int,
        second: Int
    ) {
        requireBinding().time.text = DateTimeParser.formatTime(hourOfDay, minute)
    }

    private fun addButtonTitle() {
        var buttonText = getString(R.string.update_your_task)
        var toolbarText = getString(R.string.edit_task)

        val args = arguments?.getBoolean("isNewTask")

        args?.let { isNewTask ->
            if (isNewTask) {
                buttonText = getString(R.string.add_your_task)
                toolbarText = getString(R.string.new_task)
            }
        }

        requireBindingSelf {
            addButton.text = buttonText
            toolbar.title = toolbarText
        }
    }

    override fun observeStateChanges() {
        val taskId = bundleArgs()

        taskId?.let { id ->
            repeatOnLifecycleKt {
                viewModel.queryTask(id).collect { showUi(it) }
            }
        }
    }

    private fun showUi(task: Task) {
        requireBindingSelf {
            title.setText(task.title)
            noteMessage.setText(task.description)
            noteMessage.movementMethod = null
            time.text = DateTimeParser.convertDate(task.taskId.toLong()).toString()

            title.requestFocus()
            noteMessage.requestFocus()

        }
    }

    private fun bundleArgs(): String? {
        return arguments?.getString("taskId")
    }

    override fun onDaySelect(selectedDay: Day?) {
        super.onDaySelect(selectedDay)
        val theSelectedDay = DateTimeParser.parseDay(selectedDay!!)
        viewModel.selectedDay = theSelectedDay
    }

    override fun onAddTask() {
        requireBindingSelf {
            Log.e(null, (viewModel.task == null).toString())
            val task = Task(
                title = title.text?.toString(),
                description = noteMessage.text?.toString(),
                timeInMillis = time.text.toString(),
            )
            viewModel.addTask(task)
        }

        // navigate back
        findNavController().navigateUp()
    }

    override fun onShowTime() {
        timePicker.showTimePickerDialog()
    }
}