package com.freegang.douyin

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.activity.FreedomSettingActivity
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.topActivity
import com.freegang.ktutils.other.KAutomationUtils
import com.freegang.ktutils.view.findParentExact
import com.freegang.ktutils.view.findViewsByDesc
import com.freegang.ktutils.view.findViewsByType
import com.freegang.ktutils.view.parentView
import com.freegang.ktutils.view.traverse
import com.freegang.view.KDialog
import com.freegang.view.MaskView
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.getModuleDrawable
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.inflateModuleView
import com.freegang.xpler.databinding.DialogFreedomLayoutBinding
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.feed.quick.presenter.FeedDoctorFrameLayout
import com.ss.android.ugc.aweme.feed.share.long_click.FrameLayoutHoldTouchListener
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HVerticalViewPager(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<VerticalViewPager>(lpparam) {
    private val config get() = Config.get()

    private var view: ViewGroup? = null
    private var dialog: KDialog? = null
    private var longPressRunnable: Runnable? = null

    override fun onInit() {
        //视频
        lpparam.hookClass(targetClazz)
            .method("getCurrentItem") {
                onAfter {
                    if (config.isNeat) {
                        view = thisObject as? ViewGroup
                        attachView()
                    }
                }
            }

        //赞、评论等
        lpparam.hookClass(PenetrateTouchRelativeLayout::class.java)
            .methodAll {
                onBefore {
                    //if (method.name.contains("setAlpha")) return@onBefore
                    if (method.name.contains("setVisibility")) return@onBefore
                    val root = thisObject as ViewGroup
                    //透明度
                    if (config.isTranslucent) {
                        root.parentView.alpha = 0.5f
                    }
                    //清爽模式
                    if (!config.isNeat) {
                        root.isVisible = true
                    } else {
                        root.isVisible = !config.neatState
                    }
                }
                onAfter {
                    //if (method.name.contains("setAlpha")) return@onAfter
                    if (method.name.contains("setVisibility")) return@onAfter
                    val root = thisObject as ViewGroup
                    //透明度
                    if (config.isTranslucent) {
                        root.parentView.alpha = 0.5f
                    }
                    //清爽模式
                    if (!config.isNeat) {
                        root.isVisible = true
                    } else {
                        root.isVisible = !config.neatState
                    }
                }
            }

        //悬浮挑战等
        lpparam.hookClass(InteractStickerParent::class.java)
            .methodAll {
                onBefore {
                    if (method.name.contains("setAlpha")) return@onBefore
                    if (method.name.contains("setVisibility")) return@onBefore
                    val root = thisObject as ViewGroup
                    //清爽模式
                    root.isVisible = config.isNeat && !config.neatState
                    //透明度
                    root.traverse { v ->
                        if (config.isTranslucent && v !is ViewGroup) v.alpha = 0.5f
                    }
                }
                onAfter {
                    if (method.name.contains("setAlpha")) return@onAfter
                    if (method.name.contains("setVisibility")) return@onAfter
                    val root = thisObject as ViewGroup
                    //清爽模式
                    root.isVisible = config.isNeat && !config.neatState
                    //透明度
                    root.traverse { v ->
                        if (config.isTranslucent && v !is ViewGroup) v.alpha = 0.5f
                    }
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachView() {
        val view = view!!
        val parentExact = view.findParentExact(FrameLayout::class.java)
        val maskView = MaskView(view.context)
        if (parentExact?.findViewsByType(MaskView::class.java)?.isEmpty() == true) {
            parentExact.addView(maskView)
            val size = KAppUtils.screenSize()
            maskView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //预留出长按快进
                        if (event.x < size.width / 8 || event.x > size.width - size.width / 8) {
                            return@setOnTouchListener false
                        }

                        //如果是评论区视频/图片
                        if (isComment(view)) {
                            return@setOnTouchListener false
                        }

                        //
                        longPressRunnable = Runnable {
                            val obtain = MotionEvent.obtain(
                                event.downTime, event.eventTime, MotionEvent.ACTION_CANCEL, event.x, event.y, event.metaState
                            )
                            if (config.isLongPressMode) {
                                if (event.y < size.height / 2) {
                                    showOptionsMenu()
                                    view.dispatchTouchEvent(obtain)
                                }
                            } else {
                                if (event.y > size.height / 2) {
                                    showOptionsMenu()
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

    //弹出选项菜单
    private fun showOptionsMenu() {
        val view = view!!
        val screenSize = KAppUtils.screenSize()

        val dialog = this.dialog ?: KDialog()
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
            toggleView(view)
            Toast.makeText(it.context, if (config.neatState) "清爽模式" else "普通模式", Toast.LENGTH_SHORT).show()
        }

        //评论
        binding.commentItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.commentItemText.setOnClickListener {
            dialog.dismiss()
            launch {
                //如果是清爽模式的状态下
                if (config.neatState) {
                    //先取消控件隐藏
                    config.neatState = false
                    toggleView(view)

                    //等待200毫秒, 记录坐标
                    delay(200)
                    val location = IntArray(2) { 0 }
                    view.findViewsByType(FeedDoctorFrameLayout::class.java).forEach {
                        val temp = IntArray(2) { 0 }
                        it.getLocationOnScreen(temp)
                        if (temp[1] > 0 && temp[1] < screenSize.height) {
                            location[0] = temp[0] + it.right / 2
                            location[1] = temp[1] + it.bottom / 2
                        }
                    }

                    //模拟点击
                    KAutomationUtils.simulateClickByView(view, location[0].toFloat(), location[1].toFloat())

                    //恢复清爽模式
                    config.neatState = true
                    toggleView(view)
                } else {
                    //等待200毫秒, 记录坐标
                    delay(200)
                    val location = IntArray(2) { 0 }
                    view.findViewsByType(FeedDoctorFrameLayout::class.java).forEach {
                        val temp = IntArray(2) { 0 }
                        it.getLocationOnScreen(temp)
                        if (temp[1] > 0 && temp[1] < screenSize.height) {
                            location[0] = temp[0] + it.right / 2
                            location[1] = temp[1] + it.bottom / 2
                        }
                    }

                    //模拟点击
                    KAutomationUtils.simulateClickByView(view, location[0].toFloat(), location[1].toFloat())
                }
            }
        }

        //收藏
        binding.favoriteItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.favoriteItemText.setOnClickListener {
            dialog.dismiss()
            launch {
                //如果是清爽模式的状态下
                if (config.neatState) {
                    //先取消控件隐藏
                    config.neatState = false
                    toggleView(view)

                    //等待200毫秒, 记录坐标
                    delay(200)
                    val location = IntArray(2) { 0 }
                    view.findViewsByDesc(View::class.java, "收藏").forEach {
                        val temp = IntArray(2) { 0 }
                        it.getLocationOnScreen(temp)
                        if (temp[1] > 0 && temp[1] < screenSize.height) {
                            location[0] = temp[0] + it.right / 2
                            location[1] = temp[1] + it.bottom / 2
                        }
                    }

                    //模拟点击
                    KAutomationUtils.simulateClickByView(view, location[0].toFloat(), location[1].toFloat())

                    //恢复清爽模式
                    config.neatState = true
                    toggleView(view)
                } else {
                    //等待200毫秒, 记录坐标
                    delay(200)
                    val location = IntArray(2) { 0 }
                    view.findViewsByDesc(View::class.java, "收藏").forEach {
                        val temp = IntArray(2) { 0 }
                        it.getLocationOnScreen(temp)
                        if (temp[1] > 0 && temp[1] < screenSize.height) {
                            location[0] = temp[0] + it.right / 2
                            location[1] = temp[1] + it.bottom / 2
                        }
                    }

                    //模拟点击
                    KAutomationUtils.simulateClickByView(view, location[0].toFloat(), location[1].toFloat())
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
                    toggleView(view)

                    //等待200毫秒, 记录坐标
                    delay(200)
                    val location = IntArray(2) { 0 }
                    view.findViewsByType(FrameLayoutHoldTouchListener::class.java).forEach {
                        val temp = IntArray(2) { 0 }
                        it.getLocationOnScreen(temp)
                        if (temp[1] > 0 && temp[1] < screenSize.height) {
                            location[0] = temp[0] + it.right / 2
                            location[1] = temp[1] + it.bottom / 2
                        }
                    }

                    //模拟点击
                    KAutomationUtils.simulateClickByView(view, location[0].toFloat(), location[1].toFloat())

                    //恢复清爽模式
                    config.neatState = true
                    toggleView(view)
                } else {
                    //等待200毫秒, 记录坐标
                    delay(200)
                    val location = IntArray(2) { 0 }
                    view.findViewsByType(FrameLayoutHoldTouchListener::class.java).forEach {
                        val temp = IntArray(2) { 0 }
                        it.getLocationOnScreen(temp)
                        if (temp[1] > 0 && temp[1] < screenSize.height) {
                            location[0] = temp[0] + it.right / 2
                            location[1] = temp[1] + it.bottom / 2
                        }
                    }

                    //模拟点击
                    KAutomationUtils.simulateClickByView(view, location[0].toFloat(), location[1].toFloat())
                }
            }
        }

        //模块设置
        binding.settingItemText.background = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.settingItemText.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(it.context, FreedomSettingActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                topActivity,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            it.context.startActivity(intent, options.toBundle())
        }

        //取消
        binding.choiceDialogCancel.background = view.context.getModuleDrawable(R.drawable.dialog_single_button_background)
        binding.choiceDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        if (isComment(view)) return

        dialog.show()
    }

    private fun toggleView(viewGroup: ViewGroup) {
        viewGroup.traverse {
            if (it is PenetrateTouchRelativeLayout) {
                //透明度
                if (config.isTranslucent) {
                    it.parentView.alpha = 0.5f
                }
                //清爽模式
                if (!config.isNeat) {
                    it.isVisible = true
                } else {
                    it.isVisible = !config.neatState
                }
            }

            if (it is InteractStickerParent) {
                it.isVisible = config.isNeat && !config.neatState
                it.traverse { v ->
                    if (config.isTranslucent && v !is ViewGroup) v.alpha = 0.5f
                }
            }
        }
    }

    private fun isComment(viewGroup: ViewGroup): Boolean {
        return try {
            val rootView = viewGroup.rootView as ViewGroup
            rootView.traverse {
                if (it is TextView) {
                    if (it.text.contains("保存")) {
                        throw Exception("true")
                    }
                }
            }
            false
        } catch (e: Exception) {
            true
        }
    }
}