package com.Meditation.Sounds.frequencies.utils

import android.animation.ValueAnimator
import android.view.View

object AnimateUtils {

    fun animateWidth(view: View, fromWidth: Int, toWidth: Int, animationDone: () -> Unit) {
        val animator = ValueAnimator.ofInt(fromWidth, toWidth)
        animator.setDuration(300)
        animator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.animatedValue as Int
            val params = view.layoutParams
            params.width = animatedValue
            view.layoutParams = params

            if(animatedValue == toWidth){
                animationDone.invoke()
            }
        }
        animator.start()
    }
}