package com.freegang.view


import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout

class OverlayView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val mScreenWidth = context.resources.displayMetrics.widthPixels
    private val mScreenHeight = context.resources.displayMetrics.heightPixels
    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var mLayoutParams: WindowManager.LayoutParams? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        super.setLayoutParams(params)
        mLayoutParams = WindowManager.LayoutParams(
            params.width,
            params.height,
            WindowManager.LayoutParams.FIRST_SUB_WINDOW,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.RGBA_8888,
        )
    }

    fun setContentView(view: View) {
        addView(view)
    }

    fun show() {
        show(Gravity.CENTER, 0, 0)
    }

    fun show(gravity: Int, x: Int, y: Int) {
        mLayoutParams?.runCatching {
            this.gravity = gravity
            this.x = x
            this.y = y
            mWindowManager.addView(this@OverlayView, this)
        }
    }
}