package io.github.fplus.core.hook

import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.findParentExact
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallMethods
import io.github.xpler.core.thisViewGroup

class HMainTabStripScrollView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<MainTabStripScrollView>(lpparam), CallMethods {
    companion object {
        const val TAG = "HMainTabStripScrollView"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // 透明度
            if (config.isTranslucent) {
                thisViewGroup.findParentExact(MainTitleBar::class.java)?.alpha = config.translucentValue[0] / 100f
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}