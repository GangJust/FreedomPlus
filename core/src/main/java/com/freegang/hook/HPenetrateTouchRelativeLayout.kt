package com.freegang.hook

import androidx.core.view.updatePadding
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.navigationBarHeight
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
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

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (config.isImmersive) {
                thisViewGroup.apply {
                    var bottomPadding = context.dip2px(58f) // BottomTabBarHeight
                    // 全面屏手势沉浸式底部垫高 (主屏幕控件)，底部导航栏则不处理
                    if (HDisallowInterceptRelativeLayout.isEdgeToEdgeEnabled) {
                        bottomPadding += context.navigationBarHeight
                    }
                    updatePadding(bottom = bottomPadding)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}