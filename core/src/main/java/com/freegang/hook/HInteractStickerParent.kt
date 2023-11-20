package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.removeInParent
import com.freegang.xpler.core.interfaces.CallConstructors
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HInteractStickerParent(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<InteractStickerParent>(lpparam), CallConstructors {
    companion object {
        const val TAG = "HInteractStickerParent"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            // 移除悬浮贴纸
            if (config.isRemoveSticker) {
                thisViewGroup.postRunning {
                    removeInParent()
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}