package io.github.fplus.core.hook

import androidx.core.view.isVisible
import com.freegang.extension.asOrNull
import com.freegang.extension.fields
import com.freegang.extension.methods
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HPlayerController : BaseHook() {
    companion object {
        const val TAG = "HPlayerController"

        var playingAid: String? = ""

        @get:Synchronized
        @set:Synchronized
        var isPlaying = true
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.controller.PlayerController")
    }

    @OnBefore("onPlaying")
    fun onPlayingAfter(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlaying: $aid")
            playingAid = aid
            isPlaying = true
            callOpenCleanMode(params, true)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onResumePlay")
    fun onResumePlayBefore(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onResumePlay: $aid")
            playingAid = aid
            isPlaying = true
            callOpenCleanMode(params, true)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPausePlay")
    fun onPausePlayBefore(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPausePlay: $aid")
            if (playingAid == aid) {
                isPlaying = false
                callOpenCleanMode(params, false)
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnBefore("onPlayStop")
    fun onPlayStopBefore(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlayStop: $aid")
            if (playingAid == aid) {
                isPlaying = false
                callOpenCleanMode(params, false)
            }
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    // @OnBefore("onPlayCompleted")
    fun onPlayCompletedAfter(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlayCompleted: $aid")
            isPlaying = false
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnAfter("onPlayCompletedFirstTime")
    fun onPlayCompletedFirstTimeAfter(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlayCompletedFirstTime: $aid")
            isPlaying = false
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onPlayProgressChange")
    fun onPlayProgressChangeBefore(
        params: XC_MethodHook.MethodHookParam, aid: String?,
        current: Long,
        duration: Long,
    ) {
        hookBlockRunning(params) {
            playingAid = aid
            isPlaying = true
        }.onFailure {
            XplerLog.e(it)
        }
    }

    private fun callOpenCleanMode(params: XC_MethodHook.MethodHookParam, bool: Boolean) {
        if (!config.isNeatMode) {
            return
        }

        if (!config.neatModeState) {
            return
        }

        val method = params.thisObject
            .methods(returnType = VideoViewHolder::class.java)
            .firstOrNull { it.parameterTypes.isEmpty() }
        val videoViewHolder = method?.invoke(params.thisObject)

        val field = videoViewHolder
            ?.fields(type = PenetrateTouchRelativeLayout::class.java)
            ?.firstOrNull { it.type == PenetrateTouchRelativeLayout::class.java }
        val view = field?.get(videoViewHolder)?.asOrNull<PenetrateTouchRelativeLayout>()

        // toggle
        view?.isVisible = !bool
        HMainActivity.toggleView(!bool)
    }
}