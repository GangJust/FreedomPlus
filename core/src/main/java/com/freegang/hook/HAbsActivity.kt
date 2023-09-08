package com.freegang.hook

import com.bytedance.ies.uikit.base.AbsActivity
import com.freegang.base.BaseHook
import com.freegang.ktutils.app.contentView
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HAbsActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<AbsActivity>(lpparam) {
    @OnAfter("onResume")
    fun onResume(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            thisActivity.contentView.viewTreeObserver.apply {
                addOnGlobalLayoutListener {
                    DouYinMain.freeExitCountDown?.restart()
                }
                addOnScrollChangedListener {
                    DouYinMain.freeExitCountDown?.restart()
                }
            }
        }
    }

    @OnBefore("onPause")
    fun onPause(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            DouYinMain.freeExitCountDown?.cancel()
        }
    }
}