package io.github.fplus.core.hook

import android.view.MotionEvent
import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.wrapper.CallMethods

class HVideoPlayerHelper(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HVideoPlayerHelper"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.videoPlayerHelperClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // 禁用双击点赞
            if (argsOrEmpty.firstOrNull()?.javaClass == MotionEvent::class.java) {
                if (!config.isDoubleClickType) return
                if (config.doubleClickType != 2) {
                    result = null
                }
                return
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {

    }
}