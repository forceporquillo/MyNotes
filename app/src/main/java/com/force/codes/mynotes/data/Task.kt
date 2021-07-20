package com.force.codes.mynotes.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Task @JvmOverloads constructor(
    var title: String? = "",
    var description: String? = "",
    var today: String = "",
    var timeInMillis: String? = "",
    var position: Int = 0,
    var isComplete: Boolean = false,
    @PrimaryKey var taskId: String = System.currentTimeMillis().toString()
) : Parcelable

//    var time: String? = "",
//    var category: Int = -1,
//    var isCompleted: Boolean = false,
//    var allowNotifications: Boolean = false,
//    var timeToGetNotified: Long? = null,
//    var isSet: Boolean = false,