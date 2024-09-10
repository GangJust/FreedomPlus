package io.github.fplus.core.hook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.freegang.extension.asOrNull
import com.freegang.extension.postRunning
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.CallMethods
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookClass
import io.github.xpler.core.lparam
import io.github.xpler.core.proxy.MethodParam
import io.github.xpler.core.thisViewGroup

class HSeekBarSpeedModeBottomMask : BaseHook(), CallMethods {
    private val config: ConfigV1 get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.ui.seekbar.ui.SeekBarSpeedModeBottomMask")
    }

    init {
        DexkitBuilder.seekBarSpeedModeBottomContainerClazz?.runCatching {
            lparam.hookClass(this)
                .method("getMBottomLayout") {
                    onAfter {
                        if (config.isImmersive) {
                            result?.asOrNull<View>()?.background = ColorDrawable(Color.TRANSPARENT)
                        }
                    }
                }
        }
    }

    override fun callOnBeforeMethods(params: MethodParam) {

    }

    override fun callOnAfterMethods(params: MethodParam) {
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