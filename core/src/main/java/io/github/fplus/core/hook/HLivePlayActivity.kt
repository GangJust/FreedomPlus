package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.xpler.core.entity.FutureHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity

class HLivePlayActivity : BaseHook<Any>() {

    companion object {
        const val TAG = "HLivePlayActivity"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.live.LivePlayActivity")
    }

    @FutureHook
    @OnBefore("onWindowFocusChanged")
    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                ImmersiveHelper.immersive(
                    thisActivity,
                    hideStatusBar = true,
                    hideNavigationBars = true,
                )
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}