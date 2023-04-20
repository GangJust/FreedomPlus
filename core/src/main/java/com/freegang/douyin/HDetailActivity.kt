package com.freegang.douyin

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.ClipboardLogic
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.douyin.logic.SaveCommentLogic
import com.freegang.xpler.R
import com.freegang.xpler.databinding.HookAppbarLayoutBinding
import com.freegang.xpler.utils.other.KResourceUtils
import com.freegang.xpler.utils.view.KViewUtils
import com.freegang.xpler.xp.*
import com.ss.android.ugc.aweme.base.fragment.AmeBaseFragment
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<DetailActivity>(lpparam) {
    private val config get() = Config.get()
    private val clipboardLogic = ClipboardLogic(this)
    private var commentAweme: Aweme? = null

    @OnBefore(name = "onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlock(it) {
            findAndHookDetailFragment(thisActivity as DetailActivity)
        }
    }

    @OnAfter("onResume")
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            addClipboardListener(thisActivity as DetailActivity)
        }
    }

    @OnBefore(name = "onPause")
    fun onPause(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            clipboardLogic.removeClipboardListener(thisContext)
        }
    }

    private fun findVideoAweme(activity: DetailActivity): Aweme? {
        var aweme: Any? = null
        //24.2.0 ~ 至今
        var methods = activity.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            aweme = methods.first().call(activity)
        }

        //23.5.0 ~ 24.1.0
        if (aweme == null) {
            val any1 = activity.getObjectField<Any>("LIZJ")
            methods = any1?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
            if (methods.isNotEmpty()) {
                aweme = methods.first().call(any1!!)
            }
        }
        return aweme as? Aweme
    }

    private fun addClipboardListener(activity: DetailActivity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) {
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HDetailActivity, activity, aweme)
        }
    }

    private fun findAndHookDetailFragment(activity: DetailActivity) {
        activity.findFieldByType(AmeBaseFragment::class.java, true).apply {
            if (isEmpty()) return
            first().type.hookMethodAll {
                onBefore {
                    if (argsOrEmpty.isNotEmpty()) {
                        val aweme = args[0] ?: return@onBefore
                        if (aweme::class.java != Aweme::class.java) return@onBefore
                        if (commentAweme == aweme) return@onBefore
                        commentAweme = aweme as? Aweme
                    }
                }

                onAfter {
                    if (!method.name.contains("onViewCreated")) return@onAfter
                    val view = args[0] as? ViewGroup ?: return@onAfter
                    rebuildDetailFragmentTopBarView(view)
                }

                onUnhook { _, _ -> }
            }
        }
    }

    private fun rebuildDetailFragmentTopBarView(view: ViewGroup) {

        launch {
            delay(200L)

            // userRecommendStatus 【0:评论, 1:详情】
            // awemeType 【任意值:评论, 0:详情】
            // challengeList 【null:评论, 对象:详情】
            // danmakuControl 【对象:评论, null:详情】

            if (commentAweme?.awemeType == 0) return@launch

            val views = KViewUtils.findViewsByDesc(view, ImageView::class.java, "返回")
            if (views.isEmpty() || commentAweme == null) return@launch
            val backBtn = views.first()

            //清空旧视图
            val viewGroup = backBtn.parent as ViewGroup
            viewGroup.removeAllViews()

            //重新构建视图
            val appbar = KResourceUtils.inflateView<RelativeLayout>(viewGroup.context, R.layout.hook_appbar_layout)
            val binding = HookAppbarLayoutBinding.bind(appbar)
            binding.backBtn.setOnClickListener {
                backBtn.performClick()
            }
            binding.saveBtn.setOnClickListener {
                SaveCommentLogic(this@HDetailActivity, it.context, commentAweme)
            }
            viewGroup.addView(appbar)
        }
    }
}