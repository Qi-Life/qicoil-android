package com.Meditation.Sounds.frequencies.utils.extensions

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation


fun View.showViewWithFadeIn() {
    this.visibility = View.VISIBLE
    val fadeIn = AlphaAnimation(0f, 1f).apply {
        duration = 3000
        fillAfter = true
    }
    this.startAnimation(fadeIn)
}