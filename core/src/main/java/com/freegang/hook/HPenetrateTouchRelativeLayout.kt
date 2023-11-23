package com.freegang.hook

import androidx.core.view.updatePadding
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.interfaces.CallMethods
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HPenetrateTouchRelativeLayout(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<PenetrateTouchRelativeLayout>(lpparam), CallMethods {
    companion object {
        const val TAG = "HPenetrateTouchRelativeLayout"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                thisViewGroup.apply {
                    val bottomPadding = context.dip2px(58f) // BottomTabBarHeight
                    updatePadding(bottom = bottomPadding)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}