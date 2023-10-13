package com.freegang.hook

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.children
import com.freegang.base.BaseHook
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.extension.isPrimitiveObjectType
import com.freegang.ktutils.view.postRunning
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.xpler.R
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.databinding.PopupFreedomSettingBinding
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HCornerExtensionsPopupWindow(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HCornerExtendsionsPopupWindow"
    }

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.cornerExtensionsPopupWindowClazz ?: NoneHook::class.java
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (argsOrEmpty.size != 1) return
            if (args.first()?.javaClass?.isPrimitiveObjectType == false) return
            if (args.first() == false) return //fist type is Boolean
            //KLogCat.d(TAG, "更新方法: $method")

            val popupWindow = thisObject as PopupWindow
            popupWindow.contentView.postRunning {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val binding = PopupFreedomSettingBinding.inflate(inflater)
                binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                binding.freedomSettingIcon.setImageResource(R.drawable.ic_freedom)
                binding.freedomSettingContainer.setOnClickListener { view ->
                    val intent = Intent(view.context, FreedomSettingActivity::class.java)
                    intent.putExtra("isDark", view.context.isDarkMode)
                    val options = ActivityOptions.makeCustomAnimation(
                        view.context,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    view.context.startActivity(intent, options.toBundle())
                }

                val viewGroup = this as ViewGroup
                val last = viewGroup.children.last { it is ViewGroup } as ViewGroup  //RoundedLinearLayout
                binding.freedomSettingContainer.background = last.children.first().background

                last.addView(binding.root)
            }
        }
    }
}