package io.github.fplus.core.hook

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import com.freegang.extension.asOrNull
import com.freegang.extension.dip2px
import com.freegang.extension.fieldGet
import com.freegang.extension.fieldGets
import com.freegang.extension.firstParentOrNull
import com.freegang.extension.forEachChild
import com.freegang.extension.getSiblingViewAt
import com.freegang.extension.methodInvoke
import com.freegang.extension.postRunning
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.FeedRightScaleView
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.AutoPlayHelper
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.getModuleDrawable
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.loader.hostClassloader

class HVideoViewHolder : BaseHook<Any>() {

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
            .replace("，", ",")
            .replace("\\s".toRegex(), "")
            .removePrefix(",").removeSuffix(",")
            .replace(",".toRegex(), "|")
            .replace("\\|+".toRegex(), "|")
            .toRegex()
    }

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.videoViewHolderClazz ?: NoneHook::class.java
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

    private fun openAutoPlay(context: Context) {
        if (!config.isAutoPlay)
            return

        AutoPlayHelper.openAutoPlay(context)
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

    private fun getFragment(params: XC_MethodHook.MethodHookParam): Any? {
        val fragmentClazz = hostClassloader!!.loadClass("androidx.fragment.app.Fragment")
        return params.thisObject?.fieldGet(type = fragmentClazz)
    }

    private fun getFragmentType(params: XC_MethodHook.MethodHookParam): String {
        val fragment = getFragment(params)
        val arguments = fragment?.methodInvoke("getArguments")?.asOrNull<Bundle>()
        return arguments?.getString("com.ss.android.ugc.aweme.intent.extra.EVENT_TYPE") ?: ""
    }

    private fun changeFeedRightScaleView(params: XC_MethodHook.MethodHookParam) {
        if (!config.isVideoOptionBarFilter) {
            return
        }

        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        val view = views.firstOrNull { it is FeedRightScaleView }?.asOrNull<FeedRightScaleView>() ?: return

        view.postRunning {
            val isAvatarImageWithLive = videoOptionBarFilterKeywords.pattern.contains("头像")
            view.forEachChild { child ->
                if (isAvatarImageWithLive && child.javaClass.name.contains("AvatarImageWithLive")) {
                    val parentView = child.firstParentOrNull(RelativeLayout::class.java)
                    parentView?.isVisible = false
                }

                if ("${child.contentDescription}".contains(videoOptionBarFilterKeywords)) {
                    val parentView = child.firstParentOrNull(FrameLayout::class.java)
                    parentView?.isVisible = false
                }

                if (child is TextView && "${child.text}".contains(videoOptionBarFilterKeywords)) {
                    val parentView = child.firstParentOrNull(FrameLayout::class.java)
                    parentView?.isVisible = false
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

    private fun addAutoPlayButtonView(params: XC_MethodHook.MethodHookParam) {
        if (!config.isAutoPlay)
            return

        if (!config.addAutoPlayButton)
            return

        if (!getFragmentType(params).startsWith("homepage_hot"))
            return

        val views = params.thisObject?.fieldGets(type = View::class.java) ?: emptyList()
        val view = views.firstOrNull { it is FeedRightScaleView }?.asOrNull<FeedRightScaleView>() ?: return

        val isAdded = view.children.firstOrNull()?.tag == "AutoPlay"
        if (isAdded)
            return

        val autoPlayContainer = LinearLayout(view.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.END
                updatePaddingRelative(end = context.dip2px(8f))
            }
            tag = "AutoPlay"
        }

        val autoPlayImage = ImageView(view.context).apply {
            setImageDrawable(context.getModuleDrawable(R.drawable.ic_play))
            layoutParams = LinearLayout.LayoutParams(
                view.context.dip2px(42f),
                view.context.dip2px(42f)
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        val autoPlayText = TextView(view.context).apply {
            text = "连播"
            textSize = 14f
            setTextColor(Color.parseColor("#E6FFFFFF"))
            gravity = Gravity.CENTER_HORIZONTAL
        }

        autoPlayContainer.addView(autoPlayImage)
        autoPlayContainer.addView(autoPlayText)
        autoPlayContainer.setOnClickListener { openAutoPlay(it.context) }

        view.addView(autoPlayContainer, 0)
    }

    private fun getContext(params: XC_MethodHook.MethodHookParam): Context? {
        return params.thisObject.methodInvoke(name = "getContext")?.asOrNull<Context>()
    }

    @OnBefore("isCleanMode")
    fun isCleanModeBefore(params: XC_MethodHook.MethodHookParam, view: View?, bool: Boolean) {
        hookBlockRunning(params) {
            if (!config.isNeatMode)
                return

            if (!config.neatModeState)
                return

            result = Void.TYPE
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    @OnBefore("openCleanMode")
    fun openCleanModeBefore(params: XC_MethodHook.MethodHookParam, bool: Boolean) {
        hookBlockRunning(params) {
            if (!config.isNeatMode)
                return

            if (!config.neatModeState)
                return

            result = Void.TYPE
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
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
            addAutoPlayButtonView(params)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onViewHolderSelected")
    fun onViewHolderSelectedAfter(params: XC_MethodHook.MethodHookParam, index: Int) {
        hookBlockRunning(params) {
            // XplerLog.d("onViewHolderSelected")
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onViewHolderUnSelected")
    fun onViewHolderUnSelectedAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // XplerLog.d("onViewHolderUnSelected")
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
            changeFeedRightScaleView(params)
            addAutoPlayButtonView(params)
        }.onFailure {
            XplerLog.e(it)
        }
    }
}