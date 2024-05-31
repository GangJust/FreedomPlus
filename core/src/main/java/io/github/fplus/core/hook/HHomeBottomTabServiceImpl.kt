package io.github.fplus.core.hook

import android.content.Context
import android.view.View
import com.freegang.extension.getOnClickListener
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HHomeBottomTabServiceImpl : BaseHook() {
    companion object {
        const val TAG = "HHomeBottomTabServiceImpl"
    }

    private val config
        get() = ConfigV1.get()

    private var lastType: String? = "HOME"
    private var clickAgain: Boolean = false

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.homepage.tab.business.bottom.HomeBottomTabServiceImpl")
    }

    @OnBefore
    fun methodBefore(
        params: XC_MethodHook.MethodHookParam,
        context: Context?,
        type: String?,
        view: View?
    ) {
        hookBlockRunning(params) {
            if (!config.isPreventAccidentalTouch)
                return

            if (!clickAgain && lastType == "HOME" && type == "HOME") {
                val onClick = view?.getOnClickListener
                showMessageDialog(
                    context = context!!,
                    title = "温馨提示",
                    content = "你点击了首页，是否触发刷新？",
                    cancel = "取消",
                    confirm = "确定",
                    onConfirm = {
                        clickAgain = true
                        onClick?.onClick(view)
                    }
                )

                result = Void.TYPE
            }

            lastType = type
            clickAgain = false
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }
}