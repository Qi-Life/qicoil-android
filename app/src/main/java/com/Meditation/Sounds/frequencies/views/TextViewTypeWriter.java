package com.Meditation.Sounds.frequencies.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class TextViewTypeWriter extends androidx.appcompat.widget.AppCompatTextView {

    private CharSequence mText;
    private int mIndex;
    OnTypeWriterCompleteListener completeListener;


    public TextViewTypeWriter(Context context) {
        super(context);
    }

    public TextViewTypeWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if (mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, 10);
                completeListener.setOnCompleteListener();
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;
        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, 10);
    }

    public void setOnTypeWriterCompleteListener(OnTypeWriterCompleteListener listener){
        completeListener = listener;
    }

    public interface OnTypeWriterCompleteListener {
        void setOnCompleteListener();
    }
}