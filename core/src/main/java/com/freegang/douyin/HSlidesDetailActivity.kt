package com.freegang.douyin

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import com.bytedance.ies.uikit.base.AbsActivity
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.xpler.utils.log.KLogCat
import com.freegang.xpler.xp.*
import com.ss.android.ugc.aweme.familiar.feed.slides.detail.SlidesDetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HSlidesDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<SlidesDetailActivity>(lpparam) {
    @FieldGet(tag = "Ncq")
    val LJ: Any? = null

    @FieldGet(tag = "SlidesDetailParams")
    val LIZLLL: Any? = null

    private val config get() = Config.get()
    private var aweme: Aweme? = null
    private var primaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    @OnAfter
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        if (!config.isDownload) return

        //23.5.0
        val lji = LIZLLL?.getObjectField<Any>("LJI") ?: return
        val lizlll = findClass("X.NeG").getStaticObjectField<Any>("LIZLLL")
        aweme = lizlll?.callMethod<Any>("LIZIZ", lji) as? Aweme
        addClipboardListener(it)
    }

    @OnBefore
    fun onStop(it: XC_MethodHook.MethodHookParam) {
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

            DownloadLogic(this, absActivity, aweme)
        }
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    // 移除剪贴板监听
    private fun removeClipboardListener(it: XC_MethodHook.MethodHookParam) {
        val absActivity = it.thisObject as Context
        val clipboardManager = absActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
    }

    //test
    class Ncq(lpparam: XC_LoadPackage.LoadPackageParam) : KtOnHook<Any>(lpparam) {

        override fun setTargetClass(): Class<*> {
            return findClass("X.Ncq")
        }

        @FieldGet(tag = "SlidesDetailParams")
        val LJFF: Any? = null

        @FieldGet(tag = "HashMap")
        val LJIIJ: Any? = null

        @OnAfter
        fun onViewCreated(it: XC_MethodHook.MethodHookParam, view: View?, bundle: Bundle?) {
            KLogCat.d("LJFF: $LJFF")
            val lji = LJFF?.getObjectField<Any>("LJI") ?: return
            KLogCat.d("lji: $lji")
            val lizlll = findClass("X.NeG").getStaticObjectField<Any>("LIZLLL")
            KLogCat.d("lizlll: $lizlll")
            val aweme = lizlll?.callMethod<Any>("LIZIZ", lji)
            KLogCat.d("aweme: $aweme")
        }
    }
}