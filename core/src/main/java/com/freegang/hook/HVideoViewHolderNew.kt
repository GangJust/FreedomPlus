package com.freegang.hook

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import com.ss.android.ugc.aweme.feed.ui.seekbar.SeekBarState
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoViewHolderNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HVideoViewHolder"

        @get:Synchronized
        @set:Synchronized
        var aweme: Aweme? = null
    }

    private var onPreDrawMap = mutableMapOf<String, ViewTreeObserver.OnPreDrawListener?>()

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder")
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
    fun onViewHolderSelectedAfter(params: XC_MethodHook.MethodHookParam, i: Int) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            // KLogCat.d("onViewHolderSelected: $container")
            addOnPreDraw(container)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    // @OnAfter("onHolderResume")
    fun onHolderResumeAfter(params: XC_MethodHook.MethodHookParam, i: Int) {
        hookBlockRunning(params) {
            KLogCat.d("onHolderResume: ", *onPreDrawMap.map { "${it.key} = ${it.value}" }.toTypedArray())
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onResume")
    fun onResumeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            addOnPreDraw(container)
            // KLogCat.d("onResume: ", *onPreDrawMap.map { "${it.key} = ${it.value}" }.toTypedArray())
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onViewHolderUnSelected")
    fun onViewHolderUnSelectedBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            // KLogCat.d("onViewHolderUnSelected: $container")
            removeOnPreDraw(container)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    // @OnBefore("onHolderPause")
    fun onHolderPauseBefore(params: XC_MethodHook.MethodHookParam, i: Int) {
        hookBlockRunning(params) {
            KLogCat.d("onHolderPause: ", *onPreDrawMap.map { "${it.key} = ${it.value}" }.toTypedArray())
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onPause")
    fun onPauseAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            removeOnPreDraw(container)
            KLogCat.d("onPause: ", *onPreDrawMap.map { "${it.key} = ${it.value}" }.toTypedArray())
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun addOnPreDraw(view: View?) {
        if (view == null) {
            KLogCat.d("addOnPreDraw: view == null")
            return
        }

        val hexString = Integer.toHexString(System.identityHashCode(view))

        onPreDrawMap.putIfAbsent(hexString, ViewTreeObserver.OnPreDrawListener {
            changeView(view)
            true
        })

        view.viewTreeObserver.addOnPreDrawListener(onPreDrawMap[hexString])
    }

    private fun removeOnPreDraw(view: View?) {
        if (view == null) {
            KLogCat.d("removeOnPreDraw: view == null")
            return
        }

        val hexString = Integer.toHexString(System.identityHashCode(view))

        view.viewTreeObserver.removeOnPreDrawListener(onPreDrawMap[hexString])
        onPreDrawMap[hexString] = null
    }

    private fun changeView(view: View?) {
        if (view == null) {
            KLogCat.d("changeView: view == null")
            return
        }

        // 清爽模式
        if (config.isNeatMode) {
            if (config.neatModeState) {
                // 视频是否暂停
                val isPause = HCustomizedUISeekBar.action == SeekBarState.Action.PAUSE
                view.isVisible = isPause
            } else {
                view.isVisible = true
            }
        }

        // 半透明
        if (config.isTranslucent) {
            view.alpha = config.translucentValue[1] / 100f
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

    private fun getWidgetContainer(params: XC_MethodHook.MethodHookParam): PenetrateTouchRelativeLayout? {
        val allView = params.thisObject?.fieldGets(type = View::class.java)?.asOrNull<List<View?>>() ?: emptyList()

        // PenetrateTouchRelativeLayout
        return allView.firstOrNull { it is PenetrateTouchRelativeLayout }
            ?.asOrNull<PenetrateTouchRelativeLayout>()
    }
}