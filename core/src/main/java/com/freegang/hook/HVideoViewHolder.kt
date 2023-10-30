package com.freegang.hook

import android.view.View
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolder(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HVideoViewHolder"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder")
    }

    @OnBefore("isCleanMode")
    fun isCleanModeBefore(params: XC_MethodHook.MethodHookParam, view: View?, boolean: Boolean) {
        hookBlockRunning(params) {
            if (config.isNeatMode) {
                args[1] = config.neatModeState
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}