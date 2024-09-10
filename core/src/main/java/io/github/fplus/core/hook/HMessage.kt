package io.github.fplus.core.hook

import com.bytedance.im.core.model.Message
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HMessage : BaseHook() {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return Message::class.java
    }

    @OnAfter("isRecalled")
    fun isRecalledAfter(params: MethodParam) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            val message = thisObject as Message

            if (!config.preventRecalledOtherSetting.getOrElse(0) { false }) {
                if (message.isSelf) {
                    return
                }
            }

            if (message.content == "{\"aweType\":0,\"text\":\"Recall Content Hided\"}") {
                return
            }

            if (message.ext.containsKey("s:is_recalled")) {
                message.ext.remove("s:is_recalled")
                message.ext.put("f:prevent_recalled", "true")
                setResult(false)
            }

        }.onFailure {
            XplerLog.e(it)
        }
    }
}