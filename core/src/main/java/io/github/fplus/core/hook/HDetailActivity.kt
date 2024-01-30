package io.github.fplus.core.hook

import android.app.Activity
import com.freegang.ktutils.log.KLogCat
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.ClipboardLogic
import io.github.fplus.core.hook.logic.DownloadLogic
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity

class HDetailActivity : BaseHook<DetailActivity>() {
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
            removeClipboardListener(thisActivity)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun addClipboardListener(activity: Activity) {
        if (!config.isDownload) return
        if (!config.isCopyDownload) return

        clipboardLogic.addClipboardListener(activity) { clipData, firstText ->
            DownloadLogic(
                this@HDetailActivity,
                activity,
                HVideoViewHolder.aweme,
            )
        }
    }

    private fun removeClipboardListener(activity: Activity) {
        clipboardLogic.removeClipboardListener(activity)
    }
}