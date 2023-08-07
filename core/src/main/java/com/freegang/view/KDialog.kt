package com.freegang.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import com.freegang.ktutils.app.activeActivity
import com.freegang.ktutils.log.KLogCat

class KDialog : PopupWindow() {

    init {
        this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //取消默认背景色(设置透明)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        isFocusable = true //允许响应焦点
        isClippingEnabled = false //扩展到状态栏
        animationStyle = android.R.style.Animation_Dialog //dialog动画
    }

    fun setView(contentView: View): KDialog {
        super.setContentView(contentView)
        return this
    }

    override fun dismiss() {
        if (!isShowing) return
        //关闭键盘
        val imm = contentView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(contentView.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        super.dismiss()
    }

    fun show() {
        if (isShowing) return
        this.show(Gravity.CENTER, 0, 0)
    }

    fun show(gravity: Int, offsetX: Int, offsetY: Int) {
        //父布局, 默认为Android根布局
        try {
            val activity = activeActivity
                ?: throw NullPointerException("`${this::class.java.name}#show()`错误, 无法获取到当前Activity!")
            val parentView: View = activity.window.decorView.findViewById(Window.ID_ANDROID_CONTENT)
            show(parentView, gravity, offsetX, offsetY)
        } catch (e: Exception) {
            e.printStackTrace()
            KLogCat.e("`${this::class.java.name}#show()`错误:\n${e.stackTraceToString()}")
        }
    }

    fun show(parentView: View, gravity: Int, x: Int, y: Int) {
        try {
            showAtLocation(parentView, gravity, x, y)
        } catch (e: Exception) {
            e.printStackTrace()
            KLogCat.e("`${this::class.java.name}#show()`错误:\n${e.stackTraceToString()}")
        }
    }
}