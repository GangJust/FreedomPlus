package io.github.fplus.core.hook

import androidx.core.view.children
import androidx.core.view.isVisible
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup

class HBottomCtrlBar : BaseHook() {
    companion object {
        const val TAG = "HBottomCtrlBar"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.bottomCtrlBarClazz ?: NoneHook::class.java
    }

    @OnAfter
    @ReturnType(name = "boolean")
    fun isAddedAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // if (!config.isRemoveBottomCtrlBar) {
            //     return
            // }

            // thisView.isVisible = false
            thisViewGroup.children.forEach { it.isVisible = !config.isRemoveBottomCtrlBar }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}