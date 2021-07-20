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

@file:Suppress("unused")

package com.force.codes.mynotes.core.widget.calendar

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import com.force.mynotes.core.R

class ExpandIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    @IntDef(MORE, LESS, INTERMEDIATE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class State

    @State
    private var state = 0
    private var mAlpha = MORE_STATE_ALPHA
    private var centerTranslation = 0f

    @FloatRange(from = 0.0, to = 1.0)
    private var fraction = 0f
    private var animationSpeed: Float
    private var switchColor = false
    private var color = Color.BLACK
    private var colorMore = 0
    private var colorLess = 0
    private var colorIntermediate = 0
    private var paint: Paint? = null
    private val left: Point = Point()
    private val right: Point = Point()
    private val center = Point()
    private val tempLeft = Point()
    private val tempRight = Point()
    private var useDefaultPadding = false
    private var padding = 0
    private val path = Path()
    private var arrowAnimator: ValueAnimator? = null

    fun setColor(color: Int) {
        colorLess = color
        colorMore = color
        colorIntermediate = color
        invalidate()
    }

    /**
     * Changes state and updates view
     *
     * @param animate Indicates thaw state will be changed with animation or not
     */
    @JvmOverloads
    fun switchState(animate: Boolean = true) {
        val newState: Int = when (state) {
            MORE -> LESS
            LESS -> MORE
            INTERMEDIATE -> finalStateByFraction
            else -> throw IllegalArgumentException("Unknown state [$state]")
        }
        setState(newState, animate)
    }

    /**
     * Set one of two states and updates view
     *
     * @param state   [.MORE] or [.LESS]
     * @param animate Indicates thaw state will be changed with animation or not
     * @throws IllegalArgumentException if {@param state} is invalid
     */
    fun setState(@State state: Int, animate: Boolean) {
        this.state = state
        fraction = if (state == MORE) {
            0f
        } else if (state == LESS) {
            1f
        } else {
            throw IllegalArgumentException("Unknown state, must be one of STATE_MORE = 0,  STATE_LESS = 1")
        }
        updateArrow(animate)
    }

    /**
     * Set current fraction for arrow and updates view
     *
     * @param fraction Must be value from 0f to 1f [.MORE] state value is 0f, [.LESS]
     * state value is 1f
     * @throws IllegalArgumentException if fraction is less than 0f or more than 1f
     */
    fun setFraction(@FloatRange(from = 0.0, to = 1.0) fraction: Float, animate: Boolean) {
        require(!(fraction < 0f || fraction > 1f)) { "Fraction value must be from 0 to 1f, fraction=$fraction" }
        if (this.fraction == fraction) {
            return
        }
        this.fraction = fraction
        state = if (fraction == 0f) {
            MORE
        } else if (fraction == 1f) {
            LESS
        } else {
            INTERMEDIATE
        }
        updateArrow(animate)
    }

    /**
     * Set duration of icon animation from state [.MORE] to state [.LESS]
     *
     * @param animationDuration
     */
    fun setAnimationDuration(animationDuration: Long) {
        animationSpeed = DELTA_ALPHA / animationDuration
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(0f, centerTranslation)
        paint?.let { canvas.drawPath(path, it) }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        calculateArrowMetrics(width, height)
        updateArrowPath()
    }

    private fun calculateArrowMetrics(width: Int, height: Int) {
        val arrowMaxWidth = if (height >= width) width else height
        if (useDefaultPadding) {
            padding = (PADDING_PROPORTION * arrowMaxWidth).toInt()
        }
        val arrowWidth = arrowMaxWidth - 2 * padding
        val thickness: Float = (arrowWidth * THICKNESS_PROPORTION)
        paint?.strokeWidth = thickness
        center[width / 2] = height / 2
        left[center.x - arrowWidth / 2] = center.y
        right[center.x + arrowWidth / 2] = center.y
    }

    private fun updateArrow(animate: Boolean) {
        val toAlpha = MORE_STATE_ALPHA + fraction * DELTA_ALPHA
        if (animate) {
            animateArrow(toAlpha)
        } else {
            cancelAnimation()
            alpha = toAlpha
            if (switchColor) {
                updateColor(ArgbEvaluator())
            }
            updateArrowPath()
            invalidate()
        }
    }

    private fun updateArrowPath() {
        path.reset()
        rotate(left, -alpha.toDouble(), tempLeft)
        rotate(right, alpha.toDouble(), tempRight)
        centerTranslation = ((center.y - tempLeft.y) / 2).toFloat()
        path.moveTo(tempLeft.x.toFloat(), tempLeft.y.toFloat())
        path.lineTo(center.x.toFloat(), center.y.toFloat())
        path.lineTo(tempRight.x.toFloat(), tempRight.y.toFloat())
    }

    private fun animateArrow(toAlpha: Float) {
        cancelAnimation()
        val valueAnimator = ValueAnimator.ofFloat(alpha, toAlpha)
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            private val colorEvaluator = ArgbEvaluator()
            override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
                alpha = valueAnimator.animatedValue as Float
                updateArrowPath()
                if (switchColor) {
                    updateColor(colorEvaluator)
                }
                postInvalidateOnAnimationCompat()
            }
        })
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = calculateAnimationDuration(toAlpha)
        valueAnimator.start()
        arrowAnimator = valueAnimator
    }

    private fun cancelAnimation() {
        if (arrowAnimator != null && arrowAnimator!!.isRunning) {
            arrowAnimator!!.cancel()
        }
    }

    private fun updateColor(colorEvaluator: ArgbEvaluator) {
        val fraction: Float
        val colorFrom: Int
        val colorTo: Int
        if (colorIntermediate != -1) {
            colorFrom = if (alpha <= 0f) colorMore else colorIntermediate
            colorTo = if (alpha <= 0f) colorIntermediate else colorLess
            fraction = if (alpha <= 0) 1 + alpha / 45f else alpha / 45f
        } else {
            colorFrom = colorMore
            colorTo = colorLess
            fraction = (alpha + 45f) / 90f
        }
        color = colorEvaluator.evaluate(fraction, colorFrom, colorTo) as Int
        paint?.color = color
    }

    private fun calculateAnimationDuration(toAlpha: Float): Long {
        return (Math.abs(toAlpha - alpha) / animationSpeed).toLong()
    }

    private fun rotate(startPosition: Point, degrees: Double, target: Point) {
        val angle = Math.toRadians(degrees)
        val x = (center.x + (startPosition.x - center.x) * Math.cos(angle) -
                (startPosition.y - center.y) * Math.sin(angle)).toInt()
        val y =
            (center.y + (startPosition.x - center.x) * Math.sin(angle) + (startPosition.y - center.y) * Math.cos(
                angle
            )).toInt()
        target[x] = y
    }

    @get:State
    private val finalStateByFraction: Int
        get() = if (fraction <= .5f) {
            MORE
        } else {
            LESS
        }

    private fun postInvalidateOnAnimationCompat() {
        postInvalidateOnAnimation()
    }

    companion object {
        private const val MORE_STATE_ALPHA = -45f
        private const val LESS_STATE_ALPHA = 45f
        private const val DELTA_ALPHA = 90f
        private const val THICKNESS_PROPORTION = 5f / 36f
        private const val PADDING_PROPORTION = 4f / 24f
        private const val DEFAULT_ANIMATION_DURATION: Long = 150
        const val MORE = 0
        const val LESS = 1
        private const val INTERMEDIATE = 2
    }

    init {
        val array = getContext().theme.obtainStyledAttributes(
            attrs,
            R.styleable.ExpandIconView,
            0, 0
        )
        val roundedCorners: Boolean
        val animationDuration: Long
        try {
            roundedCorners = array.getBoolean(R.styleable.ExpandIconView_eiv_roundedCorners, false)
            switchColor = array.getBoolean(R.styleable.ExpandIconView_eiv_switchColor, false)
            color = array.getColor(R.styleable.ExpandIconView_eiv_color, Color.BLACK)
            colorMore = array.getColor(R.styleable.ExpandIconView_eiv_colorMore, Color.BLACK)
            colorLess = array.getColor(R.styleable.ExpandIconView_eiv_colorLess, Color.BLACK)
            colorIntermediate = array.getColor(R.styleable.ExpandIconView_eiv_colorIntermediate, -1)
            animationDuration = array.getInteger(
                R.styleable.ExpandIconView_eiv_animationDuration,
                DEFAULT_ANIMATION_DURATION.toInt()
            ).toLong()
            padding = array.getDimensionPixelSize(R.styleable.ExpandIconView_eiv_padding, -1)
            useDefaultPadding = padding == -1
        } finally {
            array.recycle()
        }
        run {
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint?.color = color
            paint?.style = Paint.Style.STROKE
            paint?.isDither = true
            if (roundedCorners) {
                paint?.strokeJoin = Paint.Join.ROUND
                paint?.strokeCap = Paint.Cap.ROUND
            }
        }
        animationSpeed = DELTA_ALPHA / animationDuration
        setState(MORE, false)
    }
}