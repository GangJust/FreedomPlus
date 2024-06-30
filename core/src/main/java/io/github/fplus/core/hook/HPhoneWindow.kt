package io.github.fplus.core.hook

import android.graphics.Color
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.HookEntity
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HPhoneWindow : HookEntity() {
    companion object {
        const val TAG = "HPhoneWindow"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.android.internal.policy.PhoneWindow")
    }

    @OnBefore("setStatusBarColor")
    fun setStatusBarColorBefore(params: XC_MethodHook.MethodHookParam, color: Int) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                args[0] = Color.TRANSPARENT
            }
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    @OnBefore("setNavigationBarColor")
    fun setNavigationBarColorBefore(params: XC_MethodHook.MethodHookParam, color: Int) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                args[0] = Color.TRANSPARENT
            }
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }
}