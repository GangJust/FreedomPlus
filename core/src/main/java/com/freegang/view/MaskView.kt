package com.freegang.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout

class MaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var mGestureDetector: GestureDetector? = null

    fun setOnGestureListener(l: GestureDetector.SimpleOnGestureListener) {
        this.mGestureDetector = GestureDetector(context, l)
    }

    init {
        layoutParams = LayoutParams(-1, -1)
        contentDescription = "FreedomMaskView"
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return mGestureDetector?.onTouchEvent(ev!!) ?: super.dispatchTouchEvent(ev)
    }

}