package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.ClipboardLogic
import io.github.fplus.core.hook.logic.DownloadLogic
import io.github.xpler.core.OnAfter
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity
import io.github.xpler.core.thisContext

class HDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<DetailActivity>(lpparam) {
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
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onPause")
    fun onPauseBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            clipboardLogic.removeClipboardListener(thisContext)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun findVideoAweme(activity: DetailActivity): Aweme? {
        // 24.2.0 ~ 至今
        var firstAweme = activity.methodInvokeFirst(returnType = Aweme::class.java)
        // 23.5.0 ~ 24.1.0
        if (firstAweme == null) {
            // deprecated
            // val any1 = activity.getObjectField<Any>("LIZJ")
            // firstAweme = any1?.methodInvokeFirst(returnType = Aweme::class.java)

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
            DownloadLogic(
                this@HDetailActivity,
                activity,
                aweme ?: HVideoViewHolderV1.aweme,
            )
        }
    }
}