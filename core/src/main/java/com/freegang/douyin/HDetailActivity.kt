package com.freegang.douyin

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.douyin.logic.ClipboardLogic
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.call
import com.freegang.xpler.core.findMethodsByReturnType
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
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            addClipboardListener(thisActivity as DetailActivity)
        }
    }

    @OnBefore("onPause")
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
        return aweme as Aweme?
    }

    private fun addClipboardListener(activity: DetailActivity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) {
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HDetailActivity, activity, aweme)
        }
    }
}