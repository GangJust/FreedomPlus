package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.ktutils.app.activeActivity
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.EmptyHook
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay
import xyz.junerver.ssktx.buildSpannableString

class HDouYinSettingNewVersionActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmptyHook>(lpparam) {
    override fun setTargetClass(): Class<*> = findClass("com.ss.android.ugc.aweme.setting.ui.DouYinSettingNewVersionActivity")

    @OnAfter("onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlock(it) {
            launch {
                delay(200L)
                thisActivity.contentView.traverse { view ->
                    if (view is TextView) {
                        if ("${view.text}".contains("抖音 version")) {
                            view.buildSpannableString {
                                addText("${view.text}")
                                addText(" (Freedom+)") {
                                    onClick(useUnderLine = false) {
                                        val intent = Intent(view.context, FreedomSettingActivity::class.java)
                                        intent.putExtra("isDark", view.context.isDarkMode)
                                        val options = ActivityOptions.makeCustomAnimation(
                                            activeActivity,
                                            android.R.anim.slide_in_left,
                                            android.R.anim.slide_out_right
                                        )
                                        it.context.startActivity(intent, options.toBundle())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}