package com.freegang.hook

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.CallConstructors
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HInteractStickerParent(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<InteractStickerParent>(lpparam), CallConstructors, CallMethods {
    companion object {
        const val TAG = "HInteractStickerParent"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            // 移除悬浮贴纸
            thisViewGroup.postRunning { isVisible = !config.isRemoveSticker }
        }
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            // 半透明
            if (config.isTranslucent && !method.name.contains("Alpha")) {
                thisViewGroup.postRunning {
                    traverse {
                        runCatching {
                            if (this !is ViewGroup) {
                                alpha = 0.5f
                            }
                        }.onFailure {
                            KLogCat.tagE(TAG, it)
                        }
                    }
                }
            }

            // 移除悬浮贴纸
            if (!method.name.contains("setVisibility")) {
                thisViewGroup.postRunning { isVisible = !config.isRemoveSticker }
            }
        }
    }
}