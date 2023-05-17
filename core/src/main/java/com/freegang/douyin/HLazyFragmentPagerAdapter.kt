package com.freegang.douyin

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.config.Config
import com.freegang.xpler.core.KtOnHook
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.utils.view.KViewUtils
import com.ss.android.ugc.aweme.lego.lazy.LazyFragmentPagerAdapter
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HLazyFragmentPagerAdapter(lpparam: XC_LoadPackage.LoadPackageParam) : KtOnHook<LazyFragmentPagerAdapter>(lpparam) {
    private val config: Config get() = Config.get()

    override fun onInit() {
        KtXposedHelpers.hookClass(targetClazz)
            .methodAllByName("finishUpdate") {
                onAfter {
                    val viewGroup = argsOrEmpty[0] as? ViewGroup ?: return@onAfter
                    changeViewAlpha(viewGroup)
                }
            }
    }

    //透明度、清爽模式
    private fun changeViewAlpha(viewGroup: ViewGroup) {
        KViewUtils.traverseViewGroup(viewGroup) {
            //透明度
            if (config.isTranslucent) {
                val descr = it.contentDescription ?: ""
                var result = it::class.java.name.contains("FeedRightScaleView") //点赞、评论..
                result = result or it::class.java.name.contains("AwemeIntroInfoLayout") //文案
                result = result or descr.contains("按钮")
                if (result) {
                    it.alpha = 0.8f
                }
            }

            //清爽模式
            if (config.isNeat && config.neatState) {
                val descr = it.contentDescription ?: ""
                var result = it::class.java.name.contains("FeedRightScaleView") //点赞、评论..
                result = result or it::class.java.name.contains("AwemeIntroInfoLayout") //文案
                result = result or descr.contains("音乐") //音乐
                if (result) {
                    it.isVisible = false
                }
                //预约直播
                if (descr.contains("预约直播")) {
                    (it.parent as View).isVisible = false
                }
                //关注页，顶部n个直播
                if (descr.contains("个直播")) {
                    (it.parent as View).isVisible = false
                }
                //同城页，顶部附近
                if (descr.contains("附近") && descr.contains("按钮")) {
                    (it.parent.parent as View).isVisible = false
                }
                //悬浮的挑战
                if ((it is TextView) && it.hint?.contains("挑战") == true) {
                    (it.parent.parent.parent.parent.parent as View).isVisible = false
                }
                //弹幕
                if (descr.contains("发弹幕")) {
                    it.isVisible = false
                }
                //全屏
                if (descr.contains("全屏")) {
                    it.isVisible = false
                }
                //视频页悬浮的回复评论
                if (descr.contains("回复的评论内容")) {
                    it.isVisible = false
                }

                //底部，热榜/合集/相关推荐等
                /*if(it.idName == "@id/container"){
                    it.isVisible = false //id方式在关注页会卡顿
                }*/
               if (descr.contains("相关") || descr == "按钮") {
                    (it.parent.parent as View).isVisible = false
                }
                if ((it is TextView) && it.text.contains("相关")) {
                    (it.parent.parent as View).isVisible = false
                }
                if (descr.contains("合集")) {
                   (it.parent.parent as View).isVisible = false
               }
               if ((it is TextView) && it.text.contains("合集")) {
                   (it.parent.parent as View).isVisible = false
               }
                if ((it is TextView) && it.text.contains("下一集")) {
                    (it.parent.parent as View).isVisible = false
                }
                if (descr.contains("热点")) {
                    (it.parent as View).isVisible = false
                }
                if (descr.contains("热榜")) {
                    (it.parent as View).isVisible = false
                }
                if (it::class.java.name == "UIView") {
                    (it.parent as View).isVisible = false
                }

                //不会隐藏风险提示, 请注意
            }
        }
    }
}