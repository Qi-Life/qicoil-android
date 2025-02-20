package com.Meditation.Sounds.frequencies.lemeor.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.Meditation.Sounds.frequencies.R

class CardViewShadow @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT // Không ảnh hưởng màu nền
        setShadowLayer(30f, 0f, 0f, Color.WHITE) // Bóng màu trắng
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.black_30)
        setShadowLayer(30f, 0f, 0f, Color.WHITE) // Bóng màu trắng
    }

    val shadowRect = RectF(30f, 30f, width - 30f, height - 30f)
    val backgroundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())


    private val cornerRadius = 40f // Bo tròn góc

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint) // Tránh lỗi khi vẽ bóng
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        shadowRect.set(0f - 10, 0f - 10f, width.toFloat() + 10f, height.toFloat() + 10f )
        backgroundRect.set(30f, 30f, width - 30f, height - 30f)
//        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)

        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, backgroundPaint)

    }
}