package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.findParentExact
import com.freegang.xpler.core.interfaces.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

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