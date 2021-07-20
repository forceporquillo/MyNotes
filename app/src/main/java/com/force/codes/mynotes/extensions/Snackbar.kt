
package com.force.codes.mynotes.extensions

import com.force.codes.mynotes.ui.MyTaskFragment
import com.google.android.material.snackbar.Snackbar

fun MyTaskFragment.showSnackBar(message: String = "Task successfully added!") {
    Snackbar.make(
        requireBinding().coordinatorLayout,
        message,
        Snackbar.LENGTH_SHORT
    ).show()
}