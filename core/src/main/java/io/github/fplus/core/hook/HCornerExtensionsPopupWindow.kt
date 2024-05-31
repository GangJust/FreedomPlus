package io.github.fplus.core.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
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
import io.github.fplus.core.databinding.PopupFreedomSettingBinding
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HCornerExtensionsPopupWindow : BaseHook() {
    companion object {
        const val TAG = "HCornerExtensionsPopupWindow"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.cornerExtensionsPopupWindowClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun methodAfter(params: XC_MethodHook.MethodHookParam, boolean: Boolean) {
        hookBlockRunning(params) {
            if (!boolean)
                return

            val popupWindow = thisObject as PopupWindow
            popupWindow.contentView.postRunning {
                val inflateView = KtXposedHelpers.inflateView<View>(it.context, R.layout.popup_freedom_setting)
                val icFreedom = KtXposedHelpers.getDrawable(R.drawable.ic_freedom)
                val binding = PopupFreedomSettingBinding.bind(inflateView)
                binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                binding.freedomSettingIcon.setImageDrawable(icFreedom)
                binding.freedomSettingContainer.setOnClickListener { view ->
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

                val viewGroup = it as ViewGroup
                val last = viewGroup.children.last { child -> child is ViewGroup } as ViewGroup  // RoundedLinearLayout
                binding.freedomSettingContainer.background = last.children.first().background
                binding.root.contentDescription = "扩展功能"

                if (last.children.last().contentDescription != "扩展功能") {
                    last.addView(binding.root)
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}