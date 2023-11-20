package com.freegang.hook

import android.view.View
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.interfaces.CallConstructors
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoPlayerState(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HVideoPlayerState"

        @get:Synchronized
        @set:Synchronized
        var playState: Int = 2
    }

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.videoPlayerStateClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val playState = args[3] as Int
            val videoViewHolder = args[4].fieldGets(type = VideoViewHolder::class.java)
                .filterNotNull()
                .lastOrNull()
            val views = videoViewHolder?.fieldGets(type = View::class.java) ?: emptyList()
            val view = views.firstOrNull { it is PenetrateTouchRelativeLayout }
                ?.asOrNull<PenetrateTouchRelativeLayout>()

            HVideoPlayerState.playState = playState
            view?.isVisible = playState == 1

            KLogCat.d(
                "播放状态: ",
                "playState=$playState",
                "videoViewHolders=$videoViewHolder"
            )
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

}