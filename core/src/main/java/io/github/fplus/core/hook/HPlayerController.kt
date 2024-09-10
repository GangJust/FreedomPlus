package io.github.fplus.core.hook

import androidx.core.view.isVisible
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.findMethodInvoke
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.EmptyHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HPlayerController : BaseHook() {
    companion object {
        var playingAid: String? = ""

        @get:Synchronized
        @set:Synchronized
        var isPlaying = true
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return runCatching {
            findClass("com.ss.android.ugc.aweme.feed.controller.PlayerController")
        }.getOrDefault(EmptyHook::class.java)
    }

    @OnBefore("onPlaying")
    fun onPlayingAfter(params: MethodParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlaying: $aid")
            playingAid = aid
            isPlaying = true
            // callOpenCleanMode(params, true)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onResumePlay")
    fun onResumePlayBefore(params: MethodParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onResumePlay: $aid")
            playingAid = aid
            isPlaying = true
            // callOpenCleanMode(params, true)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPausePlay")
    fun onPausePlayBefore(params: MethodParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPausePlay: $aid")
            if (playingAid == aid) {
                isPlaying = false
                // callOpenCleanMode(params, false)
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnBefore("onPlayStop")
    fun onPlayStopBefore(params: MethodParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlayStop: $aid")
            if (playingAid == aid) {
                isPlaying = false
                // callOpenCleanMode(params, false)
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnBefore("onPlayCompleted")
    fun onPlayCompletedAfter(params: MethodParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlayCompleted: $aid")
            isPlaying = false
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnAfter("onPlayCompletedFirstTime")
    fun onPlayCompletedFirstTimeAfter(params: MethodParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPlayCompletedFirstTime: $aid")
            isPlaying = false
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onPlayProgressChange")
    fun onPlayProgressChangeBefore(
        params: MethodParam, aid: String?,
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

    private fun callOpenCleanMode(params: MethodParam, bool: Boolean) {
        if (!config.isNeatMode) {
            return
        }

        if (!config.neatModeState) {
            return
        }

        val videoViewHolder = params.thisObject?.findMethodInvoke<VideoViewHolder> {
            returnType(VideoViewHolder::class.java, true)
            predicate { it.parameterTypes.isEmpty() }
        }

        val view = videoViewHolder?.findFieldGetValue<PenetrateTouchRelativeLayout> {
            type(PenetrateTouchRelativeLayout::class.java)
        }

        // toggle
        view?.isVisible = !bool
        HMainActivity.toggleView(!bool)
    }
}