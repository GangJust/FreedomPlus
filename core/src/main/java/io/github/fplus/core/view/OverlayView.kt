package io.github.fplus.core.view

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.LayoutRes

class OverlayView : LinearLayout {
    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mParams = createLayoutParams()

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.format = PixelFormat.TRANSLUCENT
        params.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        return params
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        mParams.width = params.width
        mParams.height = params.height
        super.setLayoutParams(params)
    }

    fun setContentView(view: View) {
        removeAllViews()
        addView(view)
    }

    fun setContentView(@LayoutRes rsId: Int) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(rsId, null)
        setContentView(view)
    }

    @JvmOverloads
    fun show(
        gravity: Int = Gravity.CENTER,
        offsetX: Int = 0,
        offsetY: Int = 0,
    ) {
        mParams.gravity = gravity
        mParams.x = offsetX
        mParams.y = offsetY
        mWindowManager.addView(this@OverlayView, mParams)
    }
}