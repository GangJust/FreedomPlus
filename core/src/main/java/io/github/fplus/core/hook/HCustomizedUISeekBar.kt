package io.github.fplus.core.hook

import android.widget.FrameLayout
import androidx.core.view.updateMargins
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.ss.android.ugc.aweme.feed.ui.seekbar.SeekBarState
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallMethods
import io.github.xpler.core.thisView

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
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            action = argsOrEmpty.firstOrNull()?.asOrNull<SeekBarState.Action>() ?: return
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}