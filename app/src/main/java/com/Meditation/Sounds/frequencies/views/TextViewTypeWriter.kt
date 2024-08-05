package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

class TextViewTypeWriter : AppCompatTextView {
    private var mText: CharSequence? = null
    private var mIndex = 0
    var completeListener: OnTypeWriterCompleteListener? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    @Suppress("DEPRECATION")
    private val mHandler = Handler()
    private val characterAdder: Runnable = object : Runnable {
        override fun run() {
            text = mText?.subSequence(0, mIndex++)
            if (mIndex <= (mText?.length ?: 0)) {
                mHandler.postDelayed(this, 5)
                completeListener?.setOnTypingListener()
            } else {
                completeListener?.setOnCompleteListener()
            }
        }
    }

    fun animateText(text: CharSequence?) {
        mText = text
        mIndex = 0
        setText("")
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, 5)
    }

    fun setOnTypeWriterCompleteListener(listener: OnTypeWriterCompleteListener) {
        completeListener = listener
    }

    fun cancelWriter() {
        mHandler.removeCallbacks(characterAdder)
        completeListener?.setOnCompleteListener()
    }

    interface OnTypeWriterCompleteListener {
        fun setOnTypingListener()
        fun setOnCompleteListener()
    }
}