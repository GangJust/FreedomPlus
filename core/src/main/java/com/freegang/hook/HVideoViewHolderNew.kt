package com.freegang.hook

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.xpler.core.CallConstructors
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolderNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HVideoViewHolder"
    }

    private val config get() = ConfigV1.get()

    private var onDrawListener: ViewTreeObserver.OnDrawListener? = null

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder")
    }

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            val fieldGets = thisObject?.fieldGets(type = View::class.java) ?: emptyList()

            // PenetrateTouchRelativeLayout
            val penetrateTouchRelativeLayout = fieldGets.firstOrNull { it is PenetrateTouchRelativeLayout }
                ?.asOrNull<ViewGroup>()
            changePenetrateTouchRelativeLayout(this, penetrateTouchRelativeLayout)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun changePenetrateTouchRelativeLayout(params: XC_MethodHook.MethodHookParam, view: View?) {
        hookBlockRunning(params) {
            view?.also {
                onDrawListener?.let {
                    view.viewTreeObserver.removeOnDrawListener(it)
                    onDrawListener = null
                }

                onDrawListener = ViewTreeObserver.OnDrawListener {
                    // 清爽模式
                    if (config.isNeatMode) {
                        thisObject.methodInvokeFirst("isCleanMode", args = arrayOf(view, config.neatModeState))
                    }

                    // 半透明
                    if (config.isTranslucent) {
                        it.alpha = config.translucentValue[1] / 100f
                        if (it.alpha <= 0) {  // 全透明
                            thisObject.methodInvokeFirst("isCleanMode", args = arrayOf(view, config.neatModeState))
                            return@OnDrawListener
                        }
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
                }
                view.viewTreeObserver.addOnDrawListener(onDrawListener)
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}