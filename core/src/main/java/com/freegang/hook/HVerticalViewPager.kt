package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.activeActivity
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.other.KAutomationUtils
import com.freegang.ktutils.view.KFastClickUtils
import com.freegang.ktutils.view.traverse
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.thisView
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.feed.quick.presenter.FeedDoctorFrameLayout
import com.ss.android.ugc.aweme.feed.share.long_click.FrameLayoutHoldTouchListener
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.math.abs

class HVerticalViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<VerticalViewPager>(lpparam) {
    private val config get() = ConfigV1.get()
    private val screenSize get() = KDisplayUtils.screenSize()

    private var onDragListener: ViewTreeObserver.OnDrawListener? = null

    //long press
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var isLongPressFast = false
    private var longPressFastRunnable: Runnable? = null
    private var longPressRunnable: Runnable? = null

    //video pinch
    private var isVideoPinch = false

    override fun onInit() {
        lpparam.hookClass(VideoViewHolderRootView::class.java)
            .constructorsAll {
                onAfter {
                    onDragListener?.let {
                        thisView.viewTreeObserver.removeOnDrawListener(it)
                        onDragListener = null
                    }
                    onDragListener = ViewTreeObserver.OnDrawListener { toggleView(thisViewGroup) }
                    thisView.viewTreeObserver.addOnDrawListener(onDragListener)
                }
            }
            .method("dispatchTouchEvent", MotionEvent::class.java) {
                onBefore {
                    val event = args[0] as MotionEvent
                    val cancelEvent = MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        MotionEvent.ACTION_CANCEL,
                        event.x,
                        event.y,
                        event.metaState
                    )

                    //定时退出
                    if (config.isTimedExit) {
                        DouYinMain.freeExitCountDown?.cancel()
                        DouYinMain.freeExitCountDown?.start()
                    }

                    if (event.action == MotionEvent.ACTION_DOWN) {
                        //避免快速下发 ACTION_DOWN
                        if (KFastClickUtils.isFastDoubleClick(50L)) {
                            return@onBefore
                        }

                        //防止双击
                        if (KFastClickUtils.isFastDoubleClick(300L) && config.isDisableDoubleLike) {
                            thisView.dispatchTouchEvent(cancelEvent)
                            result = true
                            return@onBefore
                        }
                    }

                    //长按
                    handleLongPress(thisView, cancelEvent, event)
                }
            }

        DouYinMain.videoPinchClazz?.runCatching {
            lpparam.hookClass(this)
                .methodAll {
                    onBefore {
                        if (argsOrEmpty.size == 1) {
                            if (args[0] !is String) return@onBefore
                            //KLogCat.d("退出专注模式")
                            isVideoPinch = false
                        }
                        if (method.name.contains("getMOriginView")) {
                            //KLogCat.d("进入专注模式")
                            isVideoPinch = true
                        }
                    }
                }
        }
    }

    private fun toggleView(view: View) {
        view.traverse {
            if (it is PenetrateTouchRelativeLayout) {
                if (config.isTranslucent) it.alpha = 0.5f
                if (config.isNeatMode) {
                    it.isVisible = !config.neatModeState && !isLongPressFast
                    it.isVisible = !config.neatModeState && !isLongPressFast && !isVideoPinch
                } else {
                    it.isVisible = !isLongPressFast && !isVideoPinch
                }

                if (HDetailPageFragment.isComment) {
                    it.isVisible = false
                }
            }

            if (it is InteractStickerParent) {
                it.isVisible = !isVideoPinch
                if (config.isTranslucent) it.alpha = 0.5f
                if (config.isNeatMode) {
                    it.isVisible = !config.neatModeState && !isLongPressFast
                    it.isVisible = !config.neatModeState && !isLongPressFast && !isVideoPinch
                } else {
                    it.isVisible = !isLongPressFast && !isVideoPinch
                }
                if (HDetailPageFragment.isComment) {
                    it.isVisible = false
                }
            }
        }
    }

    private fun handleLongPress(
        view: View,
        cancel: MotionEvent,
        event: MotionEvent
    ) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y

                //预留出长按快进
                if (event.x < screenSize.width / 8 || event.x > screenSize.width - screenSize.width / 8) {
                    longPressFastRunnable = Runnable { isLongPressFast = true }
                    handler.postDelayed(longPressFastRunnable!!, 200L)
                    return
                }

                //非清爽模式
                if (!config.isNeatMode) {
                    return
                }

                //模块菜单显示逻辑
                if (config.longPressMode) {
                    if (event.y < screenSize.height / 2) {
                        longPressRunnable = Runnable {
                            showOptionsMenuV1(view)
                            view.dispatchTouchEvent(cancel)
                        }
                        handler.postDelayed(longPressRunnable!!, 300L)
                    }
                } else {
                    if (event.y > screenSize.height / 2) {
                        longPressRunnable = Runnable {
                            showOptionsMenuV1(view)
                            view.dispatchTouchEvent(cancel)
                        }
                        handler.postDelayed(longPressRunnable!!, 300L)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (abs(downX - event.x) < 10 && abs(downY - event.y) < 10) return //消除误差
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
    }

    private fun showOptionsMenuV1(view: View) {
        if (HDetailPageFragment.isComment) return

        val items = if (config.isNeatMode) {
            mutableListOf(if (!config.neatModeState) "清爽模式" else "普通模式", "评论", "收藏", "分享")
        } else {
            mutableListOf("评论", "收藏", "分享")
        }

        if (!config.isDisablePlugin) {
            items.add("模块设置")
        }

        showChoiceDialog(
            context = view.context,
            title = "Freedom+",
            items = items.toTypedArray(),
            onChoice = { it, item, _ ->
                when (item) {
                    "清爽模式", "普通模式" -> {
                        config.neatModeState = !config.neatModeState
                        toggleView(view)
                        showToast(it.context, if (config.neatModeState) "清爽模式" else "普通模式")
                    }

                    "评论" -> {
                        onClickViewV1(view, targetView = FeedDoctorFrameLayout::class.java)
                    }

                    "收藏" -> {
                        onClickViewV1(view, targetContent = Regex("收藏"))
                    }

                    "分享" -> {
                        onClickViewV1(view, targetView = FrameLayoutHoldTouchListener::class.java)
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
                }
            }
        )
    }

    private fun onClickViewV1(
        parent: View,
        targetView: Class<out View>? = null,
        targetText: Regex = Regex(""),
        targetHint: Regex = Regex(""),
        targetContent: Regex = Regex(""),
    ) {
        parent.traverse {
            var needClick = false
            if (it is TextView) {
                needClick = "${it.text}".containsNotEmpty(targetText)
                needClick = needClick || "${it.hint}".containsNotEmpty(targetHint)
            }
            needClick = needClick || "${it.contentDescription}".containsNotEmpty(targetContent)
            needClick = needClick || (targetView?.isInstance(it) ?: false)
            if (!needClick) return@traverse

            val location = IntArray(2) { 0 }
            it.getLocationOnScreen(location)
            if (location[1] > 0 && location[1] < screenSize.height) {
                location[0] = location[0] + it.right / 2
                location[1] = location[1] + it.bottom / 2
                if (!KFastClickUtils.isFastDoubleClick(200)) {
                    KAutomationUtils.simulateClickByView(it, location[0].toFloat(), location[1].toFloat())
                }
            }
        }
    }

    private fun CharSequence.containsNotEmpty(regex: Regex): Boolean {
        if (regex.pattern.isEmpty()) return false
        return contains(regex)
    }
}