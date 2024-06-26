package io.github.fplus.core.hook

import android.view.View
import androidx.core.view.updatePadding
import com.freegang.extension.dip2px
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup
import io.github.xpler.core.wrapper.CallMethods

class HPenetrateTouchRelativeLayout : BaseHook(), CallMethods {
    companion object {
        const val TAG = "HPenetrateTouchRelativeLayout"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return PenetrateTouchRelativeLayout::class.java
    }

    @OnBefore("setVisibility")
    fun setVisibilityBefore(params: XC_MethodHook.MethodHookParam, visibility: Int) {
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
    fun methodBefore(params: XC_MethodHook.MethodHookParam, visibility: Int, string: String?) {
        hookBlockRunning(params) {
            setVisibilityBefore(params, visibility)
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
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