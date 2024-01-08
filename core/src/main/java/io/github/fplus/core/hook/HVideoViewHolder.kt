package io.github.fplus.core.hook

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.reflect.methodFirst
import com.freegang.ktutils.reflect.methodInvokeFirst
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
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.OnAfter
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallConstructors

class HVideoViewHolder(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<VideoViewHolder>(lpparam), CallConstructors {

    companion object {
        const val TAG = "HVideoViewHolder"

        @get:Synchronized
        @set:Synchronized
        var aweme: Aweme? = null
    }

    private val config get() = ConfigV1.get()

    private val videoOptionBarFilterKeywords by lazy {
        config.videoOptionBarFilterKeywords
            .removePrefix(",").removePrefix("，")
            .removeSuffix(",").removeSuffix("，")
            .replace("\\s".toRegex(), "")
            .replace("[,，]".toRegex(), "|")
            .toRegex()
    }

    private var onDrawMaps = mutableMapOf<String, ViewTreeObserver.OnDrawListener?>()

    private fun addOnDraw(view: View?) {
        if (view == null) {
            KLogCat.d("addOnDraw", "view == null")
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

    private fun callOpenCleanMode(params: XC_MethodHook.MethodHookParam, bool: Boolean) {
        if (!config.isNeatMode) {
            return
        }

        if (!config.neatModeState) {
            return
        }

        val first = params.thisObject.methodFirst("openCleanMode", paramTypes = arrayOf(Boolean::class.java))
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
        if (!config.isVideoOptionBarFilter) return

        val isAvatarImageWithLive = videoOptionBarFilterKeywords.pattern.contains("头像")

        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        val view = views.firstOrNull { it is FeedRightScaleView }?.asOrNull<FeedRightScaleView>() ?: return

        view.postRunning {
            view.forEachChild {
                if (isAvatarImageWithLive && this.javaClass.name.contains("AvatarImageWithLive")) {
                    this.firstParentOrNull(FrameLayout::class.java)?.isVisible = false
                }

                if ("${this.contentDescription}".contains(videoOptionBarFilterKeywords)) {
                    this.firstParentOrNull(FrameLayout::class.java)?.isVisible = false
                }

                if (this is TextView && "$text".contains(videoOptionBarFilterKeywords)) {
                    this.firstParentOrNull(FrameLayout::class.java)?.isVisible = false
                }
            }

            val isMusicContainer = videoOptionBarFilterKeywords.pattern.contains("音乐")
            view.getSiblingViewAt(1)?.isVisible = !isMusicContainer
        }
    }

    private fun getContext(params: XC_MethodHook.MethodHookParam): Context? {
        return params.thisObject.methodInvokeFirst("getContext")?.asOrNull<Context>()
    }

    @OnAfter("getAweme")
    fun getAwemeAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            HVideoViewHolder.aweme = result.asOrNull()
        }.onFailure {
            KLogCat.tagE(HVideoViewHolder.TAG, it)
        }
    }

    @OnAfter
    fun startStayTime(params: XC_MethodHook.MethodHookParam, long: Long?) {
        hookBlockRunning(params) {
            // KLogCat.d("long: $long")
            changeFeedRightScaleView(params)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onViewHolderSelected")
    fun onViewHolderSelectedAfter(params: XC_MethodHook.MethodHookParam, index: Int) {
        hookBlockRunning(params) {
            // KLogCat.d("onViewHolderSelected")
            callOpenCleanMode(params, true)
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onViewHolderUnSelected")
    fun onViewHolderUnSelectedAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // KLogCat.d("onViewHolderSelected")
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

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            callOpenCleanMode(params, true)
        }
    }

    override fun onInit() {
        DexkitBuilder.videoViewHolderMethods.firstOrNull()?.hook {
            onAfter {
                callOpenCleanMode(this, true)
            }
        }
    }
}