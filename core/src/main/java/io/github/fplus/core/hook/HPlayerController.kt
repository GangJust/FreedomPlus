package io.github.fplus.core.hook

import com.freegang.ktutils.reflect.methodInvoke
import com.freegang.ktutils.reflect.methods
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HPlayerController : BaseHook<Any>() {
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
            playingAid = aid
            isPlaying = true
            callOpenCleanMode(params, true)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPausePlay")
    fun onPausePlayAfter(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // XplerLog.d("onPausePlay: $aid")
            if (playingAid == aid) {
                isPlaying = false
            }
            callOpenCleanMode(params, false)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnBefore("onPlayCompleted")
    fun onPlayCompletedAfter(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnBefore("onPlayCompletedFirstTime")
    fun onPlayCompletedFirstTimeAfter(params: XC_MethodHook.MethodHookParam, aid: String?) {
        hookBlockRunning(params) {
            // isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPlayProgressChange")
    fun onPlayProgressChangeBefore(
        params: XC_MethodHook.MethodHookParam, aid: String?,
        current: Long,
        duration: Long,
    ) {
        hookBlockRunning(params) {
            playingAid = aid
            isPlaying = true
            // callOpenCleanMode(params, false)
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

        val methodFirst = params.thisObject.methods(returnType = VideoViewHolder::class.java)
            .firstOrNull { it.parameterTypes.isEmpty() }
        val videoViewHolder = methodFirst?.invoke(params.thisObject)
        videoViewHolder?.methodInvoke(name = "openCleanMode", args = arrayOf(bool))

        //
        HMainActivity.toggleView(!bool)
    }
}