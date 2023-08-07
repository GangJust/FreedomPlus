package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.base.ui.FlippableViewPager
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HFlippableViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<FlippableViewPager>(lpparam) {
    private val config get() = ConfigV1.get()

    override fun onInit() {
        //禁止ViewPager左右滑动
        lpparam.hookClass(targetClazz)
            .methodAll {
                onBefore {
                    if (!config.isHideTab) return@onBefore
                    if (method.name.contains("onInterceptTouchEvent|onTouchEvent|dispatchHoverEvent".toRegex())) {
                        result = false
                    }
                }
            }
    }

}