/*
 *
 * Copyright 2021 Force Porquillo (strongforce1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

@file:Suppress("unused", "WeakerAccess")

package com.force.codes.mynotes.core.util

import androidx.annotation.MainThread
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.force.codes.mynotes.core.widget.calendar.Day
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeParser {

    private const val TIME_FORMAT = "hh:mm aa"
    private const val DATE_FORMAT = "MMMM-dd-yyyy"

    private var INSTANCE: SimpleDateFormat? = null

    private val weekDays =
        arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private val months = arrayOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    fun getTodayDate(): String {
        val date = Calendar.getInstance().time
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)
    }

    fun parseDate(
        index: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ): String = "${weekDays[index - 1]}, ${months[monthOfYear]} $dayOfMonth"

    fun formatTime(
        hourOfDay: Int,
        minute: Int
    ): String {
        val minuteStr = if (minute < 10) "0$minute" else minute

        return try {
            val hourStr = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay
            val sdf = SimpleDateFormat("H:mm", Locale.US)
            val d = sdf.parse("$hourStr:$minuteStr")!!
            SimpleDateFormat("hh:mm aa", Locale.US).format(d).toString()
        } catch (_: ParseException) {
            val eHour = if (hourOfDay > 12) hourOfDay % 12 else hourOfDay
            val format = if (hourOfDay >= 12) "PM" else "AM"
            "$eHour:$minuteStr $format"
        }
    }

    fun parseDay(day: Day): String {
        return "${months[day.month]}-${day.day}-${day.year}"
    }

    fun decrementAndParseDay(day: Day?): String {
        return parseDay(Day(day!!.year, day.month - 1, day.day))
    }

    @MainThread
    fun convertDate(dateInMilliseconds: Long? = null): String? {
        var dateInMillis = dateInMilliseconds
        if (INSTANCE == null) {
            INSTANCE = SimpleDateFormat(TIME_FORMAT, Locale.US)
        }
        if (dateInMilliseconds == null) {
            dateInMillis = System.currentTimeMillis()
        }
        return INSTANCE?.format(dateInMillis)
    }

    val dateFormatter: SimpleDateFormat
        get() {
            if (INSTANCE == null) {
                INSTANCE = SimpleDateFormat(TIME_FORMAT, Locale.US)
            }

            return INSTANCE as SimpleDateFormat
        }
}

inline fun with(
    owner: LifecycleOwner,
    body: TimePicker.Builder.() -> Unit
) = TimePicker.Builder(owner).apply(body)

private const val TIME_PICKER_TAG = "TimePickerDialogTag"

class TimePicker private constructor(
    lifecycleOwner: LifecycleOwner,
    private val manager: FragmentManager,
    private val timeListener: OnTimeSetListener
) : LifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private var _timePickerDialog: TimePickerDialog? = null
    val timePickerDialog get() = _timePickerDialog!!

    fun showTimePickerDialog() {
        val rightNow = Calendar.getInstance()
        rightNow.get(Calendar.DAY_OF_WEEK)
        val hourOfDay = rightNow.get(Calendar.HOUR_OF_DAY)
        val minute = rightNow.get(Calendar.MINUTE)

        if (_timePickerDialog == null) {
            _timePickerDialog = TimePickerDialog.newInstance(
                timeListener,
                hourOfDay, minute,
                false
            )
        } else {
            _timePickerDialog?.initialize(
                timeListener,
                hourOfDay, minute,
                rightNow.get(Calendar.SECOND),
                false
            )
        }

        timePickerDialog.apply {
            version = TimePickerDialog.Version.VERSION_2

        }.show(manager, TIME_PICKER_TAG)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (_timePickerDialog != null) {
            _timePickerDialog = null
        }
    }

    class Builder(private val owner: LifecycleOwner) {
        lateinit var manager: FragmentManager
        lateinit var timeListener: OnTimeSetListener

        fun build() = TimePicker(owner, manager, timeListener)
    }
}