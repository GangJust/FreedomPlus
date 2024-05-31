package io.github.fplus.core.hook

import com.freegang.extension.postRunning
import com.freegang.extension.removeInParent
import com.ss.android.ugc.aweme.sticker.infoSticker.interact.consume.view.InteractStickerParent
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisViewGroup
import io.github.xpler.core.wrapper.CallConstructors

class HInteractStickerParent : BaseHook(), CallConstructors {
    companion object {
        const val TAG = "HInteractStickerParent"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return InteractStickerParent::class.java
    }

    override fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isRemoveSticker)
                return

            thisViewGroup.postRunning { child ->
                child.removeInParent()
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}