package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.ui.seekbar.SeekBarState
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HCustomizedUISeekBar(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HCustomizedUISeekBar"

        @get:Synchronized
        @set:Synchronized
        var action: SeekBarState.Action? = null
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.seekbar.CustomizedUISeekBar")
    }

    /*@OnAfter("setVisibility")
    fun setVisibilityAfter(params: XC_MethodHook.MethodHookParam, visibility: Int) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                // 全面屏手势沉浸式底部垫高 (进度条)，底部导航栏则不处理
                if (HDisallowInterceptRelativeLayout.isEdgeToEdgeEnabled) {
                    thisView.apply {
                        val lp = layoutParams as FrameLayout.LayoutParams?
                        lp?.updateMargins(bottom = context.dip2px(58f))
                        layoutParams = layoutParams
                    }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }*/

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            action = argsOrEmpty.firstOrNull()?.asOrNull<SeekBarState.Action>() ?: return
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}