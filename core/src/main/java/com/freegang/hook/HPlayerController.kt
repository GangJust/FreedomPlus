package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.reflect.methods
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HPlayerController(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HPlayerController"

        @get:Synchronized
        @set:Synchronized
        var isPlaying = true
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.controller.PlayerController")
    }

    @OnAfter("onPlaying")
    fun onPlayingAfter(params: XC_MethodHook.MethodHookParam, string: String) {
        hookBlockRunning(params) {
            isPlaying = true
            // callOpenCleanMode(params, true)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onPausePlay")
    fun onPausePlayAfter(params: XC_MethodHook.MethodHookParam, string: String) {
        hookBlockRunning(params) {
            isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onPlayCompleted")
    fun onPlayCompletedAfter(params: XC_MethodHook.MethodHookParam, string: String) {
        hookBlockRunning(params) {
            // isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onPlayCompletedFirstTime")
    fun onPlayCompletedFirstTimeAfter(params: XC_MethodHook.MethodHookParam, string: String) {
        hookBlockRunning(params) {
            // isPlaying = false
            // callOpenCleanMode(params, false)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onPlayProgressChange")
    fun onPlayProgressChangeAfter(params: XC_MethodHook.MethodHookParam, float: Float) {
        hookBlockRunning(params) {
            // isPlaying = true
            // callOpenCleanMode(params, true)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun callOpenCleanMode(params: XC_MethodHook.MethodHookParam, bool: Boolean) {
        val methodFirst = params.thisObject.methods(returnType = VideoViewHolder::class.java)
            .firstOrNull { it.parameterTypes.isEmpty() }
        val videoViewHolder = methodFirst?.invoke(params.thisObject)
        videoViewHolder?.methodInvokeFirst("openCleanMode", args = arrayOf(bool))
    }
}