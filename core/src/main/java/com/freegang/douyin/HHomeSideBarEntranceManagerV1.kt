package com.freegang.douyin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.freegang.base.BaseHook
import com.freegang.douyin.activity.FreedomSettingActivity
import com.freegang.ktutils.app.contentView
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

    @SuppressLint("ResourceAsColor")
    @OnAfter("onClick")
    fun onClick(param: XC_MethodHook.MethodHookParam, view: View) {
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
                val text = sideRootView.findViewsByType(TextView::class.java).firstOrNull() ?: return@launch
                val isNight = KColorUtils.isLightColor(text.currentTextColor)

                val setting = KtXposedHelpers.inflateView<ViewGroup>(v.context, R.layout.side_freedom_setting)
                val binding = SideFreedomSettingBinding.bind(setting)

                val backRes: Int
                val iconRes: Int
                val dividerColorRes: Int
                val textColorRes: Int
                if (isNight) {
                    backRes = R.drawable.dialog_background_night
                    iconRes = R.drawable.ic_freedom_night
                    dividerColorRes = Color.parseColor("#14FFFFFF")
                    textColorRes = Color.parseColor("#E6FFFFFF")
                } else {
                    backRes = R.drawable.dialog_background
                    iconRes = R.drawable.ic_freedom
                    dividerColorRes = Color.parseColor("#1F161823")
                    textColorRes = Color.parseColor("#FF161823")
                }

                binding.freedomSettingContainer.background = KtXposedHelpers.getDrawable(backRes)
                binding.freedomSettingText.setTextColor(textColorRes)
                binding.freedomSettingDivider.setBackgroundColor(dividerColorRes)
                binding.freedomSettingIcon.background = KtXposedHelpers.getDrawable(iconRes)
                binding.freedomSettingTitle.text = "Freedom+"
                binding.freedomSettingTitle.setTextColor(textColorRes)
                binding.freedomSetting.setOnClickListener {
                    val intent = Intent(it.context, FreedomSettingActivity::class.java)
                    it.context.startActivity(intent)
                }
                sideRootView.addView(binding.root)
            }
        }
    }
}