package com.freegang.hook

import android.view.MotionEvent
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.base.ui.FlippableViewPager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HFlippableViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<FlippableViewPager>(lpparam) {
    companion object {
        const val TAG = "HFlippableViewPager"
    }

    private val config get() = ConfigV1.get()

    @OnBefore("onInterceptTouchEvent", "onTouchEvent", "dispatchTouchEvent")
    fun onTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            if (!config.isHideTab) return
            result = false // 禁止ViewPager左右滑动
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }
}