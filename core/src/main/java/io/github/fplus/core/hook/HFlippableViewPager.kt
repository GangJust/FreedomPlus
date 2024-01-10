package io.github.fplus.core.hook

import android.view.MotionEvent
import com.freegang.ktutils.log.KLogCat
import com.ss.android.ugc.aweme.base.ui.FlippableViewPager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.FutureHook
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning

class HFlippableViewPager(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<FlippableViewPager>(lpparam) {
    companion object {
        const val TAG = "HFlippableViewPager"
    }

    private val config get() = ConfigV1.get()

    @FutureHook
    @OnBefore("onInterceptTouchEvent", "onTouchEvent", "dispatchTouchEvent")
    fun onTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            if (!config.isHideTab) return
            result = false // 禁止ViewPager左右滑动
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}