package com.freegang.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.view.isVisible
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

class HMainBottomTabView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HMainBottomTabView"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.mainBottomTabViewClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (method.name.contains(Regex("Background|Alpha|Enabled"))) return
            if (argsOrEmpty.size != 1) return
            if (args.first()?.javaClass?.isPrimitiveObjectType == false) return
            // KLogCat.d(TAG, "更新方法: $method")

            // 底部导航栏透明度
            if (config.isTranslucent) {
                val alphaValue = config.translucentValue[2] / 100f
                thisViewGroup.parentView?.alpha = alphaValue
                thisViewGroup.parentView?.isVisible = alphaValue > 0
            }

            // 底部导航栏全局沉浸式
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