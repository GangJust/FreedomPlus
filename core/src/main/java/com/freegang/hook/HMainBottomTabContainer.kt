package com.freegang.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.extension.isPrimitiveObjectType
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.parentView
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HMainBottomTabContainer(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam), CallMethods {
    private val TAG = "HMainBottomTabContainer"
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.mainBottomTabViewClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (method.name.contains(Regex("Background|Alpha"))) return
            if (argsOrEmpty.size != 1) return
            if (args.first()?.javaClass?.isPrimitiveObjectType == false) return
            //KLogCat.d(TAG, "更新方法: $method")

            // 半透明
            if (config.isTranslucent) {
                thisViewGroup.parentView?.alpha = 0.5f
            }

            // 全屏沉浸式
            if (config.isImmersive) {
                thisViewGroup.parentView?.background = ColorDrawable(Color.TRANSPARENT)
                thisViewGroup.traverse {
                    background = ColorDrawable(Color.TRANSPARENT)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}