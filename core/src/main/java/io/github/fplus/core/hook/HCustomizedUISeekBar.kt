package io.github.fplus.core.hook

import android.widget.FrameLayout
import androidx.core.view.updateMargins
import com.freegang.extension.dip2px
import com.ss.android.ugc.aweme.feed.ui.seekbar.SeekBarState
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisView

class HCustomizedUISeekBar : BaseHook<Any>() {
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

    // @OnAfter("setAlpha")
    fun setAlphaAfter(params: XC_MethodHook.MethodHookParam, alpha: Float) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                thisView.apply {
                    val lp = layoutParams as FrameLayout.LayoutParams?
                    lp?.updateMargins(context.dip2px(58f))
                    layoutParams = layoutParams
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter
    fun methodAfter(params: XC_MethodHook.MethodHookParam, action: SeekBarState.Action?) {
        hookBlockRunning(params) {
            HCustomizedUISeekBar.action = action
        }.onFailure {
            XplerLog.e(it)
        }
    }
}