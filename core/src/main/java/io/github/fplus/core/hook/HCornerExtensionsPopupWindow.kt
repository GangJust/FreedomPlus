package io.github.fplus.core.hook

import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.children
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.extension.isPrimitiveObjectType
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.databinding.PopupFreedomSettingBinding
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.HookConfig
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.NoneHook
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.interfaces.CallMethods

class HCornerExtensionsPopupWindow(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HCornerExtendsionsPopupWindow"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.cornerExtensionsPopupWindowClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (argsOrEmpty.size != 1) return
            if (args.first()?.javaClass?.isPrimitiveObjectType == false) return
            if (args.first() == false) return // fist type is Boolean
            // KLogCat.d(TAG, "更新方法: $method")

            val popupWindow = thisObject as PopupWindow
            popupWindow.contentView.postRunning {
                val inflateView = KtXposedHelpers.inflateView<View>(context, R.layout.popup_freedom_setting)
                val icFreedom = KtXposedHelpers.getDrawable(R.drawable.ic_freedom)
                val binding = PopupFreedomSettingBinding.bind(inflateView)
                binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                binding.freedomSettingIcon.setImageDrawable(icFreedom)
                binding.freedomSettingContainer.setOnClickListener { view ->
                    val intent = Intent()
                    if (config.isDisablePlugin) {
                        if (!KAppUtils.isAppInstalled(view.context, HookConfig.modulePackageName)) {
                            KToastUtils.show(context, "未安装Freedom+模块!")
                            return@setOnClickListener
                        }
                        intent.setClassName(HookConfig.modulePackageName, "io.github.fplus.activity.MainActivity")
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

                val viewGroup = this as ViewGroup
                val last = viewGroup.children.last { it is ViewGroup } as ViewGroup  // RoundedLinearLayout
                binding.freedomSettingContainer.background = last.children.first().background
                binding.root.contentDescription = "扩展功能"

                if (last.children.last().contentDescription != "扩展功能") {
                    last.addView(binding.root)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}