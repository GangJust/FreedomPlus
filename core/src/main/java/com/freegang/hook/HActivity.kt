package com.freegang.hook

import android.app.Activity
import android.view.MotionEvent
import com.freegang.base.BaseHook
import com.freegang.xpler.core.OnBefore
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Activity>(lpparam) {

    @OnBefore("dispatchTouchEvent")
    fun dispatchTouchEventBefore(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlock(param) {
            DouYinMain.freeExitCountDown?.restart()
        }
    }

    @OnBefore("onResume")
    fun onResumeBefore(param: XC_MethodHook.MethodHookParam) {
        hookBlock(param) {
            DouYinMain.freeExitCountDown?.restart()
        }
    }
}