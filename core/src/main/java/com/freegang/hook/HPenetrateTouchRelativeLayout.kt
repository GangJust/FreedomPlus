package com.freegang.hook

import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HPenetrateTouchRelativeLayout(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<PenetrateTouchRelativeLayout>(lpparam), CallMethods {
    companion object {
        const val TAG = "HPenetrateTouchRelativeLayout"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            //全屏沉浸式底部垫高
            if (config.isImmersive) {
                if (HDisallowInterceptRelativeLayout.isImmersive) {
                    thisViewGroup.apply {
                        updatePadding(bottom = context.dip2px(58f))
                    }
                }
            }
        }
    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            //半透明
            if (config.isTranslucent) {
                if (method.name.contains("Alpha")) return
                thisViewGroup.postRunning {
                    traverse {
                        runCatching {
                            if (this !is ViewGroup) {
                                alpha = 0.5f
                            }
                        }.onFailure {
                            KLogCat.tagE(TAG, it)
                        }
                    }
                }
            }
        }
    }
}