package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.app.navigationBarHeight
import com.freegang.ktutils.color.KColorUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.findViewsByType
import com.freegang.ktutils.view.postRunning
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.xpler.HookPackages
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.freegang.xpler.databinding.SideFreedomSettingBinding
import com.ss.android.ugc.aweme.sidebar.SideBarNestedScrollView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HSideBarNestedScrollView(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<SideBarNestedScrollView>(lpparam) {
    companion object {
        const val TAG = "HSideBarNestedScrollView"
    }

    private val config get() = ConfigV1.get()

    @OnAfter("onAttachedToWindow")
    fun onAttachedToWindowAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            thisViewGroup.postRunning {
                val onlyChild = getChildAt(0) as ViewGroup
                if (onlyChild.children.lastOrNull()?.contentDescription == "扩展功能") return@postRunning
                val text = onlyChild.findViewsByType(TextView::class.java).firstOrNull() ?: return@postRunning
                val isDark = KColorUtils.isDarkColor(text.currentTextColor)

                val setting = KtXposedHelpers.inflateView<ViewGroup>(onlyChild.context, R.layout.side_freedom_setting)
                setting.contentDescription = "扩展功能"
                val binding = SideFreedomSettingBinding.bind(setting)

                val backgroundRes: Int
                val iconColorRes: Int
                val textColorRes: Int
                if (!isDark) {
                    backgroundRes = R.drawable.side_item_background_night
                    iconColorRes = R.drawable.ic_freedom_night
                    textColorRes = Color.parseColor("#E6FFFFFF")
                } else {
                    backgroundRes = R.drawable.dialog_background
                    iconColorRes = R.drawable.ic_freedom
                    textColorRes = Color.parseColor("#FF161823")
                }

                binding.freedomSettingContainer.background = KtXposedHelpers.getDrawable(backgroundRes)
                binding.freedomSettingText.setTextColor(textColorRes)
                binding.freedomSettingIcon.background = KtXposedHelpers.getDrawable(iconColorRes)
                binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                binding.freedomSettingTitle.setTextColor(textColorRes)
                binding.freedomSetting.setOnClickListener { view ->
                    val intent = Intent()
                    if (config.isDisablePlugin) {
                        if (!KAppUtils.isAppInstalled(view.context, HookPackages.modulePackageName)) {
                            KToastUtils.show(context, "未安装Freedom+模块!")
                            return@setOnClickListener
                        }
                        intent.setClassName(HookPackages.modulePackageName, "com.freegang.fplus.activity.MainActivity")
                        KToastUtils.show(context, "若设置未生效请尝试重启抖音!")
                    } else {
                        intent.setClass(view.context, FreedomSettingActivity::class.java)
                    }

                    intent.putExtra("isDark", view.context.isDarkMode)
                    val options = ActivityOptions.makeCustomAnimation(
                        view.context,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    view.context.startActivity(intent, options.toBundle())
                }
                onlyChild.addView(binding.root)

                if (config.isImmersive) {
                    // 全面屏手势沉浸式底部垫高 (首页侧滑)，底部导航栏则不处理
                    if (HDisallowInterceptRelativeLayout.isEdgeToEdgeEnabled) {
                        onlyChild.updatePadding(bottom = context.navigationBarHeight)
                    }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}