package io.github.fplus.core.hook

import android.app.Activity
import android.view.MotionEvent
import androidx.core.view.updatePadding
import com.freegang.extension.contentView
import com.freegang.extension.navBarInteractionMode
import com.freegang.extension.navigationBarHeight
import com.ss.android.ugc.aweme.live.LivePlayActivity
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HActivity : BaseHook() {
    companion object {
        const val TAG = "HActivity"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return Activity::class.java
    }

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            val activity = thisObject as Activity
            DouYinMain.freeExitHelper?.restart()

            if (activity is FreedomSettingActivity)
                return

            if (activity is LivePlayActivity)
                DouYinMain.freeExitHelper?.cancel()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onResume")
    fun onResumeBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val activity = thisObject as Activity

            if (activity is LivePlayActivity) {
                DouYinMain.freeExitHelper?.cancel()
            } else {
                DouYinMain.freeExitHelper?.restart()
            }

            if (DouYinMain.timerExitHelper?.isPaused == true) {
                DouYinMain.timerExitHelper?.restart()
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        hookBlockRunning(params) {
            singleLaunchMain {
                val activity = thisObject as Activity

                if (activity is FreedomSettingActivity)
                    return@singleLaunchMain

                immersive(activity)
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    private fun immersive(activity: Activity) {
        if (config.isImmersive) {
            ImmersiveHelper.immersive(
                activity = activity,
                hideStatusBar = config.systemControllerValue[0],
                hideNavigationBars = config.systemControllerValue[1],
            )
            ImmersiveHelper.systemBarColor(activity)

            // 底部三键导航
            if (activity.navBarInteractionMode == 0 && !config.systemControllerValue[1]) {
                activity.contentView.apply {
                    updatePadding(bottom = context.navigationBarHeight)
                }
            }
        }
    }
}