package io.github.fplus.core.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.freegang.extension.asOrNull
import com.freegang.extension.postRunning
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookClass
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup
import io.github.xpler.core.wrapper.CallMethods

class HSeekBarSpeedModeBottomMask : BaseHook(),
    CallMethods {
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

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (config.isImmersive) {
                thisViewGroup.postRunning {
                    it.background = ColorDrawable(Color.TRANSPARENT)
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}