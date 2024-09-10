package io.github.fplus.core.hook

import com.freegang.extension.firstParentOrNull
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.CallMethods
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.core.thisViewGroup

class HMainTabStripScrollView : BaseHook(), CallMethods {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return MainTabStripScrollView::class.java
    }

    override fun callOnBeforeMethods(params: MethodParam) {}

    override fun callOnAfterMethods(params: MethodParam) {
        hookBlockRunning(params) {
            if (!config.isTranslucent)
                return

            thisViewGroup.firstParentOrNull(MainTitleBar::class.java)?.alpha = config.translucentValue[0] / 100f
        }.onFailure {
            XplerLog.e(it)
        }
    }
}