package io.github.fplus.core.hook

import android.app.Activity
import com.freegang.extension.findMethodInvoke
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.longervideo.landscape.home.activity.LandscapeFeedActivity
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.fplus.core.hook.logic.ClipboardLogic
import io.github.fplus.core.hook.logic.DownloadLogic
import io.github.xpler.core.XplerLog
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.core.thisActivity

class HLandscapeFeedActivity : BaseHook() {
    private val config get() = ConfigV1.get()

    private val clipboardLogic = ClipboardLogic(this)

    override fun setTargetClass(): Class<*> {
        return LandscapeFeedActivity::class.java
    }

    @OnAfter("onResume")
    fun onResumeAfter(params: MethodParam) {
        hookBlockRunning(params) {
            addClipboardListener(thisActivity)
            ImmersiveHelper.immersive(
                thisActivity,
                hideStatusBar = true,
                hideNavigationBars = true,
            )
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPause")
    fun onPauseBefore(params: MethodParam) {
        hookBlockRunning(params) {
            removeClipboardListener(thisActivity)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onWindowFocusChanged")
    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: MethodParam, boolean: Boolean) {
        hookBlockRunning(params) {
            ImmersiveHelper.immersive(
                thisActivity,
                hideStatusBar = true,
                hideNavigationBars = true,
            )
        }.onFailure {
            XplerLog.e(it)
        }
    }

    private fun addClipboardListener(activity: Activity) {
        if (!config.isDownload)
            return

        if (!config.copyLinkDownload)
            return

        clipboardLogic.addClipboardListener(activity) { _, _ ->
            val aweme = activity.findMethodInvoke<Aweme> { returnType(Aweme::class.java) }
            DownloadLogic(
                this@HLandscapeFeedActivity,
                activity,
                aweme,
            )
        }
    }

    private fun removeClipboardListener(activity: Activity) {
        clipboardLogic.removeClipboardListener(activity)
    }
}