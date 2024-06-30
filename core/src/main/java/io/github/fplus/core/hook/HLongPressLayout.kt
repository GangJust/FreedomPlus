package io.github.fplus.core.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.freegang.extension.activeActivity
import com.freegang.extension.asOrNull
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.firstParentOrNull
import com.freegang.extension.forEachChild
import com.freegang.extension.isDarkMode
import com.freegang.extension.screenSize
import com.freegang.ktutils.other.KAutomationUtils
import com.freegang.ktutils.view.KViewUtils
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.hook.logic.DownloadLogic
import io.github.fplus.core.hook.logic.cityInfo
import io.github.fplus.core.hook.logic.createDate
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookClass
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisView


class HLongPressLayout : BaseHook() {
    companion object {
        const val TAG = "HLongPressLayout"
    }

    private val config
        get() = ConfigV1.get()

    private var lastTouchDown: Long = 0
    private var numberOfTaps: Int = 0
    private var lastTapTimeMs: Long = 0
    private var touchDownX: Float = 0f
    private var touchDownY: Float = 0f

    private val LONG_PRESS_TIME = ViewConfiguration.getLongPressTimeout()
    private val DOUBLE_TAP_TIME = ViewConfiguration.getDoubleTapTimeout()

    private var longPressRunnable: Runnable? = null

    private var aweme: Aweme? = null

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.LongPressLayout")
    }

    @OnAfter("onTouchEvent")
    fun onTouchEventAfter(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            longPressRunnable = longPressRunnable ?: Runnable { onLongPress(thisView, event) }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressRunnable?.let { handler.postDelayed(it, LONG_PRESS_TIME.toLong()) }
                    touchDownX = event.x
                    touchDownY = event.y
                    lastTouchDown = System.currentTimeMillis()
                }

                MotionEvent.ACTION_UP -> {
                    longPressRunnable?.let {
                        handler.removeCallbacks(it)
                        longPressRunnable = null
                    }

                    if (System.currentTimeMillis() - lastTouchDown < LONG_PRESS_TIME) {
                        // onClick(thisView,event)
                        if (System.currentTimeMillis() - lastTapTimeMs < DOUBLE_TAP_TIME) {
                            if (numberOfTaps == 1) {
                                onDoubleClick(thisView, event)
                                numberOfTaps = 0
                            }
                        } else {
                            numberOfTaps = 1
                        }
                        lastTapTimeMs = System.currentTimeMillis()
                    }
                }

                else -> {
                    if (isMoved(event) || event.action == MotionEvent.ACTION_CANCEL) {
                        longPressRunnable?.let {
                            handler.removeCallbacks(it)
                            longPressRunnable = null
                        }
                    }
                }
            }

        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    private fun isMoved(event: MotionEvent): Boolean {
        return Math.abs(touchDownX - event.x) > 10 || Math.abs(touchDownY - event.y) > 10
    }

    private fun onClick(view: View, event: MotionEvent) {

    }

    private fun onDoubleClick(view: View, event: MotionEvent) {
        if (!config.isDoubleClickType)
            return

        val holderRootView = view.firstParentOrNull(VideoViewHolderRootView::class.java) ?: return
        when (config.doubleClickType) {
            1 -> onClickView(holderRootView, targetContent = Regex("评论(.*?)，按钮"))// 打开评论区
            2 -> {} // 点赞
        }
    }

    private fun onLongPress(view: View, event: MotionEvent) {
        // 预留出长按快进
        if (event.x < screenSize.width / 8 || event.x > screenSize.width - screenSize.width / 8)
            return

        if (!config.isNeatMode)
            return

        if (!config.longPressMode) {
            if (event.y < screenSize.height / 2)
                return
        } else {
            if (event.y > screenSize.height / 2)
                return
        }

        val holderRootView = view.firstParentOrNull(VideoViewHolderRootView::class.java) ?: return
        showOptionsMenu(holderRootView)
    }

    private fun showOptionsMenu(view: ViewGroup) {
        if (HDetailPageFragment.isComment) return

        val items = getChoiceItems(view).toTypedArray()
        showChoiceDialog(
            context = view.context,
            title = "Freedom+",
            items = items,
            onChoice = { _, item, _ ->
                onChoiceSelected(view, item)
            }
        )
    }

    private fun getChoiceItems(view: View): List<String> {
        val items = mutableListOf("评论", "收藏", "分享")

        if (config.isDownload) {
            items.add("下载")
        }

        if (config.isNeatMode) {
            items.add(0, if (!config.neatModeState) "清爽模式" else "普通模式")
        }

        // if (config.isVideoFilter) {
        //     items.add("过滤统计")
        // }

        items.add("视频信息")

        if (!config.isDisablePlugin) {
            items.add("模块设置")
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
                onClickView(view, targetContent = Regex("评论(.*?)，按钮"))
            }

            "收藏" -> {
                onClickView(view, targetContent = Regex("收藏(.*?)，按钮"))
            }

            "分享" -> {
                onClickView(view, targetContent = Regex("分享(.*?)，按钮"))
            }

            "下载" -> {
                DownloadLogic(
                    hook = this@HLongPressLayout,
                    context = view.context,
                    aweme = aweme,
                )
            }

            "视频信息" -> {
                singleLaunchMain("$item") {
                    val msg = "视频属地: ${aweme?.cityInfo()}\n发布时间: ${aweme?.createDate()}".trim()
                    showMessageDialog(
                        context = view.context,
                        title = "视频信息",
                        content = msg,
                        singleButton = true,
                    )
                }
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
        }
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

    private fun onClickView(
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

    override fun onInit() {

        // 长按菜单事件拦截
        DexkitBuilder.longPressEventClazz?.also {
            lpparam.hookClass(it)
                .method(
                    "onLongPressAwemeSure",
                    Float::class.java,
                    Float::class.java,
                ) {
                    onBefore {
                        val x = args[0] as Float
                        val y = args[1] as Float

                        if (!config.isNeatMode)
                            return@onBefore

                        aweme = thisObject.findFieldGetValue { type(Aweme::class.java) }
                            ?: HVideoViewHolder.aweme

                        if (config.longPressMode) {
                            if (y < screenSize.height / 2)
                                result = Void.TYPE
                        } else {
                            if (y > screenSize.height / 2)
                                result = Void.TYPE
                        }
                    }
                }
        }

        // 双击点赞事件拦截
        DexkitBuilder.doubleClickEventClazz?.also {
            lpparam.hookClass(it)
                .methodAllByParamTypes(
                    View::class.java,
                    MotionEvent::class.java,
                    MotionEvent::class.java,
                    MotionEvent::class.java,
                ) {
                    onBefore {
                        if (!config.isDoubleClickType)
                            return@onBefore

                        if (config.doubleClickType == 1)
                            result = Void.TYPE
                    }
                }
        }
    }
}