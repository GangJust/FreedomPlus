package com.freegang.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.postRunning
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.thisViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HSeekBarSpeedModeBottomMask(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HSeekBarSpeedModeBottomMask"
    }

    private val config: ConfigV1 get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.seekbar.ui.SeekBarSpeedModeBottomMask")
    }

    init {
        DexkitBuilder.seekBarSpeedModeBottomContainerClazz?.runCatching {
            lpparam.hookClass(this)
                .method("getMBottomLayout") {
                    onAfter {
                        if (config.isImmersive) {
                            result.asOrNull<View>()?.background = ColorDrawable(Color.TRANSPARENT)
                        }
                    }
                }
        }
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (config.isImmersive) {
                thisViewGroup.postRunning {
                    background = ColorDrawable(Color.TRANSPARENT)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}