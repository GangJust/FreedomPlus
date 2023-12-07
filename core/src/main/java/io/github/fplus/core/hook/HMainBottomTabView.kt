package io.github.fplus.core.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.freegang.ktutils.extension.isPrimitiveObjectType
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.parentView
import com.freegang.ktutils.view.traverse
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.NoneHook
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallMethods
import io.github.xpler.core.thisViewGroup

class HMainBottomTabView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HMainBottomTabView"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.mainBottomTabViewClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {


    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (method.name.contains(Regex("Background|Alpha|Enabled"))) return
            if (argsOrEmpty.size != 1) return
            if (args.first()?.javaClass?.isPrimitiveObjectType == false) return
            // KLogCat.d(TAG, "更新方法: $method")

            // 底部导航栏透明度
            if (config.isTranslucent) {
                val alphaValue = config.translucentValue[2] / 100f
                thisViewGroup.parentView?.alpha = alphaValue
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