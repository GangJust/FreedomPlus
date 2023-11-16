package com.freegang.hook

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.interfaces.CallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage


class HMainBottomTabItem(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HMainBottomTabItem"
    }

    val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.mainBottomTabItemClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            isHidePhotoButton(thisView)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun isHidePhotoButton(view: View) {
        if (!config.isHidePhotoButton) return
        view.traverse {
            if ("$contentDescription".contains(Regex("拍摄|道具"))) {
                // 隐藏按钮
                if (config.photoButtonType == 2) {
                    view.isVisible = false
                    return@traverse
                }

                // 占位按钮, 移除加号图标
                if (this is ImageView) {
                    setImageDrawable(null)
                    background = null
                    foreground = null
                }

                // 允许拍摄直接结束逻辑
                if (config.photoButtonType == 0) {
                    view.isVisible = true
                    return@traverse
                }

                // 不允许拍摄
                view.setOnClickListener {
                    KToastUtils.show(it.context, "已禁止拍摄")
                }
                view.setOnLongClickListener {
                    KToastUtils.show(it.context, "已禁止拍摄")
                    true
                }
            }
        }
    }
}