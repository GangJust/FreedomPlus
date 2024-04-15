package io.github.fplus.core.hook

import com.freegang.extension.firstParentOrNull
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup
import io.github.xpler.core.wrapper.CallMethods

class HMainTabStripScrollView : BaseHook<MainTabStripScrollView>(),
    CallMethods {

    companion object {
        const val TAG = "HMainTabStripScrollView"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // 透明度
            if (config.isTranslucent) {
                thisViewGroup.firstParentOrNull(MainTitleBar::class.java)?.alpha = config.translucentValue[0] / 100f
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}