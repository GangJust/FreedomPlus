package io.github.fplus.core.hook

import com.ss.android.ugc.aweme.live.LivePlayActivity
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.xpler.core.XplerLog
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.core.thisActivity

class HLivePlayActivity : BaseHook() {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return LivePlayActivity::class.java
    }

    @OnBefore("onWindowFocusChanged")
    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: MethodParam, boolean: Boolean) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                ImmersiveHelper.immersive(
                    thisActivity,
                    hideStatusBar = true,
                    hideNavigationBars = true,
                )
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}