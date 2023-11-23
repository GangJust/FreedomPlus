package com.freegang.hook

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.reflect.methodFirst
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolderNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<VideoViewHolder>(lpparam) {
    companion object {
        const val TAG = "HVideoViewHolder"

        @get:Synchronized
        @set:Synchronized
        var aweme: Aweme? = null
    }

    private var onDrawMaps = mutableMapOf<String, ViewTreeObserver.OnDrawListener?>()

    private val config get() = ConfigV1.get()

    private fun addOnDraw(view: View?) {
        if (view == null) {
            KLogCat.d("addOnDraw", "view == null")
            return
        }

        val key = Integer.toHexString(System.identityHashCode(view))
        val isNeatMode = config.isNeatMode
        val neatModeState = config.neatModeState
        val isTranslucent = config.isTranslucent
        val translucentValue = config.translucentValue

        onDrawMaps.putIfAbsent(key, ViewTreeObserver.OnDrawListener {
            if (!isNeatMode) return@OnDrawListener

            if (neatModeState) {
                if (HPlayerController.isPlaying && view.isVisible) {
                    view.isVisible = false
                } else if (!HPlayerController.isPlaying && !view.isVisible) {
                    view.isVisible = true
                }
            }

            if (isTranslucent) {
                if (view.isVisible) {
                    val alpha = translucentValue[1] / 100f
                    if (view.alpha > alpha) {
                        view.alpha = alpha
                    }
                }
            }
        })

        view.viewTreeObserver.addOnDrawListener(onDrawMaps[key])
    }

    private fun removeOnDraw(view: View?) {
        if (view == null) {
            KLogCat.d("removeOnDraw", "view == null")
            return
        }

        val key = Integer.toHexString(System.identityHashCode(view))
        view.viewTreeObserver.removeOnDrawListener(onDrawMaps[key])
    }

    private fun testOnDraw(tag: String) {
        val array = onDrawMaps.map { "${it.key} = ${it.value}" }.toTypedArray()
        KLogCat.d(tag, *array)
    }

    private fun testAllOnDraw(view: View?) {
        if (view == null) {
            KLogCat.d("removeOnDraw", "view == null")
            return
        }

        val first = view.viewTreeObserver.fieldGetFirst("mOnDrawListeners")?.asOrNull<List<*>>() ?: return
        KLogCat.d("监听集合", *first.map { "$it" }.toTypedArray())
    }

    @OnAfter("getAweme")
    fun getAwemeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            aweme = result.asOrNull()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onViewHolderSelected")
    fun onViewHolderSelectedAfter(params: XC_MethodHook.MethodHookParam, index: Int) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onViewHolderUnSelected")
    fun onViewHolderUnSelectedAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            removeOnDraw(container)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onPause")
    fun onPauseBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            removeOnDraw(container)
            onDrawMaps.clear()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onResume")
    fun onResumeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun getWidgetContainer(params: XC_MethodHook.MethodHookParam): PenetrateTouchRelativeLayout? {
        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        return views.firstOrNull { it is PenetrateTouchRelativeLayout }
            ?.asOrNull<PenetrateTouchRelativeLayout>()
    }

    private fun callOpenCleanMode(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        val first = params.thisObject.methodFirst("openCleanMode", paramTypes = arrayOf(Boolean::class.java))
        XposedBridge.invokeOriginalMethod(first, params.thisObject, arrayOf(boolean))
    }
}