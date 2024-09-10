package io.github.fplus.core.hook

import android.annotation.SuppressLint
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import com.freegang.extension.asOrNull
import com.freegang.extension.dip2px
import com.freegang.extension.findField
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.findMethodInvoke
import com.freegang.extension.firstParentOrNull
import com.freegang.extension.forEachChild
import com.freegang.extension.getBrotherViewAt
import com.freegang.extension.postRunning
import com.freegang.extension.setLayoutWidth
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.FeedRightScaleView
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.AutoPlayHelper
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.plugin.injectRes
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.loader.hostClassloader

class HVideoViewHolder : BaseHook() {
    companion object {
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

            if (config.isNeatMode) {
                if (config.neatModeState) {
                    view.isVisible = !HPlayerController.isPlaying
                    HMainActivity.toggleView(view.isVisible)
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

        val first = view.viewTreeObserver.findFieldGetValue<List<*>> { name("mOnDrawListeners") } ?: return
        XplerLog.d("监听集合", *first.map { "$it" }.toTypedArray())
    }

    private fun openAutoPlay(context: Context) {
        if (!config.isAutoPlay)
            return

        AutoPlayHelper.openAutoPlay(context)
    }

    private fun getContext(params: MethodParam): Context? {
        return params.thisObject?.findMethodInvoke<Context> { name("getContext") }
    }

    private fun getAllView(params: MethodParam): List<View?> {
        val views = params.thisObject?.findFieldGetValue<List<View?>> { type(View::class.java) }
        return views ?: emptyList()
    }

    private fun getWidgetContainer(params: MethodParam): PenetrateTouchRelativeLayout? {
        return params.thisObject?.findFieldGetValue<PenetrateTouchRelativeLayout> {
            type(PenetrateTouchRelativeLayout::class.java)
        }
    }

    private fun getFeedRightScaleView(params: MethodParam): FeedRightScaleView? {
        val views = params.thisObject?.findField {
            type(View::class.java, true)
        }?.getValues(params.thisObject)
        return views?.firstOrNull { it is FeedRightScaleView }?.asOrNull()
    }

    private fun getFragment(params: MethodParam): Any? {
        return params.thisObject?.findFieldGetValue<Any> {
            type(hostClassloader!!.loadClass("androidx.fragment.app.Fragment"))
        }
    }

    private fun getFragmentType(params: MethodParam): String {
        val fragment = getFragment(params)
        val arguments = fragment?.findMethodInvoke<Bundle> { name("getArguments") }
        return arguments?.getString("com.ss.android.ugc.aweme.intent.extra.EVENT_TYPE") ?: ""
    }

    private fun changeFeedRightScaleView(params: MethodParam) {
        if (!config.isVideoOptionBarFilter) {
            return
        }

        val view = getFeedRightScaleView(params)
        view?.postRunning {
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
            view.getBrotherViewAt(1)?.isVisible = !isMusicContainer
        }
    }

    private fun changeFeedRightScaleViewAlpha(params: MethodParam) {
        if (!config.isTranslucent) {
            return
        }

        val view = getFeedRightScaleView(params)
        view?.alpha = config.translucentValue[2] / 100f
        view?.getBrotherViewAt(1)?.alpha = config.translucentValue[2] / 100f // 音乐
    }

    private fun adjustMusicContainer(params: MethodParam) {
        val view = getFeedRightScaleView(params)
        val isMusicContainer = view?.getBrotherViewAt(1)?.asOrNull<FrameLayout>()
        isMusicContainer?.setLayoutWidth(52f.dip2px())
    }

    @SuppressLint("SetTextI18n")
    private fun addAutoPlayButtonView(params: MethodParam) {
        if (!config.isAutoPlay)
            return

        if (!config.addAutoPlayButton)
            return

        if (!getFragmentType(params).startsWith("homepage_hot"))
            return

        val view = getFeedRightScaleView(params) ?: return

        val isAdded = view.children.firstOrNull()?.tag == "AutoPlay"
        if (isAdded)
            return

        injectRes(view.context.resources)

        val autoPlayContainer = LinearLayout(view.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.END
                updatePaddingRelative(end = 8f.dip2px())
            }
            tag = "AutoPlay"
        }

        val autoPlayImage = ImageView(view.context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_play))
            layoutParams = LinearLayout.LayoutParams(
                42f.dip2px(),
                42f.dip2px()
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

    @OnBefore("isCleanMode")
    fun isCleanModeBefore(params: MethodParam, view: View?, bool: Boolean) {
        hookBlockRunning(params) {
            if (!config.isNeatMode)
                return

            if (!config.neatModeState)
                return

            setResultVoid()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("openCleanMode")
    fun openCleanModeBefore(params: MethodParam, bool: Boolean) {
        hookBlockRunning(params) {
            if (!config.isNeatMode)
                return

            if (!config.neatModeState)
                return

            setResultVoid()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("getAweme")
    fun getAwemeAfter(params: MethodParam) {
        hookBlockRunning(params) {
            HVideoViewHolder.aweme = result?.asOrNull()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter
    fun startStayTime(params: MethodParam, long: Long?) {
        hookBlockRunning(params) {
            // XplerLog.d("long: $long")
            changeFeedRightScaleView(params)
            changeFeedRightScaleViewAlpha(params)
            adjustMusicContainer(params)
            addAutoPlayButtonView(params)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onViewHolderSelected")
    fun onViewHolderSelectedAfter(params: MethodParam, index: Int) {
        hookBlockRunning(params) {
            // XplerLog.d("onViewHolderSelected")
            val container = getWidgetContainer(params)
            addOnDraw(container)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onViewHolderUnSelected")
    fun onViewHolderUnSelectedAfter(params: MethodParam) {
        hookBlockRunning(params) {
            // XplerLog.d("onViewHolderUnSelected")
            val container = getWidgetContainer(params)
            removeOnDraw(container)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPause")
    fun onPauseBefore(params: MethodParam) {
        hookBlockRunning(params) {
            val container = getWidgetContainer(params)
            removeOnDraw(container)
            onDrawMaps.clear()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onResume")
    fun onResumeAfter(params: MethodParam) {
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