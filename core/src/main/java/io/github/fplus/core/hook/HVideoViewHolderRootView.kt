package io.github.fplus.core.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.freegang.extension.activeActivity
import com.freegang.extension.asOrNull
import com.freegang.extension.fields
import com.freegang.extension.forEachChild
import com.freegang.extension.isDarkMode
import com.freegang.extension.parentView
import com.freegang.extension.toViewTreeString
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.other.KAutomationUtils
import com.freegang.ktutils.view.KViewUtils
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.DownloadLogic
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.fplus.core.ui.dialog.FreedomDialog
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisView
import io.github.xpler.core.thisViewGroup
import kotlin.math.abs

class HVideoViewHolderRootView : BaseHook<VideoViewHolderRootView>() {
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

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        onActionUpEvent(params, event)
        if (interdictEvent(params, event)) return
        longPressEvent(params, event)
    }

    private fun toggleView(view: View, visible: Boolean) {
        val viewHolderRootView = view as VideoViewHolderRootView
        val monitorScrollFrameLayout = viewHolderRootView.children.lastOrNull {
            it.javaClass.name.contains("MonitorScrollFrameLayout")
        }?.asOrNull<ViewGroup>()

        // 清爽模式
        monitorScrollFrameLayout?.children?.forEach {
            if (it is PenetrateTouchRelativeLayout) {
                it.isVisible = visible
            }
        }

        HMainActivity.toggleView(visible)
    }

    private fun longPressEvent(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
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
                                // showOptionsMenuV2(viewGroup)
                                viewGroup.parentView?.dispatchTouchEvent(cancelEvent)
                            }
                            handler.postDelayed(longPressRunnable!!, 300L)
                        } else {

                        }
                    } else {
                        if (event.y > screenSize.height / 2) {
                            longPressRunnable = Runnable {
                                showOptionsMenuV1(viewGroup)
                                // showOptionsMenuV2(viewGroup)
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
            XplerLog.e(it)
        }
    }

    private fun interdictEvent(params: XC_MethodHook.MethodHookParam, event: MotionEvent): Boolean {
        hookBlockRunning(params) {
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
                if (KViewUtils.isFastClick(50L)) {
                    return true
                }

                // 防止双击点赞 (see at: HVideoPlayerHelper#callOnBeforeMethods#禁用双击点赞)
                if (KViewUtils.isFastClick(300L) && config.isDoubleClickType) {
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
            XplerLog.e(it)
        }
        return false
    }

    private fun onActionUpEvent(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            if (event.action != MotionEvent.ACTION_UP) {
                return
            }

            if (!config.isNeatMode) {
                return
            }

            if (!config.neatModeState) {
                return
            }

            // some logic...
        }.onFailure {
            XplerLog.e(it)
        }
    }

    private fun showOptionsMenuV1(view: ViewGroup) {
        if (HDetailPageFragment.isComment) return

        val items = getChoiceItems(view).toTypedArray()
        showChoiceDialog(
            context = view.context,
            title = "Freedom+",
            items = items,
            onChoice = { it, item, _ ->
                onChoiceSelected(view, item)
            }
        )
    }

    private fun showOptionsMenuV2(view: ViewGroup) {
        if (HDetailPageFragment.isComment) return

        val items = getChoiceItems(view)
        showComposeDialog(view.context) { closeHandler ->
            FreedomDialog(
                items = items,
                showIcon = !config.isDisablePlugin,
                onIconClick = {
                    val intent = Intent().setClass(view.context, FreedomSettingActivity::class.java)
                    intent.putExtra("isDark", view.context.isDarkMode)
                    val options = ActivityOptions.makeCustomAnimation(
                        activeActivity,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    view.context.startActivity(intent, options.toBundle())
                },
                onChoice = { item ->
                    closeHandler.invoke()
                    onChoiceSelected(view, item)
                }
            )
        }
    }

    private fun getChoiceItems(view: View): List<String> {
        val items = mutableListOf("评论", "收藏", "分享")

        if (config.isDownload) {
            items.add("下载")
        }

        if (config.isNeatMode) {
            items.add(0, if (!config.neatModeState) "清爽模式" else "普通模式")
        }

        if (config.isVideoFilter) {
            items.add("过滤统计")
        }

        if (!config.isDisablePlugin) {
            items.add("模块设置")
        }

        if (KtXposedHelpers.moduleVersionName(view.context)?.contains("dev") == true) {
            items.add("布局信息")
            items.add("视频信息")
        }

        return items
    }

    private fun onChoiceSelected(view: View, item: CharSequence) {
        when (item) {
            "清爽模式", "普通模式" -> {
                config.neatModeState = !config.neatModeState
                toggleView(view, !config.neatModeState)
                showToast(view.context, if (config.neatModeState) "清爽模式" else "普通模式")
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
                DownloadLogic(
                    hook = this@HVideoViewHolderRootView,
                    context = view.context,
                    aweme = HVideoViewHolder.aweme,
                )
            }

            "过滤统计" -> {
                val builder = StringBuilder()
                if (HVerticalViewPager.filterLiveCount > 0) {
                    builder.append("直播过滤: ")
                        .append(HVerticalViewPager.filterLiveCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterImageCount > 0) {
                    builder.append("图文过滤: ")
                        .append(HVerticalViewPager.filterImageCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterAdCount > 0) {
                    builder.append("广告过滤: ")
                        .append(HVerticalViewPager.filterAdCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterLongVideoCount > 0) {
                    builder.append("长视频过滤: ")
                        .append(HVerticalViewPager.filterLongVideoCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterRecommendedCardsCount > 0) {
                    builder.append("推荐卡片过滤: ")
                        .append(HVerticalViewPager.filterRecommendedCardsCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterRecommendedMerchantsCount > 0) {
                    builder.append("推荐商家过滤: ")
                        .append(HVerticalViewPager.filterRecommendedMerchantsCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterEmptyDescCount > 0) {
                    builder.append("空文案过滤: ")
                        .append(HVerticalViewPager.filterEmptyDescCount)
                        .append("\n")
                }
                if (HVerticalViewPager.filterOtherCount > 0) {
                    builder.append("关键字过滤: ").append(HVerticalViewPager.filterOtherCount)
                }
                val msg = builder.toString().trim()
                if (msg.isEmpty()) {
                    showToast(view.context, "未过滤视频")
                } else {
                    showToast(view.context, msg)
                }
            }

            "模块设置" -> {
                val intent = Intent().setClass(view.context, FreedomSettingActivity::class.java)
                intent.putExtra("isDark", view.context.isDarkMode)
                val options = ActivityOptions.makeCustomAnimation(
                    activeActivity,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                view.context.startActivity(intent, options.toBundle())
            }

            "布局信息" -> {
                KLogCat.clearStorage()
                KLogCat.openStorage()
                KLogCat.d(
                    view.rootView.toViewTreeString(indent = 1)
                )
                KLogCat.closeStorage()
                KToastUtils.show(view.context.applicationContext, "布局信息获取成功!")
            }

            "视频信息" -> {
                if (HVerticalViewPager.currentAweme == null) {
                    KToastUtils.show(view.context.applicationContext, "未获取到视频信息!")
                    return
                }
                KLogCat.clearStorage()
                KLogCat.openStorage()
                HVerticalViewPager.currentAweme?.fields()?.forEach {
                    KLogCat.i("${it.type.name} ${it.name} = ${it.get(HVerticalViewPager.currentAweme)}")
                }
                KLogCat.closeStorage()
                KToastUtils.show(view.context.applicationContext, "视频信息获取成功!")
            }
        }
    }

    private fun onClickViewV2(
        parent: View,
        targetView: Class<out View>? = null,
        targetText: Regex = Regex(""),
        targetHint: Regex = Regex(""),
        targetContent: Regex = Regex(""),
    ) {
        parent.forEachChild {
            var needClick = false
            if (it is TextView) {
                needClick = "${it.text}".containsNotEmpty(targetText)
                needClick = needClick || "${it.hint}".containsNotEmpty(targetHint)
            }
            needClick = needClick || "${it.contentDescription}".containsNotEmpty(targetContent)
            needClick = needClick || (targetView?.isInstance(it) ?: false)
            if (!needClick) return@forEachChild
            // KLogCat.d("找到: \n${this}")

            // 是否具有点击事件
            val onClickListener = KViewUtils.getOnClickListener(it)
            if (onClickListener != null) {
                if (!KViewUtils.isFastClick(200L)) {
                    onClickListener.onClick(it)
                }
                return@forEachChild
            }
            // KLogCat.d("没有点击事件")

            // 模拟手势
            val location = IntArray(2) { 0 }
            it.getLocationOnScreen(location)
            if (location[1] > 0 && location[1] < screenSize.height) { // 是否在屏幕内
                location[0] = location[0] + it.right / 2
                location[1] = location[1] + it.bottom / 2
                if (!KViewUtils.isFastClick(200)) {
                    KAutomationUtils.simulateClickByView(it, location[0].toFloat(), location[1].toFloat())
                }
                return@forEachChild
            }
        }
    }

    private fun CharSequence.containsNotEmpty(regex: Regex): Boolean {
        if (regex.pattern.isEmpty()) return false
        return contains(regex)
    }
}