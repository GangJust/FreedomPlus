package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.interfaces.CallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookBlockRunning
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

@Deprecated("淘汰区域，删除倒计时中")
class HVideoPinchView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HVideoPinchView"

        @get:Synchronized
        @set:Synchronized
        var isVideoPinchView = false
    }

    val config = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.videoPinchViewClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (argsOrEmpty.size == 1) {
                if (args[0] !is String) return
                // KLogCat.d("退出专注模式")
                isVideoPinchView = false
            }
            if (method.name.contains("getMOriginView")) {
                // KLogCat.d("进入专注模式")
                isVideoPinchView = true
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {

    }
}