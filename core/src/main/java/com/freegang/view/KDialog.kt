package com.freegang.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.IntDef
import com.freegang.xpler.utils.log.KLogCat


class KDialog(private val context: Context) : PopupWindow(context) {

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
        super.dismiss()
    }

    fun show() {
        this.show(Gravity.CENTER, 0, 0)
    }

    fun show(@PopupWindowGravity gravity: Int, offsetX: Int, offsetY: Int) {
        //父布局, 默认为Android根布局
        try {
            val parentView: View = (context as Activity).window.decorView.findViewById(android.R.id.content)
            show(parentView, gravity, offsetX, offsetY)
        } catch (e: Exception) {
            KLogCat.e("`${this::class.java.name}#show()`错误:\n${e.stackTraceToString()}")
        }
    }

    fun show(parentView: View, @PopupWindowGravity gravity: Int, x: Int, y: Int) {
        try {
            showAtLocation(parentView, gravity, x, y)
        } catch (e: Exception) {
            KLogCat.e("`${this::class.java.name}#show()`错误:\n${e.stackTraceToString()}")
        }
    }

    //定位注解
    @IntDef(*[Gravity.TOP, Gravity.BOTTOM, Gravity.START, Gravity.END, Gravity.CENTER_VERTICAL, Gravity.FILL_VERTICAL, Gravity.CENTER_HORIZONTAL, Gravity.FILL_HORIZONTAL, Gravity.CENTER, Gravity.FILL, Gravity.CLIP_VERTICAL, Gravity.CLIP_HORIZONTAL])
    annotation class PopupWindowGravity
}