package com.freegang.douyin

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.callMethod
import com.freegang.xpler.core.findFieldByType
import com.freegang.xpler.core.getModuleDrawable
import com.freegang.xpler.databinding.HookBottomViewBinding
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.kiwi.model.QModel
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HLongPressPanel(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam) {
    private val config: Config get() = Config.get()

    override fun setTargetClass(): Class<*> = DouYinMain.longPressPanel ?: NoneHook::class.java

    @OnAfter("onViewCreated")
    fun onViewCreated(param: XC_MethodHook.MethodHookParam, view: View?, bundle: Bundle?) {
        hookBlock(param) {
            if (!config.isNeat) return
            if (view == null) return

            //视图替换
            val parent = view.parent as ViewGroup
            val bottomView = KtXposedHelpers.inflateView<View>(view.context, R.layout.hook_bottom_view)
            val indexOfChild = parent.indexOfChild(view)
            parent.removeView(view)
            parent.addView(bottomView, indexOfChild)

            //初始化
            val binding = HookBottomViewBinding.bind(bottomView)
            binding.neatItem.background = view.context.getModuleDrawable(R.drawable.dialog_background)
            binding.downloadItem.background = view.context.getModuleDrawable(R.drawable.dialog_background)
            binding.awemeItem.background = view.context.getModuleDrawable(R.drawable.dialog_background)
            binding.neatItemText.text = if (!config.neatState) "清爽模式" else "普通模式"

            val dialog = thisObject.callMethod<Dialog>("getDialog")

            //点击事件
            //see at: HLazyFragmentPagerAdapter.kt
            binding.neatItem.setOnClickListener {
                config.neatState = !config.neatState
                Toast.makeText(it.context, if (config.neatState) "清爽模式" else "普通模式", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()

                if (!config.isNeatHint) return@setOnClickListener
                showMessageDialog(
                    context = it.context,
                    title = "提示",
                    content = "将隐藏/显示首页大部分控件, 下个视频生效, 若部分控件未隐藏/显示请尝试重启抖音!",
                    cancel = "不再提示",
                    confirm = "确定",
                    onCancel = {
                        config.isNeatHint = false
                        config.save(it.context)
                    },
                )
            }
            binding.downloadItem.setOnClickListener {
                val qmodelFields = thisObject.findFieldByType(QModel::class.java, true)
                for (item in qmodelFields) {
                    val qmodel = item.get(thisObject) ?: continue
                    val awemes = qmodel.findFieldByType(Aweme::class.java)
                    if (awemes.isEmpty()) continue
                    val aweme = awemes.first().get(qmodel) as? Aweme ?: continue
                    DownloadLogic(this@HLongPressPanel, it.context, aweme)
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            binding.awemeItem.setOnClickListener {
                parent.removeView(bottomView)
                parent.addView(view, indexOfChild)
            }
        }
    }
}