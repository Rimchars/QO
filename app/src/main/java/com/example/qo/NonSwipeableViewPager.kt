package com.example.qo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    var isSwipeable = true
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return isSwipeable && super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return isSwipeable && super.onTouchEvent(event)
    }
}