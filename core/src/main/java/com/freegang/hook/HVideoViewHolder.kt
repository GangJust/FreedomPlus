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
import com.freegang.xpler.core.EmptyHook
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolder(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<EmptyHook>(lpparam) {
    companion object {
        const val TAG = "HVideoViewHolder"
    }

    private val config get() = ConfigV1.get()

    override fun onInit() {
        lpparam.hookClass("com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder")
            .methodAllByName("bind") {
                onAfter {
                    val fieldGets = thisObject?.fieldGets(type = View::class.java) ?: emptyList()

                    // PenetrateTouchRelativeLayout
                    val penetrateTouchRelativeLayout = fieldGets.firstOrNull { it is PenetrateTouchRelativeLayout }
                        ?.asOrNull<ViewGroup>()
                    changePenetrateTouchRelativeLayout(this, penetrateTouchRelativeLayout)
                }
            }
    }

    private var onPreDraw: ViewTreeObserver.OnDrawListener? = null
    private fun changePenetrateTouchRelativeLayout(params: XC_MethodHook.MethodHookParam, view: View?) {
        hookBlockRunning(params) {
            view?.also {
                onPreDraw?.let {
                    view.viewTreeObserver.removeOnDrawListener(it)
                    onPreDraw = null
                }

                onPreDraw = ViewTreeObserver.OnDrawListener {
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
                view.viewTreeObserver.addOnDrawListener(onPreDraw)
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}