package com.freegang.hook

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.navBarInteractionMode
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.EmptyHook
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HLivePlayActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<EmptyHook>(lpparam) {

    companion object {
        const val TAG = "HLivePlayActivity"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.live.LivePlayActivity")
    }

    @OnAfter("onWindowFocusChanged")
    fun onWindowFocusChangedAfter(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        hookBlockRunning(params) {
            initImmersive(thisActivity)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun initImmersive(activity: Activity) {
        // 全屏沉浸式
        if (config.isImmersive) {
            val window = activity.window
            if (config.systemControllerValue[0]) {
                WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
            }
            if (config.systemControllerValue[1]) {
                WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.navigationBars())
            }
            if (activity.navBarInteractionMode == 2) {
                HMainActivity.isEdgeToEdgeEnabled = true
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
            } else {
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.parseColor("#161616")
            }
        }
    }
}