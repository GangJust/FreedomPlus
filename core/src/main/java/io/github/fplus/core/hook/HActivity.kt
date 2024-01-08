package io.github.fplus.core.hook

import android.app.Activity
import android.view.MotionEvent
import androidx.core.view.updatePadding
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.navBarInteractionMode
import com.freegang.ktutils.app.navigationBarHeight
import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity

class HActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Activity>(lpparam) {
    companion object {
        const val TAG = "HActivity"
    }

    private val config get() = ConfigV1.get()

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(param) {
            DouYinMain.freeExitCountDown?.restart()
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
    fun onResumeBefore(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            DouYinMain.freeExitCountDown?.restart()

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