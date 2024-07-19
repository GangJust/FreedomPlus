package io.github.fplus.core.hook

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.children
import com.freegang.extension.isDarkMode
import com.freegang.extension.postRunning
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.Constant
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.databinding.SideFreedomSettingBinding
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup

class HSideBarNestedScrollView : BaseHook() {
    companion object {
        const val TAG = "HSideBarNestedScrollView"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.sideBarNestedScrollViewClazz ?: NoneHook::class.java
    }

    @OnAfter("onTouchEvent")
    fun onTouchEventAfter(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    insertSettingView(thisViewGroup)
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // @OnAfter("onAttachedToWindow")
    fun onAttachedToWindowAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (thisViewGroup.context.packageName.contains("lite")) {
                thisViewGroup.postRunning { insertSettingView(it) }
            } else {
                thisViewGroup.postRunning { insertSettingView(it) }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    private fun insertSettingView(viewGroup: ViewGroup) {
        viewGroup.postRunning {
            val onlyChild = it.getChildAt(0) as ViewGroup
            if (onlyChild.children.lastOrNull()?.contentDescription == "扩展功能") return@postRunning

            val setting = KtXposedHelpers.inflateView<ViewGroup>(onlyChild.context, R.layout.side_freedom_setting)
            setting.contentDescription = "扩展功能"
            val binding = SideFreedomSettingBinding.bind(setting)

            val backgroundRes: Int
            val iconColorRes: Int
            val textColorRes: Int
            if (viewGroup.context.isDarkMode) {
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
                    if (!KAppUtils.isAppInstalled(view.context, Constant.modulePackage)) {
                        KToastUtils.show(it.context, "未安装Freedom+模块!")
                        return@setOnClickListener
                    }
                    intent.setClassName(Constant.modulePackage, "io.github.fplus.activity.MainActivity")
                    KToastUtils.show(it.context, "若设置未生效请尝试重启抖音!")
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
        }
    }
}