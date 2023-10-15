package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.traverse
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.xpler.core.EmptyHook
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import xyz.junerver.ssktx.buildSpannableString

@Deprecated("淘汰区域，删除倒计时中")
class HDouYinSettingNewVersionActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmptyHook>(lpparam) {
    companion object {
        const val TAG = "HDouYinSettingNewVersionActivity"
    }

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.setting.ui.DouYinSettingNewVersionActivity")
    }

    private val config get() = ConfigV1.get()

    @OnAfter("onCreate")
    fun onCreate(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        // 去插件化
        if (config.isDisablePlugin) return
        hookBlockRunning(params) {
            thisActivity.contentView.postRunning {
                traverse {
                    if (this is TextView) {
                        if ("$text".contains("抖音 version")) {
                            buildSpannableString {
                                addText("$text")
                                addText(" (Freedom+)") {
                                    onClick(useUnderLine = false) { v ->
                                        val intent = Intent(v.context, FreedomSettingActivity::class.java)
                                        intent.putExtra("isDark", context.isDarkMode)
                                        val options = ActivityOptions.makeCustomAnimation(
                                            thisActivity,
                                            android.R.anim.slide_in_left,
                                            android.R.anim.slide_out_right
                                        )
                                        v.context.startActivity(intent, options.toBundle())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}