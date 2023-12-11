package io.github.fplus.core.hook

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.ImmersiveHelper
import io.github.xpler.core.OnAfter
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
            if (event.action == MotionEvent.ACTION_DOWN) {// 重新沉浸
                if (config.isImmersive) {
                    ImmersiveHelper.immersive(
                        thisActivity,
                        hideStatusBar = config.systemControllerValue[0],
                        hideNavigationBars = config.systemControllerValue[1],
                    )
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onCreate")
    fun onCreateAfter(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                ImmersiveHelper.immersive(
                    thisActivity,
                    hideStatusBar = config.systemControllerValue[0],
                    hideNavigationBars = config.systemControllerValue[1],
                )
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onResume")
    fun onResumeBefore(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            DouYinMain.freeExitCountDown?.restart()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}