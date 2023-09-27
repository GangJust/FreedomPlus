package com.freegang.hook

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.SaveCommentLogic
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.view.KViewUtils
import com.freegang.ktutils.view.findViewsByExact
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.databinding.HookAppbarLayoutBinding
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HDetailPageFragment(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam) {
    companion object {
        @get:Synchronized
        @set:Synchronized
        var isComment = false
    }

    override fun setTargetClass(): Class<*> = DouYinMain.detailPageFragmentClazz ?: NoneHook::class.java

    private val config get() = ConfigV1.get()

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(param: XC_MethodHook.MethodHookParam, view: View, bundle: Bundle?) {
        hookBlock(param) {
            if (!config.isEmoji) return
            HDetailPageFragment.isComment = false
            rebuildTopBarView(thisObject, view)
        }
    }

    @OnAfter("onStop")
    fun onStopBefore(param: XC_MethodHook.MethodHookParam) {
        HDetailPageFragment.isComment = false
    }

    private fun rebuildTopBarView(any: Any, view: View) {
        launch {
            delay(200L)

            val aweme = any.methodInvokeFirst(returnType = Aweme::class.java) as? Aweme ?: return@launch

            // awemeType 【134:评论区图片, 133|136:评论区视频, 0:主页视频详情, 68:主页图文详情, 13:私信视频/图文, 6000:私信图片】 by 25.1.0 至今
            if (aweme.awemeType != 134 && aweme.awemeType != 133 && aweme.awemeType != 136) return@launch

            val imageViews = KViewUtils.findViewsByDesc(view, ImageView::class.java, "返回")
            if (imageViews.isEmpty()) return@launch
            val backBtn = imageViews.first()

            // 清空旧视图
            val viewGroup = backBtn.parent as ViewGroup
            viewGroup.removeAllViews()

            // 重新构建视图
            val appbar = KtXposedHelpers.inflateView<RelativeLayout>(viewGroup.context, R.layout.hook_appbar_layout)
            val binding = HookAppbarLayoutBinding.bind(appbar)
            binding.backBtn.setOnClickListener {
                backBtn.performClick()
            }
            binding.saveBtn.setOnClickListener {
                val aweme = any.methodInvokeFirst(returnType = Aweme::class.java) as? Aweme // 重新获取
                SaveCommentLogic(this@HDetailPageFragment, it.context, aweme)
            }
            viewGroup.addView(appbar)
            HDetailPageFragment.isComment = true

            // 我也发一张
            val textViews = view.findViewsByExact(TextView::class.java) {
                "${it.text}".contains("我也发") || "${it.contentDescription}".contains("我也发")
            }
            if (textViews.isEmpty()) return@launch
            binding.rightSpace.setPadding(0, 0, KDisplayUtils.dip2px(view.context, 128f), 0)
        }
    }
}