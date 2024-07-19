package io.github.fplus.core.hook

import android.view.MotionEvent
import com.ss.android.ugc.aweme.base.ui.FlippableViewPager
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HFlippableViewPager : BaseHook() {
    companion object {
        const val TAG = "HFlippableViewPager"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return FlippableViewPager::class.java
    }

    @OnBefore("onInterceptTouchEvent", "onTouchEvent", "dispatchTouchEvent")
    fun onTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            if (!config.isHideTopTab) return
            result = false // 禁止ViewPager左右滑动
        }.onFailure {
            XplerLog.e(it)
        }
    }
}