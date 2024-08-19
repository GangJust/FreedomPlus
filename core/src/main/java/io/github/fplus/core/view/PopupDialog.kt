package io.github.fplus.core.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import com.freegang.extension.activeActivity

open class PopupDialog : PopupWindow() {
    private var mBackground: Drawable = ColorDrawable(Color.parseColor("#55000000"))
    private val backgroundPopup = PopupWindow()

    init {
        // 初始化弹窗
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        isFocusable = true
        isClippingEnabled = false // 拓展到状态栏
        animationStyle = android.R.style.Animation_Dialog // dialog动画

        // 初始化背景PopupWindow
        backgroundPopup.width = WindowManager.LayoutParams.MATCH_PARENT
        backgroundPopup.height = WindowManager.LayoutParams.MATCH_PARENT
        isFocusable = true
        backgroundPopup.isClippingEnabled = false // 拓展到状态栏
    }

    @Deprecated("Use `setBackground` instead", ReplaceWith("this.setBackground(background)"))
    override fun setBackgroundDrawable(background: Drawable) {
        super.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        backgroundPopup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.mBackground = background
    }

    @Deprecated("Use `show` instead", ReplaceWith("this.show(parent,gravity,x,y)"))
    override fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
    }

    override fun dismiss() {
        // 关闭键盘
        val imm = contentView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(contentView.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)

        // 关闭背景
        backgroundPopup.dismiss()

        // 关闭弹窗
        super.dismiss()
    }

    open fun setView(view: View): PopupDialog {
        this.setContentView(view)
        return this
    }

    open fun setBackground(background: Drawable): PopupDialog {
        this.setBackgroundDrawable(background)
        return this
    }

    open fun setBackgroundColor(color: Int): PopupDialog {
        this.setBackgroundDrawable(ColorDrawable(color))
        return this
    }

    open fun show() {
        this.show(Gravity.CENTER, 0, 0)
    }

    open fun show(gravity: Int, offsetX: Int, offsetY: Int) {
        val activity = activeActivity
            ?: throw NullPointerException("`${this::class.java.name}#show()`错误, 无法获取到当前Activity!")
        val parentView: View = activity.window.decorView.findViewById(Window.ID_ANDROID_CONTENT)
        this.show(parentView, gravity, offsetX, offsetY)
    }

    open fun show(parent: View, gravity: Int, x: Int, y: Int) {
        if (isShowing)
            return

        // 显示背景
        backgroundPopup.contentView = View(parent.context).apply { background = mBackground }
        backgroundPopup.showAtLocation(parent, Gravity.CENTER, 0, 0)

        // 显示弹窗
        this.showAtLocation(parent, gravity, x, y)
    }
}