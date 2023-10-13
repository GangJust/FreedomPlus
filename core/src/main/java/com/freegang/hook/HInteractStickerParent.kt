package com.freegang.hook

import android.view.ViewGroup
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisViewGroup
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HInteractStickerParent(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<InteractStickerParent>(lpparam), CallMethods {
    companion object {
        const val TAG = "HInteractStickerParent"
    }

    private val config get() = ConfigV1.get()

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (!config.isTranslucent) return
            if (method.name.contains("Alpha")) return
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
    }
}