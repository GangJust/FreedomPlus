package com.freegang.douyin

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bytedance.ies.uikit.base.AbsActivity
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.douyin.logic.SaveLogic
import com.freegang.xpler.R
import com.freegang.xpler.databinding.HookAppbarLayoutBinding
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionName
import com.freegang.xpler.utils.other.KResourceUtils
import com.freegang.xpler.xp.*
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.*


/// 基类Activity
class HAbsActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<AbsActivity>(lpparam) {
    private val config get() = Config.get()
    private var primaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    @OnAfter("onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        if (!config.isEmoji) return
        if (it.thisActivity !is DetailActivity) return
        hookComment(it.thisObject as DetailActivity)
    }

    @OnAfter("onResume")
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        if (!config.isDownload) return
        addClipboardListener(it)
    }

    @OnBefore("onPause")
    fun onPause(it: XC_MethodHook.MethodHookParam) {
        if (!config.isDownload) return
        removeClipboardListener(it)
    }

    // 添加剪贴板监听
    private fun addClipboardListener(it: XC_MethodHook.MethodHookParam) {
        val absActivity = it.thisObject as AbsActivity
        val clipboardManager = absActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (!clipboardManager.hasPrimaryClip() || clipData!!.itemCount <= 0) return@OnPrimaryClipChangedListener

            //获取剪贴板内容
            val clipDataItem = clipData.getItemAt(0)
            val shareText = clipDataItem.text.toString()
            if (!shareText.contains("http")) return@OnPrimaryClipChangedListener

            //跳过直播链接, 按文本检查
            if (shareText.contains("【抖音】") && shareText.contains("正在直播") && shareText.contains("一起支持")) {
                showToast(absActivity, "不支持直播!")
                return@OnPrimaryClipChangedListener
            }

            // @Deprecated
            //showToast(absActivity, "复制成功!\n$shareText")

            //截取短链接, 一般这个截取逻辑能用到死, 但是不排除抖音更新分享文本格式, 如果真更新再说.
            //val urlIndexOf = shareText.indexOf("http")
            //val sortUrl = shareText.substring(urlIndexOf)
            //mainLogic(absActivity, sortUrl, config)

            hookActivity(absActivity)
        }
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    // 移除剪贴板监听
    private fun removeClipboardListener(it: XC_MethodHook.MethodHookParam) {
        val absActivity = it.thisObject as Context
        val clipboardManager = absActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
    }

    private fun hookActivity(activity: AbsActivity) {
        hookActivityAt1(activity)
        hookActivityAt2(activity)
    }

    private fun hookActivityAt1(activity: AbsActivity) {
        val versions = listOf("24.0.0", "24.1.0", "24.2.0")
        if (!versions.contains(activity.appVersionName)) return

        if (activity is MainActivity) {
            val methods = activity.findMethodsByReturnType(Aweme::class.java)
            if (methods.isNotEmpty()) {
                var aweme = methods.first().call(activity)
                if (aweme == null) {
                    val curFragment = activity.findMethod("getCurFragment", *arrayOf<Any>())?.call(activity)
                    val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
                    if (curFragmentMethods.isNotEmpty()) {
                        aweme = curFragmentMethods.first().call(curFragment!!)
                    }
                }
                DownloadLogic(this@HAbsActivity, activity, aweme, config.isOwnerDir)
            }
        }

        if (activity is DetailActivity) {
            val any1 = activity.getObjectField<Any>("LIZJ") ?: return
            val methods = any1.findMethodsByReturnType(Aweme::class.java)
            if (methods.isNotEmpty()) {
                val aweme = methods.first().call(any1)
                DownloadLogic(this@HAbsActivity, activity, aweme, config.isOwnerDir)
            }
        }
    }

    private fun hookActivityAt2(activity: AbsActivity) {
        val versions = listOf("24.2.0", "24.3.0", "24.4.0", "24.5.0", "24.6.0", "24.7.0")
        if (!versions.contains(activity.appVersionName)) return

        if (activity is DetailActivity || activity is MainActivity) {
            val methods = activity.findMethodsByReturnType(Aweme::class.java)
            if (methods.isNotEmpty()) {
                var aweme = methods.first().call(activity)
                if (aweme == null) {
                    val curFragment = activity.findMethod("getCurFragment", *arrayOf<Any>())?.call(activity)
                    val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
                    if (curFragmentMethods.isNotEmpty()) {
                        aweme = curFragmentMethods.first().call(curFragment!!)
                    }
                }
                DownloadLogic(this@HAbsActivity, activity, aweme, config.isOwnerDir)
            }
        }
    }

    // 获取评论区图片
    private fun hookComment(activity: DetailActivity) {
        launch {
            delay(200L)

            hookCommentAt1(activity)
            hookCommentAt2(activity)
        }
    }

    private fun hookCommentAt1(activity: DetailActivity) {
        val versions = listOf("24.0.0", "24.1.0", "24.2.0")

        if (!versions.contains(activity.appVersionName)) return
        var urlList: List<String> = listOf()

        val any1 = activity.getObjectField<Any>("LIZJ") ?: return
        val methods = any1.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            val aweme = methods.first().call(any1) ?: return
            val aid = aweme.getObjectField<Any>("aid")
            if (aid != null && "$aid".contains("-|[a-z]".toRegex())) {
                val image = aweme.getObjectField<List<Any>>("images")?.first() ?: return
                urlList = image.getObjectField<List<String>>("urlList") ?: listOf()
            }
        }

        if (urlList.isEmpty()) return
        rebuildCommentTopBarView(activity, urlList)
    }

    private fun hookCommentAt2(activity: DetailActivity) {
        val versions = listOf("24.2.0", "24.3.0", "24.4.0", "24.5.0", "24.6.0", "24.7.0")
        if (!versions.contains(activity.appVersionName)) return

        var urlList: List<String> = listOf()
        val methods = activity.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            val aweme = methods.first().call(activity) ?: return
            // 如果是评论区的评论, 则获取图片
            // 24.2.0, 24.3.0, 24.4.0
            val commentFeedOuterCid = aweme.getObjectField<Any>("commentFeedOuterCid")
            if (commentFeedOuterCid != null) {
                val image = aweme.getObjectField<List<Any>>("images")?.first() ?: return
                urlList = image.getObjectField<List<String>>("urlList") ?: listOf()
            }

            // 24.5.0, 24.6.0
            val commentFeedOuterAweme = aweme.getObjectField<Any>("commentFeedOuterAweme")
            if (commentFeedOuterAweme != null) {
                val image = aweme.getObjectField<List<Any>>("images")?.first() ?: return
                urlList = image.getObjectField<List<String>>("urlList") ?: listOf()
            }
        }
        if (urlList.isEmpty()) return
        rebuildCommentTopBarView(activity, urlList)
    }

    // 重新构建评论区顶部操作栏
    private fun rebuildCommentTopBarView(activity: DetailActivity, urlList: List<String> = listOf()) {
        val contentView: View = activity.window.decorView.findViewById(android.R.id.content)
        val outViews = ArrayList<View>()
        contentView.findViewsWithText(outViews, "返回", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
        val backBtn = outViews.first { it.contentDescription.equals("返回") }

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
            SaveLogic(this@HAbsActivity, it.context, urlList)
        }
        viewGroup.addView(appbar)
    }
}