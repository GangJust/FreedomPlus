package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.DownloadLogic
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.app.activeActivity
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.other.KAutomationUtils
import com.freegang.ktutils.reflect.fields
import com.freegang.ktutils.view.KFastClickUtils
import com.freegang.ktutils.view.KViewUtils
import com.freegang.ktutils.view.parentView
import com.freegang.ktutils.view.toViewTreeString
import com.freegang.ktutils.view.traverse
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.xpler.HookPackages
import com.freegang.xpler.core.CallConstructors
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisView
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.math.abs

class HVideoViewHolderRootView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<VideoViewHolderRootView>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HVideoViewHolderRootView"
    }

    private val config get() = ConfigV1.get()
    private val screenSize get() = KDisplayUtils.screenSize()

    // long press
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var isLongPressFast = false
    private var longPressFastRunnable: Runnable? = null
    private var longPressRunnable: Runnable? = null

    //
    private var onDragListener: ViewTreeObserver.OnDrawListener? = null

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            val view = thisView
            onDragListener?.let {
                view.viewTreeObserver.removeOnDrawListener(it)
                onDragListener = null
            }
            onDragListener = ViewTreeObserver.OnDrawListener {
                toggleView(view)
            }
            view.viewTreeObserver.addOnDrawListener(onDragListener)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        if (interdictEvent(params, event)) return
        longPressEvent(params, event)
    }

    private fun toggleView(view: View) {
        val viewHolderRootView = view as VideoViewHolderRootView
        val monitorScrollFrameLayout = viewHolderRootView.children.lastOrNull {
            it.javaClass.name.contains("MonitorScrollFrameLayout")
        }?.asOrNull<ViewGroup>()

        monitorScrollFrameLayout?.children?.forEach {
            if (it is PenetrateTouchRelativeLayout) {

                // 半透明
                if (config.isTranslucent) {
                    it.alpha = config.translucentValue[1] / 100f
                    if (it.alpha <= 0) {  // 全透明
                        it.isVisible = false
                        return@forEach
                    }
                }

                // 专注模式
                if (HVideoPinchView.isVideoPinchView) {
                    it.isVisible = false
                    return@forEach
                }

                // 清爽模式
                if (config.isNeatMode) {
                    it.isVisible = !config.neatModeState
                }

                // 长按快进
                if (isLongPressFast) {
                    it.isVisible = !isLongPressFast
                }
            }
        }
    }

    private fun longPressEvent(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(param) {
            val cancelEvent = MotionEvent.obtain(
                event.downTime,
                event.eventTime,
                MotionEvent.ACTION_CANCEL,
                event.x,
                event.y,
                event.metaState
            )

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y

                    // 预留出长按快进
                    if (event.x < screenSize.width / 8 || event.x > screenSize.width - screenSize.width / 8) {
                        longPressFastRunnable = Runnable { isLongPressFast = true }
                        handler.postDelayed(longPressFastRunnable!!, 200L)
                        return
                    }

                    // 非清爽模式
                    if (!config.isNeatMode) {
                        return
                    }

                    // 模块菜单显示逻辑
                    val viewGroup = thisViewGroup
                    if (config.longPressMode) {
                        if (event.y < screenSize.height / 2) {
                            longPressRunnable = Runnable {
                                showOptionsMenuV1(viewGroup)
                                viewGroup.parentView?.dispatchTouchEvent(cancelEvent)
                            }
                            handler.postDelayed(longPressRunnable!!, 300L)
                        } else {

                        }
                    } else {
                        if (event.y > screenSize.height / 2) {
                            longPressRunnable = Runnable {
                                showOptionsMenuV1(viewGroup)
                                viewGroup.parentView?.dispatchTouchEvent(cancelEvent)
                            }
                            handler.postDelayed(longPressRunnable!!, 300L)
                        } else {

                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(downX - event.x) < 10 && abs(downY - event.y) < 10) return // 消除误差
                    longPressRunnable?.runCatching { handler.removeCallbacks(this) }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isLongPressFast = false
                    longPressFastRunnable?.runCatching { handler.removeCallbacks(this) }
                    longPressRunnable?.runCatching { handler.removeCallbacks(this) }
                }

                else -> {
                    isLongPressFast = false
                    longPressFastRunnable?.runCatching { handler.removeCallbacks(this) }
                    longPressRunnable?.runCatching { handler.removeCallbacks(this) }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun interdictEvent(param: XC_MethodHook.MethodHookParam, event: MotionEvent): Boolean {
        hookBlockRunning(param) {
            val cancelEvent = MotionEvent.obtain(
                event.downTime,
                event.eventTime,
                MotionEvent.ACTION_CANCEL,
                event.x,
                event.y,
                event.metaState
            )

            if (event.action == MotionEvent.ACTION_DOWN) {
                // 避免快速下发 ACTION_DOWN
                if (KFastClickUtils.isFastDoubleClick(50L)) {
                    return true
                }

                // 防止双击点赞 (see at: HVideoPlayerHelper#callOnBeforeMethods#禁用双击点赞)
                if (KFastClickUtils.isFastDoubleClick(300L) && config.isDoubleClickType) {
                    thisView.parentView?.dispatchTouchEvent(cancelEvent)
                    // 打开评论区
                    if (config.doubleClickType == 1) {
                        onClickViewV2(thisView, targetContent = Regex("评论(.*?)，按钮"))
                    } else if (config.doubleClickType == 0) {
                        result = true
                    }
                    return true
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
        return false
    }

    private fun showOptionsMenuV1(view: ViewGroup) {
        if (HDetailPageFragment.isComment) return

        val items = mutableListOf("评论", "收藏", "分享", "下载")

        if (config.isNeatMode) {
            items.add(0, if (!config.neatModeState) "清爽模式" else "普通模式")
        }

        if (config.isVideoFilter) {
            items.add("过滤统计")
        }

        if (!config.isDisablePlugin) {
            items.add("模块设置")
        }

        if (KAppUtils.getVersionName(view.context, HookPackages.modulePackageName).contains("dev")) {
            items.add("布局信息")
            items.add("视频信息")
        }

        showChoiceDialog(
            context = view.context,
            title = "Freedom+",
            items = items.toTypedArray(),
            onChoice = { it, item, _ ->
                when (item) {
                    "清爽模式", "普通模式" -> {
                        config.neatModeState = !config.neatModeState
                        showToast(it.context, if (config.neatModeState) "清爽模式" else "普通模式")
                    }

                    "评论" -> {
                        onClickViewV2(view, targetContent = Regex("评论(.*?)，按钮"))
                    }

                    "收藏" -> {
                        onClickViewV2(view, targetContent = Regex("收藏(.*?)，按钮"))
                    }

                    "分享" -> {
                        onClickViewV2(view, targetContent = Regex("分享(.*?)，按钮"))
                    }

                    "下载" -> {
                        DownloadLogic(this@HVideoViewHolderRootView, view.context, HVerticalViewPagerNew.currentAweme)
                    }

                    "过滤统计" -> {
                        val builder = StringBuilder()
                        if (HVerticalViewPagerNew.filterLiveCount > 0) {
                            builder.append("直播过滤: ").append(HVerticalViewPagerNew.filterLiveCount).append("\n")
                        }
                        if (HVerticalViewPagerNew.filterImageCount > 0) {
                            builder.append("图文过滤: ").append(HVerticalViewPagerNew.filterImageCount).append("\n")
                        }
                        if (HVerticalViewPagerNew.filterAdCount > 0) {
                            builder.append("广告过滤: ").append(HVerticalViewPagerNew.filterAdCount).append("\n")
                        }
                        if (HVerticalViewPagerNew.filterLongVideoCount > 0) {
                            builder.append("长视频过滤: ").append(HVerticalViewPagerNew.filterLongVideoCount).append("\n")
                        }
                        if (HVerticalViewPagerNew.filterPopularEffectCount > 0) {
                            builder.append("特效过滤: ").append(HVerticalViewPagerNew.filterPopularEffectCount).append("\n")
                        }
                        if (HVerticalViewPagerNew.filterOtherCount > 0) {
                            builder.append("关键字过滤: ").append(HVerticalViewPagerNew.filterOtherCount)
                        }
                        val msg = builder.toString().trim()
                        if (msg.isEmpty()) {
                            showToast(view.context, "未过滤视频")
                        } else {
                            showToast(view.context, msg)
                        }
                    }

                    "模块设置" -> {
                        val intent = Intent().setClass(it.context, FreedomSettingActivity::class.java)
                        intent.putExtra("isDark", view.context.isDarkMode)
                        val options = ActivityOptions.makeCustomAnimation(
                            activeActivity,
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )
                        it.context.startActivity(intent, options.toBundle())
                    }

                    "布局信息" -> {
                        KLogCat.clearStorage()
                        KLogCat.openStorage()
                        KLogCat.d(
                            view.rootView.toViewTreeString(indent = 2) {
                                "${it.view?.javaClass?.name}, id=${it.view?.id}, desc=${it.view?.contentDescription}"
                            }
                        )
                        KLogCat.closeStorage()
                        KToastUtils.show(view.context.applicationContext, "布局信息获取成功!")
                    }

                    "视频信息" -> {
                        if (HVerticalViewPagerNew.currentAweme == null) {
                            KToastUtils.show(view.context.applicationContext, "未获取到视频信息!")
                            return@showChoiceDialog
                        }
                        KLogCat.clearStorage()
                        KLogCat.openStorage()
                        HVerticalViewPagerNew.currentAweme?.fields()?.forEach {
                            KLogCat.i("${it.type.name} ${it.name} = ${it.get(HVerticalViewPagerNew.currentAweme)}")
                        }
                        KLogCat.closeStorage()
                        KToastUtils.show(view.context.applicationContext, "视频信息获取成功!")
                    }
                }
            }
        )
    }

    private fun onClickViewV2(
        parent: View,
        targetView: Class<out View>? = null,
        targetText: Regex = Regex(""),
        targetHint: Regex = Regex(""),
        targetContent: Regex = Regex(""),
    ) {
        parent.traverse {
            var needClick = false
            if (this is TextView) {
                needClick = "$text".containsNotEmpty(targetText)
                needClick = needClick || "$hint".containsNotEmpty(targetHint)
            }
            needClick = needClick || "$contentDescription".containsNotEmpty(targetContent)
            needClick = needClick || (targetView?.isInstance(this) ?: false)
            if (!needClick) return@traverse
            // KLogCat.d("找到: \n${this}")

            // 是否具有点击事件
            val onClickListener = KViewUtils.getOnClickListener(this)
            if (onClickListener != null) {
                if (!KFastClickUtils.isFastDoubleClick(200L)) {
                    onClickListener.onClick(this)
                }
                return@traverse
            }
            // KLogCat.d("没有点击事件")

            // 模拟手势
            val location = IntArray(2) { 0 }
            getLocationOnScreen(location)
            if (location[1] > 0 && location[1] < screenSize.height) { // 是否在屏幕内
                location[0] = location[0] + right / 2
                location[1] = location[1] + bottom / 2
                if (!KFastClickUtils.isFastDoubleClick(200)) {
                    KAutomationUtils.simulateClickByView(this, location[0].toFloat(), location[1].toFloat())
                }
                return@traverse
            }
        }
    }

    private fun CharSequence.containsNotEmpty(regex: Regex): Boolean {
        if (regex.pattern.isEmpty()) return false
        return contains(regex)
    }
}