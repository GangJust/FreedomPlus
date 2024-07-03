package io.github.fplus.core.view

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import com.freegang.extension.activeActivity
import io.github.xpler.core.log.XplerLog

class KDialog : PopupWindow() {
    init {
        this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 取消默认背景色(设置透明)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        isFocusable = true // 允许响应焦点
        isClippingEnabled = false // 扩展到状态栏
        animationStyle = android.R.style.Animation_Dialog // dialog动画
    }

    fun setView(contentView: View): KDialog {
        super.setContentView(contentView)
        return this
    }

    val context: Context
        get() = contentView.context

    override fun dismiss() {
        // 关闭键盘
        val imm = contentView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(contentView.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)

        // 关闭动画
        ValueAnimator.ofFloat(0.5f, 1f).apply {
            duration = 300L
            addUpdateListener {
                setBackgroundAlpha(it.animatedValue as Float)
            }
            start()
        }

        // 关闭弹窗
        super.dismiss()
    }

    fun show() {
        this.show(Gravity.CENTER, 0, 0)
    }

    fun show(gravity: Int, offsetX: Int, offsetY: Int) {
        // 父布局, 默认为Android根布局
        try {
            val activity = activeActivity
                ?: throw NullPointerException("`${this::class.java.name}#show()`错误, 无法获取到当前Activity!")
            val parentView: View = activity.window.decorView.findViewById(Window.ID_ANDROID_CONTENT)
            this.show(parentView, gravity, offsetX, offsetY)
        } catch (e: Exception) {
            XplerLog.e("`${this::class.java.name}#show()`错误:\n${e.stackTraceToString()}")
        }
    }

    fun show(parentView: View, gravity: Int, x: Int, y: Int) {
        try {
            if (isShowing)
                return

            // 显示动画
            ValueAnimator.ofFloat(1f, 0.5f).apply {
                duration = 300L
                addUpdateListener {
                    setBackgroundAlpha(it.animatedValue as Float)
                }
                start()
            }

            // 显示弹窗
            this.showAtLocation(parentView, gravity, x, y)
        } catch (e: Exception) {
            XplerLog.e("`${this::class.java.name}#show()`错误:\n${e.stackTraceToString()}")
        }
    }

    private fun setBackgroundAlpha(alpha: Float) {
        val window = (contentView.context as? Activity)?.window
        val layoutParams = window?.attributes
        layoutParams?.alpha = alpha
        window?.attributes = layoutParams
    }
}