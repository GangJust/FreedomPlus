package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.xpler.core.KtOnCallMethods
import com.ss.android.ugc.aweme.base.ui.FlippableViewPager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HFlippableViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<FlippableViewPager>(lpparam), KtOnCallMethods {
    private val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlock(param) {
            // 禁止ViewPager左右滑动
            if (!config.isHideTab) return@hookBlock
            if (method.name.contains("onInterceptTouchEvent|onTouchEvent|dispatchHoverEvent".toRegex())) {
                result = false
            }
        }
    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {

    }
}