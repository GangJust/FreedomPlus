package io.github.fplus.core.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import com.freegang.extension.isDarkMode
import com.freegang.extension.postRunning
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import io.github.fplus.Constant
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.databinding.PopupFreedomSettingBinding
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.fplus.plugin.injectRes
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HCornerExtensionsPopupWindow : BaseHook() {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.cornerExtensionsPopupWindowClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun methodAfter(params: MethodParam, boolean: Boolean) {
        hookBlockRunning(params) {
            if (!boolean)
                return

            val popupWindow = thisObject as PopupWindow
            popupWindow.contentView.postRunning {
                injectRes(it.context.resources)
                val icFreedom = AppCompatResources.getDrawable(it.context, R.drawable.ic_freedom)
                val binding = PopupFreedomSettingBinding.inflate(LayoutInflater.from(it.context))
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