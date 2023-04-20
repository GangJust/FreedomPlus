package com.freegang.douyin

import android.view.View
import android.view.ViewGroup
import com.freegang.config.Config
import com.freegang.xpler.utils.view.KViewUtils
import com.freegang.xpler.xp.KtOnHook
import com.freegang.xpler.xp.KtXposedHelpers
import com.freegang.xpler.xp.argsOrEmpty
import com.ss.android.ugc.aweme.lego.lazy.LazyFragmentPagerAdapter
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HLazyFragmentPagerAdapter(lpparam: XC_LoadPackage.LoadPackageParam) : KtOnHook<LazyFragmentPagerAdapter>(lpparam) {
    private val config: Config get() = Config.get()

    override fun onInit() {
        KtXposedHelpers.hookClass(targetClazz)
            .methodAllByName("finishUpdate") {
                onAfter {
                    val viewGroup = argsOrEmpty[0] as? ViewGroup ?: return@onAfter
                    changeView(viewGroup)
                }
            }
    }

    //透明度
    private fun changeView(viewGroup: ViewGroup) {
        if (!config.isTranslucent) return
        val views = KViewUtils.findViewsExact(viewGroup, View::class.java) {
            var result = it::class.java.name.contains("FeedRightScaleView") //点赞、评论..
            result = result or it::class.java.name.contains("AwemeIntroInfoLayout") //文案
            result = result or (it.contentDescription?.contains("按钮") ?: false)
            result
        }
        views.forEach { it.alpha = 0.8f }
    }
}