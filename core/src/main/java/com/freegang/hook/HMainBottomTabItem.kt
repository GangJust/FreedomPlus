package com.freegang.hook

import android.view.View
import android.widget.ImageView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.KtOnCallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.thisView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HMainBottomTabItem(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam), KtOnCallMethods {
    override fun setTargetClass(): Class<*> {
        return DouYinMain.mainBottomTabItemClazz ?: NoneHook::class.java
    }

    val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlock(param) {
            changeValue(thisView)
        }
    }

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    private fun changeValue(view: View) {
        if (!config.isHidePhotoButton) return

        view.traverse {
            if ("${it.contentDescription}".contains(Regex("拍摄|道具"))) {
                if (it is ImageView) {
                    it.setImageDrawable(null)
                    it.background = null
                    it.foreground = null
                }

                if (!config.isDisablePhotoButton) return@traverse
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