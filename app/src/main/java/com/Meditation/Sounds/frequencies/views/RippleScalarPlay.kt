package com.Meditation.Sounds.frequencies.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator

class RippleScalarPlay : View {
    private var color: String = "#0FFF50"
    private var fadedColor: String = "#000FFF50"

    private var mBubbleMaxRadiusInPx = 0f
    private var mBubbleMinRadiusInPx = 0f
    private var mVariablePaint: Paint? = null
    private var mPaint: Paint? = null

    private var mFadingCircleRadius = 0f
    private var mColoredCircleRadius = 0f

    private var colorAnimator: ValueAnimator? = null
    private var radiusAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mBubbleMaxRadiusInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, BUBBLE_MAX_RADIUS_IN_DP.toFloat(), resources.displayMetrics
        )
        mBubbleMinRadiusInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, BUBBLE_MIN_RADIUS_IN_DP.toFloat(), resources.displayMetrics
        )

        mPaint = Paint()
        mPaint?.style = Paint.Style.FILL
        mPaint?.color = Color.parseColor(color)
        mColoredCircleRadius = mBubbleMinRadiusInPx

        mVariablePaint = Paint()
        mVariablePaint?.style = Paint.Style.FILL
        mVariablePaint?.color = Color.parseColor(color)
        mFadingCircleRadius = mBubbleMinRadiusInPx
        startAnimating()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //fading circle
        canvas.drawCircle(
            mBubbleMaxRadiusInPx, mBubbleMaxRadiusInPx, mFadingCircleRadius, mVariablePaint!!
        )
        //draw colored circle
        canvas.drawCircle(
            mBubbleMaxRadiusInPx, mBubbleMaxRadiusInPx, mColoredCircleRadius, mPaint!!
        )
    }

    fun startAnimating() {
        colorAnimator = ValueAnimator.ofArgb(
            Color.parseColor(color), Color.parseColor(
                fadedColor
            )
        )
        colorAnimator?.addUpdateListener { valueAnimator ->
            mVariablePaint?.color = (valueAnimator.animatedValue as Int)
        }

        colorAnimator?.repeatCount = REPEAT_COUNT
        colorAnimator?.repeatMode = ValueAnimator.RESTART
        colorAnimator?.setDuration(ANIMATION_TIME.toLong())
        colorAnimator?.interpolator = DecelerateInterpolator()
        colorAnimator?.start()


        radiusAnimator = ValueAnimator.ofFloat(mBubbleMinRadiusInPx, mBubbleMaxRadiusInPx)
        radiusAnimator?.addUpdateListener { valueAnimator ->
            mFadingCircleRadius = valueAnimator.animatedValue as Float
            mColoredCircleRadius = mFadingCircleRadius / 5
            invalidate()
        }
        radiusAnimator?.repeatMode = ValueAnimator.RESTART
        radiusAnimator?.repeatCount = REPEAT_COUNT
        radiusAnimator?.setDuration(ANIMATION_TIME.toLong())
        radiusAnimator?.interpolator = DecelerateInterpolator()
        radiusAnimator?.start()
    }

    companion object {
        const val BUBBLE_MAX_RADIUS_IN_DP: Int = 22
        const val BUBBLE_MIN_RADIUS_IN_DP: Int = 2
        const val ANIMATION_TIME: Int = 2000
        private const val REPEAT_COUNT = 1800
    }
}