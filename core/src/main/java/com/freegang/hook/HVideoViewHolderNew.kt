package com.freegang.hook

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.xpler.core.CallConstructors
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import com.ss.android.ugc.aweme.feed.ui.seekbar.SeekBarState
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolderNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HVideoViewHolder"

        @get:Synchronized
        @set:Synchronized
        var aweme: Aweme? = null

        @get:Synchronized
        @set:Synchronized
        private var hexHasCodes = mutableListOf<String>()
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder")
    }

    @OnAfter("getAweme")
    fun getAwemeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            aweme = result.asOrNull()
        }.onFailure {
            KLogCat.tagE(HVideoViewHolder.TAG, it)
        }
    }

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            val fieldGets = thisObject?.fieldGets(type = View::class.java) ?: emptyList()

            // PenetrateTouchRelativeLayout
            val penetrateTouchRelativeLayout = fieldGets.firstOrNull { it is PenetrateTouchRelativeLayout }
                ?.asOrNull<ViewGroup>()

            val hexHashCode = Integer.toHexString(System.identityHashCode(penetrateTouchRelativeLayout))
            if (hexHasCodes.contains(hexHashCode)) return
            hexHasCodes.add(hexHashCode)

            changePenetrateTouchRelativeLayout(this, penetrateTouchRelativeLayout)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun changePenetrateTouchRelativeLayout(params: XC_MethodHook.MethodHookParam, view: View?) {
        hookBlockRunning(params) {
            view?.also {
                view.viewTreeObserver.addOnPreDrawListener {
                    // 清爽模式
                    if (config.isNeatMode) {
                        if (config.neatModeState) {
                            // 视频是否暂停
                            val isPause = HCustomizedUISeekBar.action == SeekBarState.Action.PAUSE
                            thisObject.methodInvokeFirst("isCleanMode", args = arrayOf(view, !isPause))
                        } else {
                            thisObject.methodInvokeFirst("isCleanMode", args = arrayOf(view, false))
                        }
                    }

                    // 半透明
                    if (config.isTranslucent) {
                        it.alpha = config.translucentValue[1] / 100f
                    }

                    // 全屏沉浸式
                    runCatching {
                        if (config.isImmersive) {
                            val activity = view.context as Activity
                            val window = activity.window
                            window.statusBarColor = Color.TRANSPARENT
                            window.navigationBarColor = Color.TRANSPARENT
                        }
                    }

                    true
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}