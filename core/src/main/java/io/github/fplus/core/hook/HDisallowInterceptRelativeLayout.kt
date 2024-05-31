package io.github.fplus.core.hook

import com.freegang.extension.forEachChild
import com.freegang.extension.postRunning
import com.freegang.extension.removeInParent
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.findClass
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookConstructorsAll
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup
import io.github.xpler.core.wrapper.CallConstructors

class HDisallowInterceptRelativeLayout : BaseHook(),
    CallConstructors {
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
            if (!config.isImmersive)
                return

            thisViewGroup.postRunning {
                runCatching {
                    it.forEachChild { child ->
                        // 移除顶部间隔
                        if (child.javaClass.name == "android.view.View") {
                            child.removeInParent()
                        }
                        // 移除底部间隔
                        if (child.javaClass.name == "com.ss.android.ugc.aweme.feed.ui.bottom.BottomSpace") {
                            child.removeInParent()
                        }
                    }
                }.onFailure {
                    XplerLog.e(it)
                }
            }
        }
    }
}