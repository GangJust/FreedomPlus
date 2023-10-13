package com.freegang.hook

import android.view.MotionEvent
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.display.KDisplayUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.view.KFastClickUtils
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisView
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVerticalViewPagerNew(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<VerticalViewPager>(lpparam) {
    companion object {
        const val TAG = "HVerticalViewPager"
    }

    private val config get() = ConfigV1.get()

    private val screenSize get() = KDisplayUtils.screenSize()

    //
    private var currentAweme: Aweme? = null
    private var durationRunnable: Runnable? = null

    @OnAfter("onInterceptTouchEvent")
    fun onInterceptTouchEvent(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        longVideoJudge(param, event)
        longPressChoice(param, event)
    }

    private fun longVideoJudge(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(param) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val adapter = thisObject.methodInvokeFirst("getAdapter") ?: return
                    val currentItem = thisObject.methodInvokeFirst("getCurrentItem") as? Int ?: return
                    currentAweme = adapter.methodInvokeFirst(
                        returnType = Aweme::class.java,
                        args = arrayOf(currentItem),
                    ) as? Aweme

                    //
                    if (config.isLongtimeVideoToast) {
                        durationRunnable?.run {
                            handler.removeCallbacks(this)
                            durationRunnable = null
                        }
                        durationRunnable = Runnable {
                            //
                            val delayItem = thisObject.methodInvokeFirst("getCurrentItem") as? Int ?: return@Runnable
                            if (delayItem == currentItem) {
                                return@Runnable
                            }

                            //
                            val delayAweme = adapter.methodInvokeFirst(
                                returnType = Aweme::class.java,
                                args = arrayOf(delayItem),
                            ) as? Aweme
                            val duration = delayAweme?.duration ?: 0
                            if (duration >= 1000 * 60 * 10) {
                                val minute = duration / 1000 / 60
                                val second = duration / 1000 % 60
                                KToastUtils.show(thisView.context, "请注意, 本条视频时长${minute}分${second}秒!")
                            }
                        }
                        handler.postDelayed(durationRunnable!!, 3000L)
                    }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun longPressChoice(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
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
                    return
                }

                // 防止双击
                if (KFastClickUtils.isFastDoubleClick(300L) && config.isDisableDoubleLike) {
                    thisView.dispatchTouchEvent(cancelEvent)
                    result = true
                    return
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}