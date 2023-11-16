package com.freegang.hook

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.ImmersiveHelper
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Activity>(lpparam) {
    companion object {
        const val TAG = "HActivity"
    }

    private val config get() = ConfigV1.get()

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(param) {
            DouYinMain.freeExitCountDown?.restart()
            if (event.action == MotionEvent.ACTION_DOWN) {// 重新沉浸
                ImmersiveHelper.with(thisActivity, config)
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onCreate")
    fun onCreateAfter(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlockRunning(params) {
            ImmersiveHelper.with(thisActivity, config)
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