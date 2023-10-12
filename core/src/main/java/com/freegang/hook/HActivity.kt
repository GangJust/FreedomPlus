package com.freegang.hook

import android.app.Activity
import android.view.MotionEvent
import com.freegang.base.BaseHook
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Activity>(lpparam) {
    companion object {
        const val TAG = "HActivity"
    }

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(param) {
            DouYinMain.freeExitCountDown?.restart()
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    @OnBefore("onResume")
    fun onResumeBefore(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            DouYinMain.freeExitCountDown?.restart()
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }
}