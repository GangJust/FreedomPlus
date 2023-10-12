package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.ClipboardLogic
import com.freegang.hook.logic.DownloadLogic
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisActivity
import com.freegang.xpler.core.thisContext
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<DetailActivity>(lpparam) {
    companion object {
        const val TAG = "HDetailActivity"
    }

    private val config get() = ConfigV1.get()
    private val clipboardLogic = ClipboardLogic(this)

    @OnAfter("onResume")
    fun onResumeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            addClipboardListener(thisActivity as DetailActivity)
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    @OnBefore("onPause")
    fun onPauseBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            clipboardLogic.removeClipboardListener(thisContext)
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    private fun findVideoAweme(activity: DetailActivity): Aweme? {
        // 24.2.0 ~ 至今
        var firstAweme = activity.methodInvokeFirst(returnType = Aweme::class.java)
        // 23.5.0 ~ 24.1.0
        if (firstAweme == null) {
            // deprecated
            //val any1 = activity.getObjectField<Any>("LIZJ")
            //firstAweme = any1?.methodInvokeFirst(returnType = Aweme::class.java)

            // new
            val any1 = activity.fieldGetFirst("LIZJ")
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