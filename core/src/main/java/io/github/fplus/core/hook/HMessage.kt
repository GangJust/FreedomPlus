package io.github.fplus.core.hook

import com.bytedance.im.core.model.Message
import com.freegang.extension.findMethodInvoke
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HMessage : BaseHook() {
    companion object {
        const val TAG = "HMessage"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return Message::class.java
    }

    @OnAfter("isRecalled")
    fun isRecalledAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            if (!config.preventRecalledOtherSetting.getOrElse(0) { false }) {
                val isSelf = thisObject.findMethodInvoke<Boolean> { name("isSelf") }
                if (isSelf == true) return
            }

            val content = "${thisObject.findMethodInvoke<Any> { name("getContent") }}"
            if (content == "{\"aweType\":0,\"text\":\"Recall Content Hided\"}") {
                return
            }

            val ext = thisObject.findMethodInvoke<MutableMap<String, String>> {
                name("getExt")
                returnType(java.util.Map::class.java)
            } ?: return
            if (ext.containsKey("s:is_recalled")) {
                ext.remove("s:is_recalled")
                ext.put("f:prevent_recalled", "true")
                thisObject.findMethodInvoke<Any>(ext) { name("setExt") }

                result = false
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}