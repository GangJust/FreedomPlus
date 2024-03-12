package io.github.fplus.core.hook

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.reflect.fieldGet
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.reflect.method
import com.freegang.ktutils.reflect.methodInvoke
import com.freegang.ktutils.view.firstParentOrNull
import com.freegang.ktutils.view.forEachChild
import com.freegang.ktutils.view.getSiblingViewAt
import com.freegang.ktutils.view.postRunning
import com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.FeedRightScaleView
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.wrapper.CallConstructors

class HVideoViewHolder : BaseHook<VideoViewHolder>(),
    CallConstructors {

    companion object {
        const val TAG = "HVideoViewHolder"

        @get:Synchronized
        @set:Synchronized
        var aweme: Aweme? = null
    }

    private val config get() = ConfigV1.get()

    private var onDrawMaps = mutableMapOf<String, ViewTreeObserver.OnDrawListener?>()

    private val videoOptionBarFilterKeywords by lazy {
        config.videoOptionBarFilterKeywords
            .removePrefix(",").removePrefix("，")
            .removeSuffix(",").removeSuffix("，")
            .replace("\\s".toRegex(), "")
            .replace("[,，]".toRegex(), "|")
            .toRegex()
    }

    private fun addOnDraw(view: View?) {
        if (view == null) {
            XplerLog.d("addOnDraw", "view == null")
            return
        }

        val key = Integer.toHexString(System.identityHashCode(view))

        onDrawMaps.putIfAbsent(key, ViewTreeObserver.OnDrawListener {
            if (config.isTranslucent) {
                val alpha = config.translucentValue[1] / 100f
                if (view.alpha > alpha) {
                    view.alpha = alpha
                }
            }
        })

        view.viewTreeObserver.addOnDrawListener(onDrawMaps[key])
    }

    private fun removeOnDraw(view: View?) {
        if (view == null) {
            XplerLog.d("removeOnDraw", "view == null")
            return
        }

        val key = Integer.toHexString(System.identityHashCode(view))
        view.viewTreeObserver.removeOnDrawListener(onDrawMaps[key])
    }

    private fun testOnDraw(tag: String) {
        val array = onDrawMaps.map { "${it.key} = ${it.value}" }.toTypedArray()
        XplerLog.d(tag, *array)
    }

    private fun testAllOnDraw(view: View?) {
        if (view == null) {
            XplerLog.d("removeOnDraw", "view == null")
            return
        }

        val first = view.viewTreeObserver.fieldGet("mOnDrawListeners")?.asOrNull<List<*>>() ?: return
        XplerLog.d("监听集合", *first.map { "$it" }.toTypedArray())
    }

    private fun callOpenCleanMode(params: XC_MethodHook.MethodHookParam, bool: Boolean) {
        if (!config.isNeatMode) {
            return
        }

        if (!config.neatModeState) {
            return
        }

        val first = params.thisObject.method(name = "openCleanMode", paramTypes = arrayOf(Boolean::class.java))
        XposedBridge.invokeOriginalMethod(first, params.thisObject, arrayOf(bool))

        //
        HMainActivity.toggleView(!bool)
    }

    private fun getAllView(params: XC_MethodHook.MethodHookParam): List<View?> {
        val views = params.thisObject.fieldGets(type = View::class.java)
        return views.asOrNull<List<View?>>() ?: emptyList()
    }

    private fun getWidgetContainer(params: XC_MethodHook.MethodHookParam): PenetrateTouchRelativeLayout? {
        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        return views.firstOrNull { it is PenetrateTouchRelativeLayout }
            ?.asOrNull<PenetrateTouchRelativeLayout>()
    }

    private fun changeFeedRightScaleView(params: XC_MethodHook.MethodHookParam) {
        if (!config.isVideoOptionBarFilter) {
            return
        }

        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        val view = views.firstOrNull { it is FeedRightScaleView }?.asOrNull<FeedRightScaleView>() ?: return

        view.postRunning {
            val isAvatarImageWithLive = videoOptionBarFilterKeywords.pattern.contains("头像")
            view.forEachChild {
                if (isAvatarImageWithLive && this.javaClass.name.contains("AvatarImageWithLive")) {
                    it.firstParentOrNull(RelativeLayout::class.java)?.isVisible = false
                }

                if ("${it.contentDescription}".contains(videoOptionBarFilterKeywords)) {
                    it.firstParentOrNull(FrameLayout::class.java)?.isVisible = false
                }

                if (it is TextView && "${it.text}".contains(videoOptionBarFilterKeywords)) {
                    it.firstParentOrNull(FrameLayout::class.java)?.isVisible = false
                }
            }
            val isMusicContainer = videoOptionBarFilterKeywords.pattern.contains("音乐")
            view.getSiblingViewAt(1)?.isVisible = !isMusicContainer
        }
    }

    private fun changeFeedRightScaleViewAlpha(params: XC_MethodHook.MethodHookParam) {
        if (!config.isTranslucent) {
            return
        }

        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        val view = views.firstOrNull { it is FeedRightScaleView }?.asOrNull<FeedRightScaleView>() ?: return
        view.alpha = config.translucentValue[2] / 100f
        view.getSiblingViewAt(1)?.alpha = config.translucentValue[2] / 100f // 音乐
    }

    private fun getContext(params: XC_MethodHook.MethodHookParam): Context? {
        return params.thisObject.methodInvoke(name = "getContext")?.asOrNull<Context>()
    }

    @OnAfter("getAweme")
    fun getAwemeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            HVideoViewHolder.aweme = result?.asOrNull()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter
    fun startStayTime(params: XC_MethodHook.MethodHookParam, long: Long?) {
        hookBlockRunning(params) {
            // XplerLog.d("long: $long")
            changeFeedRightScaleView(params)
            changeFeedRightScaleViewAlpha(params)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onViewHolderSelected")
    fun onViewHolderSelectedAfter(params: XC_MethodHook.MethodHookParam, index: Int) {
        hookBlockRunning(params) {
            // XplerLog.d("onViewHolderSelected")
            callOpenCleanMode(params, true)
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onViewHolderUnSelected")
    fun onViewHolderUnSelectedAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // XplerLog.d("onViewHolderSelected")
            val container = getWidgetContainer(params)
            removeOnDraw(container)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPause")
    fun onPauseBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            removeOnDraw(container)
            onDrawMaps.clear()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onResume")
    fun onResumeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            callOpenCleanMode(params, true)
        }
    }

    override fun onInit() {
        DexkitBuilder.videoViewHolderMethods
            .firstOrNull { it.name[0] in 'A'..'Z' }
            ?.hook {
                onAfter {
                    callOpenCleanMode(this, true)
                }
            }
    }
}