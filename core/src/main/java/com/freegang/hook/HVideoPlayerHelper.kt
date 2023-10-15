package com.freegang.hook

import android.view.MotionEvent
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.KtOnHook
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookBlockRunning
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoPlayerHelper(lpparam: XC_LoadPackage.LoadPackageParam) :
    KtOnHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HVideoPlayerHelper"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.videoPlayerHelperClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            // 禁用双击点赞
            if (argsOrEmpty.firstOrNull()?.javaClass == MotionEvent::class.java) {
                if (!config.isDoubleClickType) return
                if (config.doubleClickType != 2) {
                    result = null
                }
                return
            }
        }
    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {

    }
}