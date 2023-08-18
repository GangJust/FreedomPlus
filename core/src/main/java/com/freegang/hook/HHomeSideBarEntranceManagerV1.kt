package com.freegang.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.color.KColorUtils
import com.freegang.ktutils.view.findViewsByType
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.callMethod
import com.freegang.xpler.core.findFieldByType
import com.freegang.xpler.databinding.SideFreedomSettingBinding
import com.ss.android.ugc.aweme.sidebar.SideBarNestedScrollView
import com.ss.android.ugc.aweme.sidebar.entrance.HomeSideBarEntranceManagerV1
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HHomeSideBarEntranceManagerV1(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<HomeSideBarEntranceManagerV1>(lpparam) {
    companion object {
        var isAdded = false
    }

    private val config get() = ConfigV1.get()

    @SuppressLint("ResourceAsColor")
    @OnAfter("onClick")
    fun onClick(param: XC_MethodHook.MethodHookParam, view: View) {
        if (config.isDisablePlugin) return //去插件化
        if (isAdded) return
        isAdded = true
        hookBlock(param) {
            launch {
                delay(200)
                val fragmentField = thisObject.findFieldByType(Fragment::class.java).firstOrNull() ?: return@launch
                val fragment = fragmentField.get(thisObject) ?: return@launch
                val activity = fragment.callMethod<Activity>("getActivity") ?: return@launch
                val contentView = activity.contentView
                val v = contentView.findViewsByType(SideBarNestedScrollView::class.java).firstOrNull() ?: return@launch

                val sideRootView = v.children.first() as ViewGroup
                if (sideRootView.children.last().contentDescription == "扩展功能") return@launch

                val text = sideRootView.findViewsByType(TextView::class.java).firstOrNull() ?: return@launch
                val isDark = KColorUtils.isDarkColor(text.currentTextColor)

                val setting = KtXposedHelpers.inflateView<ViewGroup>(v.context, R.layout.side_freedom_setting)
                setting.contentDescription = "扩展功能"
                val binding = SideFreedomSettingBinding.bind(setting)

                val backgroundRes: Int
                val iconColorRes: Int
                val dividerColorRes: Int
                val textColorRes: Int
                if (!isDark) {
                    backgroundRes = R.drawable.dialog_background_night
                    iconColorRes = R.drawable.ic_freedom_night
                    dividerColorRes = Color.parseColor("#14FFFFFF")
                    textColorRes = Color.parseColor("#E6FFFFFF")
                } else {
                    backgroundRes = R.drawable.dialog_background
                    iconColorRes = R.drawable.ic_freedom
                    dividerColorRes = Color.parseColor("#1F161823")
                    textColorRes = Color.parseColor("#FF161823")
                }

                binding.freedomSettingContainer.background = KtXposedHelpers.getDrawable(backgroundRes)
                binding.freedomSettingText.setTextColor(textColorRes)
                binding.freedomSettingDivider.setBackgroundColor(dividerColorRes)
                binding.freedomSettingIcon.background = KtXposedHelpers.getDrawable(iconColorRes)
                binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                binding.freedomSettingTitle.setTextColor(textColorRes)
                binding.freedomSetting.setOnClickListener { view ->
                    val intent = Intent(view.context, FreedomSettingActivity::class.java)
                    intent.putExtra("isDark", view.context.isDarkMode)
                    val options = ActivityOptions.makeCustomAnimation(
                        activity,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    activity.startActivity(intent, options.toBundle())
                }
                sideRootView.addView(binding.root)
            }
        }
    }
}