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

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

private const val ANIM_DELAY = 300L

fun RecyclerView.invertVisibility() {
    isVisible = !isVisible
}

fun ViewGroup.animateRoot() {
    TransitionManager.beginDelayedTransition(this, AutoTransition())
}

fun CardView.rotateAnim(angle: Float) {
    animate()
        .rotation(angle)
        .setDuration(ANIM_DELAY)
        .start()
}