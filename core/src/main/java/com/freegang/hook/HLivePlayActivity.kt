package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.ImmersiveHelper
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HLivePlayActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {

    companion object {
        const val TAG = "HLivePlayActivity"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.live.LivePlayActivity")
    }

    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        hookBlockRunning(params) {
            ImmersiveHelper.with(thisActivity, config)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}