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

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

abstract class OnSwipeTouchListener(ctx: Context) : OnTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }

    }

    companion object {

        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    abstract fun onSwipeRight()

    abstract fun onSwipeLeft()

    abstract fun onSwipeTop()

    abstract fun onSwipeBottom()
}