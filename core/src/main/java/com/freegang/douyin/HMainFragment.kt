package com.freegang.douyin

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.xpler.utils.view.KViewUtils
import com.freegang.xpler.xp.hookClass
import com.ss.android.ugc.aweme.homepage.ui.view.MainFlippableViewPager
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import com.ss.android.ugc.aweme.main.MainFragment
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HMainFragment(
    lpparam: XC_LoadPackage.LoadPackageParam,
) : BaseHook(lpparam) {
    override fun onHook() {
        val config = Config.get()

        lpparam.hookClass(MainFragment::class.java)
            .method("onViewCreated", View::class.java, Bundle::class.java) {
                onAfter {
                    if (!config.isHideTab) return@onAfter
                    val viewGroup = args[0] as ViewGroup
                    hideTabItem(viewGroup, config)
                }
            }

        //禁止ViewPager左右滑动
        lpparam.hookClass(MainFlippableViewPager::class.java)
            .methodAll {
                onBefore {
                    if (!config.isHideTab) return@onBefore

                    if (method.name.contains("onInterceptTouchEvent|onTouchEvent|dispatchHoverEvent".toRegex())) {
                        result = false
                    }
                }
            }
    }

    //隐藏tab
    private fun hideTabItem(viewGroup: ViewGroup, config: Config) {
        val views = KViewUtils.findViews(viewGroup, MainTabStripScrollView::class.java)
        if (views.isEmpty()) return
        val hideTabKeyword = config.hideTabKeyword
            .replace("\\s".toRegex(), "")
            .replace(",|，".toRegex(), "|")
        KViewUtils.findViewsByDesc(views.first() as ViewGroup, View::class.java, hideTabKeyword.toRegex()).forEach { v ->
            v.isVisible = false
        }
    }
}