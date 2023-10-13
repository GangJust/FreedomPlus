package com.freegang.hook

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.removeInParent
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.CallConstructors
import com.freegang.xpler.core.EmptyHook
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.hookConstructorsAll
import com.freegang.xpler.core.thisViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HDisallowInterceptRelativeLayout(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<EmptyHook>(lpparam),
    CallConstructors {
    companion object {
        const val TAG = "HDisallowInterceptRelativeLayout"

        @get:Synchronized
        @set:Synchronized
        var isImmersive = false
    }

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.DisallowInterceptRelativeLayout")
    }

    private val config get() = ConfigV1.get()

    override fun onInit() {
        //旧版本
        lpparam.findClass("com.ss.android.ugc.aweme.feed.ui.DisallowInterceptRelativeLayout2")
            .hookConstructorsAll {
                onAfter {
                    callOnAfterConstructors(this)
                }
            }
    }

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {
    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            //全屏沉浸式
            if (!config.isImmersive) return

            thisViewGroup.postRunning {
                runCatching {
                    HDisallowInterceptRelativeLayout.isImmersive = false

                    traverse {
                        // 移除顶部间隔
                        if (javaClass.name == "android.view.View") {
                            removeInParent()
                        }
                        // 移除底部间隔
                        if (javaClass.name == "com.ss.android.ugc.aweme.feed.ui.bottom.BottomSpace") {
                            removeInParent()
                        }
                    }

                    // 设置沉浸式
                    val activity = context.asOrNull<Activity>() ?: return@runCatching
                    val window = activity.window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.navigationBarColor = Color.TRANSPARENT
                    window.statusBarColor = Color.TRANSPARENT

                    HDisallowInterceptRelativeLayout.isImmersive = true
                }.onFailure {
                    KLogCat.tagE(TAG, it)
                }
            }
        }
    }
}