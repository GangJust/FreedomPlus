package com.freegang.hook

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.view.findViewsByDesc
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.FieldGet
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import com.ss.android.ugc.aweme.main.MainFragment
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HMainFragment(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainFragment>(lpparam) {
    private val config get() = ConfigV1.get()

    @FieldGet("mCommonTitleBar")
    val mCommonTitleBar: View? = null

    override fun onInit() {
        lpparam.hookClass(targetClazz)
            .methodAll {
                onAfter {
                    if (!config.isTranslucent) return@onAfter
                    mCommonTitleBar?.alpha = 0.5f
                }
            }
            .method("onViewCreated", View::class.java, Bundle::class.java) {
                onAfter {
                    if (!config.isHideTab) return@onAfter
                    val viewGroup = args[0] as ViewGroup
                    changeTabItem(viewGroup)
                }
            }
            .method("onDestroy") {
                onAfter {
                }
            }
    }

    private fun changeTabItem(viewGroup: ViewGroup) {
        viewGroup.traverse {
            if (it is MainTabStripScrollView) {
                val hideTabKeywords = config.hideTabKeywords
                    .removePrefix(",").removePrefix("，")
                    .removeSuffix(",").removeSuffix("，")
                    .replace("\\s".toRegex(), "")
                    .replace("[,，]".toRegex(), "|")
                it.findViewsByDesc(View::class.java, hideTabKeywords.toRegex()).forEach { v -> v.isVisible = false }
            }
        }
    }
}