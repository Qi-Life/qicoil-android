package com.Meditation.Sounds.frequencies.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.Meditation.Sounds.frequencies.R

class ScalarAlbumAnimation : FrameLayout {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthDefault
    }
    private var strokeWidthDefault = 15f
    private var strokeColor = Color.TRANSPARENT
    private val rectF = RectF()
    private var strokeAnimator: ValueAnimator? = null
    private var colorAnimator: ValueAnimator? = null
    private var glowAnimator: ValueAnimator? = null
    private val padding = 12f
    private var radius = 20f
    private var glowRadius = 0f

    constructor(context: Context) : super(context, null) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    internal fun init(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        radius = context.resources.getDimension(R.dimen.corner_radius_album)

        paint.strokeWidth = strokeWidthDefault
        paint.color = strokeColor
        paint.setShadowLayer(glowRadius, 0f, 0f, Color.WHITE)
        rectF.set(
            padding,
            padding,
            width.toFloat() - padding,
            height.toFloat() - padding
        )
        canvas.drawRoundRect(rectF, radius, radius, paint)
    }

    private fun startStrokeAnimation() {
        strokeAnimator?.end()
        strokeAnimator = null
        strokeAnimator = ValueAnimator.ofFloat(15f, 0f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                strokeWidthDefault = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startColorAnimation() {
        colorAnimator?.end()
        colorAnimator = null
        colorAnimator = ValueAnimator.ofArgb(Color.parseColor("#80ADFF"), Color.parseColor("#3380ADFF")).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                strokeColor = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    private fun startGlowAnimation() {
        glowAnimator?.end()
        glowAnimator = null
        glowAnimator = ValueAnimator.ofFloat(0f, 10f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                glowRadius = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

     fun startAnimation() {
        clearAnimation()
        startStrokeAnimation()
        startColorAnimation()
        startGlowAnimation()
    }

    override fun clearAnimation() {
        strokeAnimator?.end()
        colorAnimator?.end()
        glowAnimator?.end()
        paint.color = Color.TRANSPARENT
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        strokeAnimator?.cancel()
        colorAnimator?.cancel()
        glowAnimator?.cancel()
    }
}