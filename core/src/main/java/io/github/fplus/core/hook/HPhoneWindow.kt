package io.github.fplus.core.hook

import android.content.Context
import android.graphics.Color
import com.freegang.extension.findMethodInvoke
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.HookEntity
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HPhoneWindow : HookEntity() {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.android.internal.policy.PhoneWindow")
    }

    private fun getContext(params: MethodParam): Context? {
        return params.thisObject!!.findMethodInvoke<Context> { name("getContext") }
    }

    @OnBefore("setStatusBarColor")
    fun setStatusBarColorBefore(params: MethodParam, color: Int) {
        hookBlockRunning(params) {
            if (getContext(this) is FreedomSettingActivity)
                return

            if (config.isImmersive) {
                args[0] = Color.TRANSPARENT
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("setNavigationBarColor")
    fun setNavigationBarColorBefore(params: MethodParam, color: Int) {
        hookBlockRunning(params) {
            if (getContext(this) is FreedomSettingActivity)
                return

            if (config.isImmersive) {
                args[0] = Color.TRANSPARENT
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}