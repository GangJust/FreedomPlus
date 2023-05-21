package com.freegang.douyin

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.view.KDialog
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.callMethod
import com.freegang.xpler.core.getModuleDrawable
import com.freegang.xpler.core.inflateModuleView
import com.freegang.xpler.databinding.DialogFreedomLayoutBinding
import com.freegang.xpler.utils.reflect.KXpTest
import com.freegang.xpler.utils.view.KViewUtils
import com.freegang.xpler.utils.view.findParentExact
import com.freegang.xpler.utils.view.findViewsByType
import com.freegang.xpler.utils.view.traverse
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.familiar.feed.slides.ui.SlidesPhotosViewPager
import com.ss.android.ugc.aweme.feed.quick.presenter.FeedDoctorFrameLayout
import com.ss.android.ugc.aweme.feed.share.long_click.FrameLayoutHoldTouchListener
import com.ss.android.ugc.aweme.feed.ui.LongPressLayout
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay
import kotlin.math.sqrt


class HVerticalViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<VerticalViewPager>(lpparam) {
    private val config: Config = Config.get()

    private var listener: Any? = null

    private var view: ViewGroup? = null

    //自定义长按事件
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var pressDuration = 500L //设置自定义的长按时间，单位为毫秒
    private var isLongPressTriggered = false
    private val longPressRunnable = Runnable {
        showOptionsMenu()
    }

