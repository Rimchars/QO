package com.example.qo

import android.view.View
import androidx.viewpager.widget.ViewPager

class StackPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            translationX = if (position > 0) {
                -width * position
            } else {
                width * position
            }
            scaleX = 1 - 0.3f * Math.abs(position)
            scaleY = 1 - 0.3f * Math.abs(position)
            alpha = 1 - 0.5f * Math.abs(position)
        }
    }
}