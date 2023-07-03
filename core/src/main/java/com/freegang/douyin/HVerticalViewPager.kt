package com.freegang.douyin

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.douyin.activity.FreedomSettingActivity
import com.freegang.ktutils.app.topActivity
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.other.KAutomationUtils
import com.freegang.ktutils.view.KFastClickUtils
import com.freegang.ktutils.view.KViewUtils
import com.freegang.ktutils.view.findParentExact
import com.freegang.ktutils.view.findViewsByType
import com.freegang.ktutils.view.traverse
import com.freegang.view.MaskView
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.thisView
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.feed.quick.presenter.FeedDoctorFrameLayout
import com.ss.android.ugc.aweme.feed.share.long_click.FrameLayoutHoldTouchListener
import com.ss.android.ugc.aweme.feed.ui.LongPressLayout
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HVerticalViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<VerticalViewPager>(lpparam) {
    private val config get() = ConfigV1.get()
    private val screenSize get() = KDisplayUtils.screenSize()
    private var longPressRunnable: Runnable? = null

    override fun onInit() {
        lpparam.hookClass(targetClazz)
            .methodAll {
                onAfter {
                    val first = argsOrEmpty.firstOrNull() ?: return@onAfter
                    if (config.isNeatMode) {
                        if (first is VerticalViewPager) {
                            toggleView(first)
                        }
                    }
                }
            }

        lpparam.hookClass(LongPressLayout::class.java)
            .method("onTouchEvent", MotionEvent::class.java) {
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

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            //防止双击
                            if (KFastClickUtils.isFastDoubleClick(500L) && config.isDisableDoubleLike) {
                                thisView.dispatchTouchEvent(obtain)
                                result = true
                                return@onBefore
                            }

                            //预留出长按快进
                            if (event.x < screenSize.width / 8 || event.x > screenSize.width - screenSize.width / 8) {
                                return@onBefore
                            }

                            //
                            if (config.longPressMode) {
                                if (event.y < screenSize.height / 2) {
                                    longPressRunnable = Runnable {
                                        val viewGroup = thisView.findParentExact(VideoViewHolderRootView::class.java)
                                        viewGroup ?: return@Runnable
                                        showOptionsMenuV1(viewGroup)
                                        thisView.dispatchTouchEvent(obtain)
                                    }
                                    handler.postDelayed(longPressRunnable!!, 300L)
                                    return@onBefore
                                }
                            } else {
                                if (event.y > screenSize.height / 2) {
                                    longPressRunnable = Runnable {
                                        val viewGroup = thisView.findParentExact(VideoViewHolderRootView::class.java)
                                        viewGroup ?: return@Runnable
                                        showOptionsMenuV1(viewGroup)
                                        thisView.dispatchTouchEvent(obtain)
                                    }
                                    handler.postDelayed(longPressRunnable!!, 300L)
                                    return@onBefore
                                }
                            }
                        }

                        else -> {
                            if (longPressRunnable != null) {
                                handler.removeCallbacks(longPressRunnable!!)
                            }
                        }
                    }

                }
            }
    }

    @Deprecated("Deprecated")
    @SuppressLint("ClickableViewAccessibility")
    private fun attachView(view: ViewGroup) {
        val parentExact = view.findParentExact(FrameLayout::class.java)
        val maskView = MaskView(view.context)
        if (parentExact?.findViewsByType(MaskView::class.java)?.isEmpty() == true) {
            parentExact.addView(maskView)
            maskView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //预留出长按快进
                        if (event.x < screenSize.width / 8 || event.x > screenSize.width - screenSize.width / 8) {
                            return@setOnTouchListener false
                        }

                        //如果是评论区视频/图片
                        if (isComment(view)) {
                            return@setOnTouchListener false
                        }

                        //
                        longPressRunnable = Runnable {
                            val obtain = MotionEvent.obtain(
                                event.downTime,
                                event.eventTime + 100,
                                MotionEvent.ACTION_CANCEL,
                                event.x,
                                event.y,
                                event.metaState
                            )
                            if (config.longPressMode) {
                                if (event.y < screenSize.height / 2) {
                                    showOptionsMenuV1(view)
                                    view.dispatchTouchEvent(obtain)
                                }
                            } else {
                                if (event.y > screenSize.height / 2) {
                                    showOptionsMenuV1(view)
                                    view.dispatchTouchEvent(obtain)
                                }
                            }
                        }
                        handler.postDelayed(longPressRunnable!!, 300L)
                        view.dispatchTouchEvent(event)
                        return@setOnTouchListener true
                    }

                    else -> {
                        if (longPressRunnable != null) {
                            handler.removeCallbacks(longPressRunnable!!)
                        }
                        view.dispatchTouchEvent(event)
                    }
                }
                return@setOnTouchListener false
            }
        }
    }

    @Deprecated("Deprecated")
    private fun isComment(viewGroup: ViewGroup): Boolean {
        return try {
            viewGroup.rootView.traverse {
                if (it is TextView) {
                    if (it.text.contains("保存")) {
                        throw Exception("true")
                    }
                    if (it.text.contains("我也发一张")) {
                        KViewUtils.hideAll(it.parent as ViewGroup)
                    }
                }
            }
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun showOptionsMenuV1(view: ViewGroup) {
        showChoiceDialog(
            context = view.context,
            title = "Freedom+",
            items = arrayOf(if (!config.neatModeState) "清爽模式" else "普通模式", "评论", "收藏", "分享", "模块设置"),
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
                        val options = ActivityOptions.makeCustomAnimation(
                            topActivity,
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )
                        it.context.startActivity(intent, options.toBundle())
                    }
                }
            }
        )
    }

    private fun toggleView(viewGroup: ViewGroup) {
        viewGroup.traverse {
            if (it is PenetrateTouchRelativeLayout) {
                if (config.isTranslucent) it.alpha = 0.5f
                it.visibility = if (config.isNeatMode && config.neatModeState) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            if (it is InteractStickerParent) {
                if (config.isTranslucent) it.alpha = 0.5f
                it.visibility = if (config.isNeatMode && config.neatModeState) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            if (it is TextView) {
                if (it.text.contains("我也发一张")) {
                    KViewUtils.hideAll(it.parent as ViewGroup)
                }
            }
        }
    }

    @Deprecated("Deprecated")
    private fun onClickView(
        parent: ViewGroup,
        targetView: Class<out View>? = null,
        targetText: Regex = Regex(""),
        targetHint: Regex = Regex(""),
        targetContent: Regex = Regex(""),
    ) {
        launch {
            //如果是清爽模式的状态下
            if (config.neatModeState) {
                //先取消控件隐藏
                config.neatModeState = false
                toggleView(parent)

                //等待300毫秒, 记录坐标
                delay(300)
                val location = IntArray(2) { 0 }
                parent.traverse {
                    var needClick = false
                    if (it is TextView) {
                        needClick = "${it.text}".containsNotEmpty(targetText)
                        needClick = needClick || "${it.hint}".containsNotEmpty(targetHint)
                    }
                    needClick = needClick || "${it.contentDescription}".containsNotEmpty(targetContent)
                    needClick = needClick || (targetView?.isInstance(it) ?: false)
                    if (!needClick) return@traverse

                    val temp = IntArray(2) { 0 }
                    it.getLocationOnScreen(temp)
                    if (temp[1] > 0 && temp[1] < screenSize.height) {
                        location[0] = temp[0] + it.right / 2
                        location[1] = temp[1] + it.bottom / 2

                        //模拟点击
                        KAutomationUtils.simulateClickByView(it, location[0].toFloat(), location[1].toFloat())

                        //恢复清爽模式
                        config.neatModeState = true
                        toggleView(parent)
                    }
                }
            } else {
                //等待300毫秒, 记录坐标
                delay(300)
                val location = IntArray(2) { 0 }
                parent.traverse {
                    var needClick = false
                    if (it is TextView) {
                        needClick = "${it.text}".containsNotEmpty(targetText)
                        needClick = needClick || "${it.hint}".containsNotEmpty(targetHint)
                    }
                    needClick = needClick || "${it.contentDescription}".containsNotEmpty(targetContent)
                    needClick = needClick || (targetView?.isInstance(it) ?: false)
                    if (!needClick) return@traverse

                    val temp = IntArray(2) { 0 }
                    it.getLocationOnScreen(temp)
                    if (temp[1] > 0 && temp[1] < screenSize.height) {
                        location[0] = temp[0] + it.right / 2
                        location[1] = temp[1] + it.bottom / 2
                        //模拟点击
                        KAutomationUtils.simulateClickByView(it, location[0].toFloat(), location[1].toFloat())
                    }
                }
            }
        }
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

            val temp = IntArray(2) { 0 }
            it.getLocationOnScreen(temp)
            if (temp[1] > 0 && temp[1] < screenSize.height) {
                val location = IntArray(2) { 0 }
                location[0] = temp[0] + it.right / 2
                location[1] = temp[1] + it.bottom / 2
                KAutomationUtils.simulateClickByView(it, location[0].toFloat(), location[1].toFloat())
            }
        }
    }

    private fun CharSequence.containsNotEmpty(regex: Regex): Boolean {
        if (regex.pattern.isEmpty()) return false
        return contains(regex)
    }
}