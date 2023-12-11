package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.removeInParent
import com.freegang.ktutils.view.onEachChild
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.findClass
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookConstructorsAll
import io.github.xpler.core.interfaces.CallConstructors
import io.github.xpler.core.thisViewGroup

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

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {
    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                thisViewGroup.postRunning {
                    runCatching {
                        onEachChild {
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