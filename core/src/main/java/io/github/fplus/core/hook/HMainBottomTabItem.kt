package io.github.fplus.core.hook

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.forEachChild
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisView


class HMainBottomTabItem : BaseHook<Any>() {
    companion object {
        const val TAG = "HMainBottomTabItem"
    }

    val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.mainBottomTabItemClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun currentIndexAfter(params: XC_MethodHook.MethodHookParam, int: Int) {
        hookBlockRunning(params) {
            isHidePhotoButton(thisView)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun isHidePhotoButton(view: View) {
        if (!config.isHidePhotoButton) {
            return
        }

        view.forEachChild {
            if ("$contentDescription".contains(Regex("拍摄|道具"))) {
                // 隐藏按钮
                if (config.photoButtonType == 2) {
                    view.isVisible = false
                    return@forEachChild
                }

                // 占位按钮, 移除加号图标
                if (this is ImageView) {
                    setImageDrawable(null)
                    background = null
                    foreground = null
                }

                // 允许拍摄直接结束逻辑
                if (config.photoButtonType == 0) {
                    view.isVisible = true
                    return@forEachChild
                }

                // 不允许拍摄
                view.setOnClickListener {
                    KToastUtils.show(it.context, "已禁止拍摄")
                }
                view.setOnLongClickListener {
                    KToastUtils.show(it.context, "已禁止拍摄")
                    true
                }
            }
        }
    }
}