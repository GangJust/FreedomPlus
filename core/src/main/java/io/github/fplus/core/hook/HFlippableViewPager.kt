package io.github.fplus.core.hook

import android.view.MotionEvent
import com.ss.android.ugc.aweme.base.ui.FlippableViewPager
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HFlippableViewPager : BaseHook() {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return FlippableViewPager::class.java
    }

    @OnBefore("onInterceptTouchEvent", "onTouchEvent", "dispatchTouchEvent")
    fun onTouchEventBefore(
        params: MethodParam,
        event: MotionEvent,
    ) {
        hookBlockRunning(params) {
            if (!config.isHideTopTab) return
            setResult(false) // 禁止ViewPager左右滑动
        }.onFailure {
            XplerLog.e(it)
        }
    }
}