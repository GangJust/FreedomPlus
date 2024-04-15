package io.github.fplus.core.hook

import android.widget.TextView
import com.bytedance.im.core.model.Message
import com.freegang.extension.asOrNull
import com.freegang.extension.fieldGet
import com.freegang.extension.methodInvoke
import com.freegang.extension.methods
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.Param
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HChatListRecalledHint : BaseHook<Any>() {
    companion object {
        const val TAG = "HChatListRecalledHint"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.chatListRecalledHintClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun testAfter(
        params: XC_MethodHook.MethodHookParam,
        @Param("null") any: Any?,
        i: Int,
        list: List<*>?,
    ) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            if (!config.preventRecalledOtherSetting.getOrElse(0) { false }) {
                val messages = thisObject.methods(returnType = Message::class.java)
                    .filter { it.parameterTypes.isEmpty() }
                    .map { it.invoke(thisObject) }

                val message = messages.firstOrNull() ?: return
                val isSelf = message.methodInvoke("isSelf")
                if (isSelf == true) return
            }

            thisObject
                .fieldGet(type = TextView::class.java)
                ?.asOrNull<TextView>()
                ?.apply {
                    text = "${text.removeSuffix(" (没收到)")} (没收到)"
                }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}