package com.example.myot.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class CustomRefreshView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#747886")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }

    private val arcRect = RectF()
    private val sweepAngle = 270f // 270도짜리 C자 원형

    private var rotateAnimator: ObjectAnimator? = null

    fun setProgress(ratio: Float) {
        if (rotateAnimator?.isRunning == true) return
        alpha = ratio
        scaleX = 0.5f + 0.5f * ratio
        scaleY = 0.5f + 0.5f * ratio
        invalidate()
    }

    fun startLoading() {
        if (rotateAnimator == null) {
            rotateAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f).apply {
                duration = 1000
                interpolator = LinearInterpolator()
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        }
    }

    fun reset() {
        rotateAnimator?.cancel()
        rotateAnimator = null
        rotation = 0f
        alpha = 0f
        scaleX = 1f
        scaleY = 1f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 10f
        val size = Math.min(width, height) - padding * 2
        val left = padding
        val top = padding
        val right = left + size
        val bottom = top + size

        arcRect.set(left, top, right, bottom)
        canvas.drawArc(arcRect, 0f, sweepAngle, false, paint)
    }
}