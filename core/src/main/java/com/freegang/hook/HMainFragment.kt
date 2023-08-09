package com.freegang.hook

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.view.findViewsByDesc
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.FieldGet
import com.freegang.xpler.core.KtCallMethods
import com.freegang.xpler.core.OnAfter
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import com.ss.android.ugc.aweme.main.MainFragment
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HMainFragment(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainFragment>(lpparam), KtCallMethods {
    private val config get() = ConfigV1.get()

    @FieldGet("mCommonTitleBar")
    val mCommonTitleBar: View? = null

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlock(param) {
            if (!config.isTranslucent) return
            mCommonTitleBar?.alpha = 0.5f
        }
    }

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {}

    @OnAfter("onViewCreated")
    fun onViewCreated(param: XC_MethodHook.MethodHookParam, view: View?, savedInstanceState: Bundle?) {
        if (!config.isHideTab) return
        val viewGroup = view as ViewGroup
        changeTabItem(viewGroup)
    }

    private fun changeTabItem(viewGroup: ViewGroup) {
        viewGroup.traverse {
            if (it is MainTabStripScrollView) {
                val hideTabKeywords = config.hideTabKeywords
                    .removePrefix(",").removePrefix("，")
                    .removeSuffix(",").removeSuffix("，")
                    .replace("\\s".toRegex(), "")
                    .replace("[,，]".toRegex(), "|")
                it.findViewsByDesc(View::class.java, hideTabKeywords.toRegex()).forEach { v -> v.isVisible = false }
            }
        }
    }
}