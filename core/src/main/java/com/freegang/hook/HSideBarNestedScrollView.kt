package com.freegang.hook

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.freegang.base.BaseHook
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.color.KColorUtils
import com.freegang.ktutils.view.findViewsByType
import com.freegang.ktutils.view.postRunning
import com.freegang.ui.activity.FreedomSettingActivity
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

    @OnAfter("onAttachedToWindow")
    fun onAttachedToWindowAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            thisViewGroup.postRunning {
                val onlyChild = getChildAt(0) as ViewGroup
                if (onlyChild.children.last().contentDescription == "扩展功能") return@postRunning
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
                    val intent = Intent(view.context, FreedomSettingActivity::class.java)
                    intent.putExtra("isDark", view.context.isDarkMode)
                    val options = ActivityOptions.makeCustomAnimation(
                        view.context,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    view.context.startActivity(intent, options.toBundle())
                }
                onlyChild.addView(binding.root)
            }
        }
    }
}