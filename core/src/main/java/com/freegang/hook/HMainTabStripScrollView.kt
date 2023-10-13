package com.freegang.hook

import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.findParentExact
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.CallConstructors
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HMainTabStripScrollView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<MainTabStripScrollView>(lpparam), CallConstructors, CallMethods {
    companion object {
        const val TAG = "HMainTabStripScrollView"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            //关键字隐藏
            if (!config.isHideTab) return

            thisViewGroup.postRunning {
                val hideTabKeywords = config.hideTabKeywords
                    .removePrefix(",").removePrefix("，")
                    .removeSuffix(",").removeSuffix("，")
                    .replace("\\s".toRegex(), "")
                    .replace("[,，]".toRegex(), "|")
                    .toRegex()
                traverse {
                    if ("$contentDescription".contains(hideTabKeywords)) {
                        isVisible = false
                        //child.removeInParent()
                    }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            //透明度
            if (!config.isTranslucent) return
            thisViewGroup.findParentExact(MainTitleBar::class.java)?.alpha = 0.5f
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}