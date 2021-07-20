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

package com.force.codes.mynotes.core.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Fragment.navigateTo(direction: NavDirections) {
    findNavController().navigate(direction)
}

fun Fragment.navigateTo(@IdRes destination: Int, bundle: Bundle? = null) {
    findNavController().navigate(destination, bundle)
}

fun Fragment.repeatOnLifecycleKt(
    state: Lifecycle.State? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(state ?: Lifecycle.State.STARTED, block)
    }
}

fun Fragment.onBackPress(toolbar: Toolbar) {
    toolbar.setNavigationOnClickListener {
        findNavController().navigateUp()
    }
}