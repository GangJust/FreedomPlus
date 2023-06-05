package com.freegang.douyin

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.SaveCommentLogic
import com.freegang.ktutils.view.KViewUtils
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.call
import com.freegang.xpler.core.findMethodsByReturnType
import com.freegang.xpler.databinding.HookAppbarLayoutBinding
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HCommonPageFragment(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam) {
    private val config get() = Config.get()

    override fun setTargetClass(): Class<*> = DouYinMain.commonPageClazz ?: NoneHook::class.java

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(param: XC_MethodHook.MethodHookParam, view: View, bundle: Bundle?) {
        hookBlock(param) {
            if (!config.isEmoji) return
            rebuildTopBarView(thisObject, view as ViewGroup)
        }
    }

    private fun rebuildTopBarView(thisObject: Any, view: ViewGroup) {
        launch {
            delay(200L)

            val methods = thisObject.findMethodsByReturnType(Aweme::class.java)
            val aweme = methods.firstOrNull()?.call<Aweme>(thisObject) ?: return@launch

            // awemeType 【134:评论区图片, 133|136:评论区视频, 0:主页视频详情, 68:主页图文详情, 13:私信视频/图文, 6000:私信图片】 by 25.1.0 至今
            if (aweme.awemeType != 134 && aweme.awemeType != 133 && aweme.awemeType != 136) return@launch


            val views = KViewUtils.findViewsByDesc(view, ImageView::class.java, "返回")
            if (views.isEmpty()) return@launch
            val backBtn = views.first()

            //清空旧视图
            val viewGroup = backBtn.parent as ViewGroup
            viewGroup.removeAllViews()

            //重新构建视图
            val appbar = KtXposedHelpers.inflateView<RelativeLayout>(viewGroup.context, R.layout.hook_appbar_layout)
            val binding = HookAppbarLayoutBinding.bind(appbar)
            binding.backBtn.setOnClickListener {
                backBtn.performClick()
            }
            binding.saveBtn.setOnClickListener {
                val aweme = methods.first().call<Aweme>(thisObject) //重新获取
                SaveCommentLogic(this@HCommonPageFragment, it.context, aweme)
            }
            binding.saveBtn.setOnLongClickListener {
                //showToast(it.context, "类型: ${aweme.awemeType}")
                true
            }
            viewGroup.addView(appbar)
        }
    }
}