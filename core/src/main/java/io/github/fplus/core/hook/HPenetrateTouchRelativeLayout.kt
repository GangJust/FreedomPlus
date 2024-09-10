package io.github.fplus.core.hook

import android.view.View
import androidx.core.view.updatePadding
import com.freegang.extension.dip2px
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.CallMethods
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.core.thisViewGroup

class HPenetrateTouchRelativeLayout : BaseHook(), CallMethods {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return PenetrateTouchRelativeLayout::class.java
    }

    @OnBefore("setVisibility")
    fun setVisibilityBefore(params: MethodParam, visibility: Int) {
        hookBlockRunning(params) {
            if (!config.isNeatMode) {
                return
            }

            if (!config.neatModeState) {
                return
            }

            if (visibility == View.GONE/* || visibility == View.INVISIBLE*/) {
                return
            }

            if (HPlayerController.isPlaying) {
                args[0] = View.GONE
                HMainActivity.toggleView(false)
            } else {
                args[0] = View.VISIBLE
                HMainActivity.toggleView(true)
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore
    fun methodBefore(params: MethodParam, visibility: Int, string: String?) {
        hookBlockRunning(params) {
            setVisibilityBefore(params, visibility)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    override fun callOnBeforeMethods(params: MethodParam) {

    }

    override fun callOnAfterMethods(params: MethodParam) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                thisViewGroup.apply {
                    val bottomPadding = 58f.dip2px() // BottomTabBarHeight
                    updatePadding(bottom = bottomPadding)
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

}