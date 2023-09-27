package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.ClipboardLogic
import com.freegang.hook.logic.DownloadLogic
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.getObjectField
import com.freegang.xpler.core.thisActivity
import com.freegang.xpler.core.thisContext
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<DetailActivity>(lpparam) {
    private val config get() = ConfigV1.get()
    private val clipboardLogic = ClipboardLogic(this)

    @OnAfter("onResume")
    fun onResume(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            addClipboardListener(thisActivity as DetailActivity)
        }
    }

    @OnBefore("onPause")
    fun onPause(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            clipboardLogic.removeClipboardListener(thisContext)
        }
    }

    private fun findVideoAweme(activity: DetailActivity): Aweme? {
        // 24.2.0 ~ 至今
        var firstAweme = activity.methodInvokeFirst(returnType = Aweme::class.java)
        // 23.5.0 ~ 24.1.0
        if (firstAweme == null) {
            val any1 = activity.getObjectField<Any>("LIZJ")
            firstAweme = any1?.methodInvokeFirst(returnType = Aweme::class.java)
        }
        return firstAweme as Aweme?
    }

    private fun addClipboardListener(activity: DetailActivity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) { clipData, firstText ->
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HDetailActivity, activity, aweme)
        }
    }
}