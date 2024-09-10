package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.widget.TextView
import com.bytedance.im.core.model.Message
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.findMethodInvoke
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HChatListRecalledHint : BaseHook() {
    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.chatListRecalledHintClazz ?: NoneHook::class.java
    }

    @SuppressLint("SetTextI18n")
    @OnAfter
    fun methodAfter(
        params: MethodParam,
        @Param any: Any?,
        i: Int,
        list: List<*>?,
    ) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            if (!config.preventRecalledOtherSetting.getOrElse(0) { false }) {
                val message = thisObject!!.findMethodInvoke<Any> {
                    returnType(Message::class.java, true)
                    predicate { it.parameterTypes.isEmpty() }
                }
                val isSelf = message?.findMethodInvoke<Boolean> { name("isSelf") }
                if (isSelf == true)
                    return
            }

            val textView = thisObject!!.findFieldGetValue<TextView> {
                type(TextView::class.java)
            }
            textView?.text = "${textView?.text?.removeSuffix(" (没收到)")} (没收到)"
        }.onFailure {
            XplerLog.e(it)
        }
    }
}