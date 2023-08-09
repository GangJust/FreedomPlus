package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.delay
import kotlin.math.abs

class HVerticalViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<VerticalViewPager>(lpparam) {
    private val config get() = ConfigV1.get()
    private val screenSize get() = KDisplayUtils.screenSize()

    private var onDragListener: ViewTreeObserver.OnDrawListener? = null
    private var isLongPressFast = false
    private var longPressFastRunnable: Runnable? = null

    private var downX: Float = 0f
    private var downY: Float = 0f
    private var longPressRunnable: Runnable? = null

    override fun onInit() {
        lpparam.hookClass(VideoViewHolderRootView::class.java)
            .constructorsAll {
                onAfter {
                    if (onDragListener != null) {
                        thisView.viewTreeObserver.removeOnDrawListener(onDragListener!!)
                    }
                    onDragListener = ViewTreeObserver.OnDrawListener {
                        toggleView(thisViewGroup)
                    }
                    thisView.viewTreeObserver.addOnDrawListener(onDragListener)
                }
            }
            .method("dispatchTouchEvent", MotionEvent::class.java) {
                onBefore {
                    val event = args[0] as MotionEvent
                    val obtain = MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        MotionEvent.ACTION_CANCEL,
                        event.x,
                        event.y,
                        event.metaState
                    )

                    if (config.isTimedExit) {
                        DouYinMain.freeExitCountDown?.cancel()
                        DouYinMain.freeExitCountDown?.start()
                    }

                    //避免快速下发 ACTION_DOWN
                    if (KFastClickUtils.isFastDoubleClick(50L) && event.action == MotionEvent.ACTION_DOWN) {
                        return@onBefore
                    }

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.x
                            downY = event.y

                            //防止双击
                            if (KFastClickUtils.isFastDoubleClick(300L) && config.isDisableDoubleLike) {
                                thisView.dispatchTouchEvent(obtain)
                                result = true
                                return@onBefore
                            }

                            //预留出长按快进
                            if (event.x < screenSize.width / 8 || event.x > screenSize.width - screenSize.width / 8) {
                                longPressFastRunnable = Runnable { isLongPressFast = true }
                                handler.postDelayed(longPressFastRunnable!!, 200L)
                                return@onBefore
                            }

                            //
                            if (config.longPressMode) {
                                if (event.y < screenSize.height / 2) {
                                    longPressRunnable = Runnable {
                                        showOptionsMenuV1(thisViewGroup)
                                        thisView.dispatchTouchEvent(obtain)
                                    }
                                    handler.postDelayed(longPressRunnable!!, 300L)
                                    return@onBefore
                                }
                            } else {
                                if (event.y > screenSize.height / 2) {
                                    longPressRunnable = Runnable {
                                        showOptionsMenuV1(thisViewGroup)
                                        thisView.dispatchTouchEvent(obtain)
                                    }
                                    handler.postDelayed(longPressRunnable!!, 300L)
                                    return@onBefore
                                }
                            }
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (abs(downX - event.x) < 10 && abs(downY - event.y) < 10) return@onBefore //消除误差
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
            }
    }

    private fun toggleView(view: ViewGroup) {
        view.traverse {
            if (it is PenetrateTouchRelativeLayout) {
                if (config.isTranslucent) it.alpha = 0.5f
                it.isVisible = !(config.isNeatMode && config.neatModeState)
                it.isVisible = if (isLongPressFast) false else !(config.isNeatMode && config.neatModeState)

                if (HDetailPageFragment.isComment) {
                    it.isVisible = false
                }
            }

            if (it is InteractStickerParent) {
                if (config.isTranslucent) it.alpha = 0.5f
                it.isVisible = !(config.isNeatMode && config.neatModeState)
                it.isVisible = if (isLongPressFast) false else !(config.isNeatMode && config.neatModeState)

                if (HDetailPageFragment.isComment) {
                    it.isVisible = false
                }
            }
        }
    }

    private fun showOptionsMenuV1(view: ViewGroup) {
        if (HDetailPageFragment.isComment) return

        val items = if (config.isNeatMode) {
            arrayOf(if (!config.neatModeState) "清爽模式" else "普通模式", "评论", "收藏", "分享", "模块设置")
        } else {
            arrayOf("评论", "收藏", "分享", "模块设置")
        }
        showChoiceDialog(
            context = view.context,
            title = "Freedom+",
            items = items,
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
                        val intent = Intent(it.context, FreedomSettingActivity::class.java)
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
        parent: ViewGroup,
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

    private fun onSwipeView(
        view: View,
        msg: String,
    ) {
        launch {
            delay(500L)
            if (!KFastClickUtils.isFastDoubleClick(500L)) {
                if (msg.isNotEmpty()) showToast(view.context, msg)
                KAutomationUtils.simulateSwipeByView(
                    view,
                    screenSize.width / 2f,
                    screenSize.height / 2f + 200,
                    screenSize.width / 2f,
                    screenSize.height / 2f - 200,
                    20,
                )
            }
        }
    }

    private fun CharSequence.containsNotEmpty(regex: Regex): Boolean {
        if (regex.pattern.isEmpty()) return false
        return contains(regex)
    }
}