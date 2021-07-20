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

package com.force.codes.mynotes.core.widget.calendar

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Day : Parcelable {
    var year: Int
        private set
    var month: Int
        private set
    var day = 0
        private set

    constructor(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
    }

    constructor(`in`: Parcel) {
        val data = IntArray(3)
        `in`.readIntArray(data)
        year = data[0]
        month = data[1]
        year = data[2]
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeIntArray(
            intArrayOf(
                year,
                month,
                day
            )
        )
    }

    fun toUnixTime(): Long {
        val date = Date(year, month, day)
        return date.time
    }

    val diff: Int
        get() {
            val todayCal = Calendar.getInstance()
            val day = Day(
                todayCal[Calendar.YEAR],
                todayCal[Calendar.MONTH],
                todayCal[Calendar.DAY_OF_MONTH]
            )
            return ((toUnixTime() - day.toUnixTime())
                    / (1000 * 60 * 60 * 24)).toInt()
        }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<*> = object : Parcelable.Creator<Any?> {
            override fun createFromParcel(`in`: Parcel): Day? {
                return Day(`in`)
            }

            override fun newArray(size: Int): Array<Day?> {
                return arrayOfNulls(size)
            }
        }
    }
}