package io.github.fplus.core.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.freegang.extension.forEachChild
import com.freegang.extension.parentView
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup

class HMainBottomTabView : BaseHook() {
    companion object {
        const val TAG = "HMainBottomTabView"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.mainBottomTabViewClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun methodAfter(params: XC_MethodHook.MethodHookParam, i: Int) {
        hookBlockRunning(params) {
            if (method.name.contains(Regex("Background|Alpha|Enabled|Visibilty"))) {
                return
            }

            // 底部导航栏透明度
            if (config.isTranslucent) {
                val alphaValue = config.translucentValue[3] / 100f
                thisViewGroup.parentView?.alpha = alphaValue
            }

            // 底部导航栏全局沉浸式
            if (config.isImmersive) {
                thisViewGroup.parentView?.background = ColorDrawable(Color.TRANSPARENT)
                thisViewGroup.forEachChild {
                    it.background = ColorDrawable(Color.TRANSPARENT)
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}