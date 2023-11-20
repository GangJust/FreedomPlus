package com.freegang.hook

import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fields
import com.freegang.ktutils.view.parentView
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.interfaces.CallConstructors
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.AwemeIntroInfoLayout
import com.ss.android.ugc.aweme.feed.ui.FeedRightScaleView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolderNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<VideoViewHolder>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HVideoViewHolder"

        @get:Synchronized
        @set:Synchronized
        var aweme: Aweme? = null
    }

    private val config get() = ConfigV1.get()

    @OnAfter("getAweme")
    fun getAwemeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            aweme = result.asOrNull()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("isCleanMode")
    fun isCleanModeBefore(params: XC_MethodHook.MethodHookParam, view: View?, boolean: Boolean) {
        hookBlockRunning(params) {
            KLogCat.d(
                "清空模式: ",
                "boolean: $boolean",
                "playState: ${HVideoPlayerState.playState}",
            )

            if (boolean) return

            // 清爽模式
            if (config.isNeatMode) {
                args[1] = config.neatModeState
                if (config.neatModeState) {
                    args[1] = HVideoPlayerState.playState == 2
                }
            }

        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        val fields = params.thisObject?.fields(type = View::class.java) ?: emptyList()
        for (field in fields) {
            val view = field.get(params.thisObject)?.asOrNull<View?>()
            view ?: continue

            // KLogCat.d(
            //     "当前对象: ${params.thisObject}",
            //     "${field.type.simpleName} ${field.name} = $view",
            //     "parent: ${view.parentView}"
            // )
            when {
                view is FeedRightScaleView -> {
                    changeAlpha(view.parentView)
                }

                view is AwemeIntroInfoLayout -> {
                    changeAlpha(view.parentView)
                }

                (field.name == "mBottomViewContainer" || view is LinearLayout)
                        || (field.name == "mMiddleEntranceStyleContainer" || view.parentView is RelativeLayout)
                        || view.javaClass.name.contains("MeasureOnceFrameLayout")
                -> {
                    changeAlpha(view)
                }
            }
        }
    }

    private fun changeAlpha(view: View?) {
        if (view == null) {
            KLogCat.d("changeAlpha: view == null")
            return
        }

        // 半透明
        if (config.isTranslucent) {
            view.alpha = config.translucentValue[1] / 100f
        }
    }
}