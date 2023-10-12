package com.freegang.hook

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import com.ss.android.ugc.aweme.main.MainFragment
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

@Deprecated("淘汰区域，删除倒计时中")
class HMainFragment(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainFragment>(lpparam) {
    companion object {
        const val TAG = "HMainFragment"
    }

    private val config get() = ConfigV1.get()

    @OnAfter("onViewCreated")
    fun onViewCreated(param: XC_MethodHook.MethodHookParam, view: View?, savedInstanceState: Bundle?) {
        hookBlockRunning(param) {
            if (config.isTranslucent) {
                val mCommonTitleBar = thisObject.fieldGetFirst("mCommonTitleBar")?.asOrNull<View>()
                mCommonTitleBar?.alpha = 0.5f
            }

            if (config.isHideTab) {
                val viewGroup = view as ViewGroup
                changeTabItem(viewGroup)
            }
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    private fun changeTabItem(viewGroup: ViewGroup) {
        val hideTabKeywords = config.hideTabKeywords
            .removePrefix(",").removePrefix("，")
            .removeSuffix(",").removeSuffix("，")
            .replace("\\s".toRegex(), "")
            .replace("[,，]".toRegex(), "|")
            .toRegex()
        viewGroup.traverse {
            if (this is MainTabStripScrollView) {
                traverse {
                    if ("$contentDescription".contains(hideTabKeywords)) {
                        isVisible = false
                    }
                }
            }
        }
    }
}