package io.github.fplus.core.hook

import android.app.Activity
import android.view.MotionEvent
import androidx.core.view.updatePadding
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.navBarInteractionMode
import com.freegang.ktutils.app.navigationBarHeight
import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity

class HActivity : BaseHook<Activity>() {
    companion object {
        const val TAG = "HActivity"
    }

    private val config get() = ConfigV1.get()

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            DouYinMain.freeExitCountDown?.restart()

            if (thisActivity is FreedomSettingActivity) return

            if (config.isImmersive) {
                // 底部三键导航
                val activity = thisObject as Activity
                if (activity.navBarInteractionMode == 0 && !config.systemControllerValue[1]) {
                    ImmersiveHelper.systemBarColor(activity, navigationBarColor = null)
                } else {
                    ImmersiveHelper.systemBarColor(activity)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onResume")
    fun onResumeBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            DouYinMain.freeExitCountDown?.restart()

            if (thisActivity is FreedomSettingActivity) return

            if (config.isImmersive) {
                ImmersiveHelper.immersive(
                    activity = thisActivity,
                    hideStatusBar = config.systemControllerValue[0],
                    hideNavigationBars = config.systemControllerValue[1],
                )

                // 底部三键导航
                val activity = thisObject as Activity
                if (activity.navBarInteractionMode == 0 && !config.systemControllerValue[1]) {
                    activity.contentView.apply {
                        updatePadding(bottom = context.navigationBarHeight)
                    }
                    ImmersiveHelper.systemBarColor(activity, navigationBarColor = null)
                } else {
                    ImmersiveHelper.systemBarColor(activity)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}