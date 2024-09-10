package io.github.fplus.core.hook

import com.freegang.extension.forEachChild
import com.freegang.extension.postRunning
import com.freegang.extension.removeInParent
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.CallConstructors
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookClass
import io.github.xpler.core.lparam
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.core.thisViewGroup

class HDisallowInterceptRelativeLayout : BaseHook(), CallConstructors {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.DisallowInterceptRelativeLayout")
    }

    override fun onInit() {
        // 旧版本
        lparam.hookClass("com.ss.android.ugc.aweme.feed.ui.DisallowInterceptRelativeLayout2")
            .constructorAll {
                onAfter {
                    callOnAfterConstructors(this)
                }
                onUnhook {
                    unhook()
                }
            }
    }

    override fun callOnBeforeConstructors(params: MethodParam) {
    }

    override fun callOnAfterConstructors(params: MethodParam) {
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