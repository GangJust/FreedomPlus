package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.removeInParent
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.hookConstructorsAll
import com.freegang.xpler.core.interfaces.CallConstructors
import com.freegang.xpler.core.thisViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HDisallowInterceptRelativeLayout(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HDisallowInterceptRelativeLayout"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.DisallowInterceptRelativeLayout")
    }

    override fun onInit() {
        // 旧版本
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
            if (config.isImmersive) {
                thisViewGroup.postRunning {
                    runCatching {
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
                    }.onFailure {
                        KLogCat.tagE(TAG, it)
                    }
                }
            }
        }
    }
}