    override fun onInit() {
        KtXposedHelpers.hookClass(targetClazz)
            .methodAll {
                onAfter {
                    //KXpTest.testXC_M(this)
                    val viewGroup = argsOrEmpty.filterIsInstance<ViewGroup>().firstOrNull() ?: return@onAfter
                    changeView(viewGroup)
                }
            }

        //暂存长按事件
        KtXposedHelpers.hookClass(LongPressLayout::class.java)
            .methodAll {
                onBefore {
                    //KXpTest.testXC_M(this)
                    if (method.name.contains("setListener")) {
                        listener = argsOrEmpty.firstOrNull()
                        result = Unit
                    }
                }
            }

        //图文的 ViewPager 在 onInterceptTouchEvent 中消费了触摸事件, 由这里重新设置点击事件
        //当出现 onTouchEvent 时, 则表示在左右滑动, 跳过事件处理
        KtXposedHelpers.hookClass(SlidesPhotosViewPager::class.java)
            .methodAll {
                onAfter {
                    //KXpTest.testXC_M(this)
                    if (method.name.contains("onInterceptTouchEvent|onTouchEvent".toRegex())) {
                        val event = argsOrEmpty.firstOrNull() as MotionEvent? ?: return@onAfter
                        val screenWidth = Resources.getSystem().displayMetrics.widthPixels // 获取屏幕宽度
                        val responsiveWidth = screenWidth * (2 / 3f) // 计算响应范围的宽度

                        view = (thisObject as SlidesPhotosViewPager).findParentExact(VerticalViewPager::class.java)
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                // 判断触摸点的位置: [屏幕左侧(1/6) | 屏幕中间 | 屏幕右侧(1/6)], 只响应屏幕中间的点击事件
                                if (event.x < responsiveWidth / 6 || event.x > screenWidth - responsiveWidth / 6) {
                                    return@onAfter // 不处理触摸事件，直接返回
                                }

                                // 当用户按下时触发，开始计时并记录初始触摸点的位置
                                handler.postDelayed(longPressRunnable, pressDuration) // 设置延迟时间，单位为毫秒
                                isLongPressTriggered = false // 重置长按事件触发状态
                                initialTouchX = event.x // 记录初始触摸点的水平位置
                                initialTouchY = event.y // 记录初始触摸点的垂直位置
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val moveX = event.x - initialTouchX // 计算触摸点在水平方向上的移动距离
                                val moveY = event.y - initialTouchY // 计算触摸点在垂直方向上的移动距离
                                val moveDistance = sqrt((moveX * moveX + moveY * moveY).toDouble()) // 计算总的移动距离

                                // 判断是否满足触发长按事件的条件：尚未触发长按、满足自定义的长按条件、移动距离不超过阈值
                                if (!isLongPressTriggered && isLongPress(event, pressDuration) && moveDistance <= 100) {
                                    isLongPressTriggered = true // 设置长按事件已触发
                                }
                            }

                            else -> {
                                // 当触摸事件结束或取消时触发，取消长按的延迟任务并重置相关状态
                                handler.removeCallbacks(longPressRunnable)
                                isLongPressTriggered = false
                            }
                        }
                    }
                }
            }
    }

    //改变View状态
    @SuppressLint("ClickableViewAccessibility")
    private fun changeView(viewGroup: ViewGroup) {
        //KXpTest.testViewGroup(viewGroup)
        viewGroup.traverse {
            //透明度、清爽模式
            if (it is PenetrateTouchRelativeLayout) {
                it.isVisible = config.isNeat && !config.neatState
                it.traverse { v ->
                    if (config.isTranslucent && v !is ViewGroup) v.alpha = 0.5f
                }
            }

            if(it is InteractStickerParent){
                it.isVisible = config.isNeat && !config.neatState
                it.traverse { v ->
                    if (config.isTranslucent && v !is ViewGroup) v.alpha = 0.5f
                }
            }

            //自定义长按事件, 响应操作菜单 (视频)
            if (it is LongPressLayout) {
                val description = it.contentDescription ?: ""
                if (description.contains("进入直播间")) return@traverse

                val screenWidth = Resources.getSystem().displayMetrics.widthPixels // 获取屏幕宽度
                val responsiveWidth = screenWidth * (2 / 3f) // 计算响应范围的宽度
                it.setOnTouchListener { _, event ->
                    view = viewGroup
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // 判断触摸点的位置: [屏幕左侧(1/5) | 屏幕中间 | 屏幕右侧(1/5)], 只响应屏幕中间的点击事件
                            if (event.x < responsiveWidth / 5 || event.x > screenWidth - responsiveWidth / 5) {
                                return@setOnTouchListener true // 不处理触摸事件，直接返回 true
                            }

                            // 当用户按下时触发，开始计时并记录初始触摸点的位置
                            handler.postDelayed(longPressRunnable, pressDuration) // 设置延迟时间，单位为毫秒
                            isLongPressTriggered = false // 重置长按事件触发状态
                            initialTouchX = event.x // 记录初始触摸点的水平位置
                            initialTouchY = event.y // 记录初始触摸点的垂直位置
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val moveX = event.x - initialTouchX // 计算触摸点在水平方向上的移动距离
                            val moveY = event.y - initialTouchY // 计算触摸点在垂直方向上的移动距离
                            val moveDistance = sqrt((moveX * moveX + moveY * moveY).toDouble()) // 计算总的移动距离

                            // 判断是否满足触发长按事件的条件：尚未触发长按、满足自定义的长按条件、移动距离不超过阈值
                            if (!isLongPressTriggered && isLongPress(event, pressDuration) && moveDistance <= 100) {
                                isLongPressTriggered = true // 设置长按事件已触发
                            }
                        }

                        else -> {
                            // 当触摸事件结束或取消时触发，取消长按的延迟任务并重置相关状态
                            handler.removeCallbacks(longPressRunnable)
                            isLongPressTriggered = false
                        }
                    }
                    false
                }
            }
        }
    }

    // 判断手指是否在视图上保持足够长的时间（自定义的长按时间）
    private fun isLongPress(event: MotionEvent, pressDuration: Long): Boolean {
        val eventDuration = event.eventTime - event.downTime
        return eventDuration >= pressDuration
    }

    //弹出选项菜单
    private fun showOptionsMenu() {
        //val view = view!!.rootView as ViewGroup
        val view = view!!

        val dialog = KDialog()
        val frameLayout = view.context.inflateModuleView<FrameLayout>(R.layout.dialog_freedom_layout)
        dialog.setView(frameLayout)

        val binding = DialogFreedomLayoutBinding.bind(frameLayout)
        binding.freedomDialogContainer.background = view.context.getModuleDrawable(R.drawable.dialog_background)

        //清爽模式
        binding.neatItemText.text = if (!config.neatState) "清爽模式" else "普通模式"
        binding.neatItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.neatItemText.setOnClickListener {
            dialog.dismiss()
            config.neatState = !config.neatState
            changeView(view)
            Toast.makeText(it.context, if (config.neatState) "清爽模式" else "普通模式", Toast.LENGTH_SHORT).show()
        }

        //评论
        binding.commentItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.commentItemText.setOnClickListener {
            dialog.dismiss()
            launch {
                KXpTest.testViewGroup(view)

                //如果是清爽模式的状态下
                if (config.neatState) {
                    //先取消控件隐藏
                    config.neatState = false
                    changeView(view)

                    //等待200毫秒, 记录坐标, 评论按钮
                    delay(200)
                    val commentOutLocation = IntArray(2) { 0 }
                    view.findViewsByType(FeedDoctorFrameLayout::class.java).lastOrNull()?.getLocationOnScreen(commentOutLocation)

                    //模拟点击
                    KViewUtils.motionClickView(view, commentOutLocation[0].toFloat(), commentOutLocation[1].toFloat())

                    //恢复清爽模式
                    config.neatState = true
                    changeView(view)
                } else {
                    //记录坐标, 评论按钮
                    val commentOutLocation = IntArray(2) { 0 }
                    view.findViewsByType(FeedDoctorFrameLayout::class.java).lastOrNull()?.getLocationOnScreen(commentOutLocation)

                    //等待200毫秒, 模拟点击
                    delay(200)
                    KViewUtils.motionClickView(view, commentOutLocation[0].toFloat(), commentOutLocation[1].toFloat())
                }
            }
        }

        //分享
        binding.shareItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.shareItemText.setOnClickListener {
            dialog.dismiss()
            launch {
                //如果是清爽模式的状态下
                if (config.neatState) {
                    //先取消控件隐藏
                    config.neatState = false
                    changeView(view)

                    //等待200毫秒, 记录坐标, 分享按钮
                    delay(200)
                    val commentOutLocation = IntArray(2) { 0 }
                    view.findViewsByType(FrameLayoutHoldTouchListener::class.java).lastOrNull()?.getLocationOnScreen(commentOutLocation)

                    //模拟点击
                    KViewUtils.motionClickView(view, commentOutLocation[0].toFloat(), commentOutLocation[1].toFloat())

                    //恢复清爽模式
                    config.neatState = true
                    changeView(view)
                } else {
                    //记录坐标, 分享按钮
                    val commentOutLocation = IntArray(2) { 0 }
                    view.findViewsByType(FrameLayoutHoldTouchListener::class.java).lastOrNull()?.getLocationOnScreen(commentOutLocation)

                    //等待200毫秒, 模拟点击
                    delay(200)
                    KViewUtils.motionClickView(view, commentOutLocation[0].toFloat(), commentOutLocation[1].toFloat())
                }
            }
        }

        //菜单
        binding.awemeItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.awemeItemText.setOnClickListener {
            dialog.dismiss()
            listener?.callMethod<Unit>("onLongPressAwemeSure", initialTouchX, initialTouchY)
        }

        //取消
        binding.choiceDialogCancel.background = view.context.getModuleDrawable(R.drawable.dialog_single_button_background)
        binding.choiceDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}