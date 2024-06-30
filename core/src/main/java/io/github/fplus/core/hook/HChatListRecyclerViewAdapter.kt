package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.bytedance.im.core.model.Message
import com.freegang.extension.asOrNull
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.firstOrNull
import com.freegang.extension.idName
import com.freegang.extension.findMethodInvoke
import com.freegang.extension.parentView
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HChatListRecyclerViewAdapter : BaseHook() {
    companion object {
        const val TAG = "HChatListRecyclerViewAdapter"
    }

    private val config get() = ConfigV1.get()


    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.chatListRecyclerViewAdapterClazz ?: NoneHook::class.java
    }

    @SuppressLint("SetTextI18n")
    @OnAfter("onBindViewHolder")
    fun onBindViewHolderAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            val itemView = args[0]?.findFieldGetValue<FrameLayout> { name("itemView") } ?: return


            val message = args[0]?.findMethodInvoke<Message> {
                returnType(Message::class.java, true)
                predicate {
                    it.parameterTypes.isEmpty()
                }
            }

            val ext = message?.findMethodInvoke<Map<String, String>> { name(("getExt")) } ?: return
            val isSelf = message.findMethodInvoke<Boolean> { name("isSelf") }

            val addedFlag = "RecalledHint"
            val viewGroup = itemView
                .firstOrNull(ViewGroup::class.java) { it.idName == "@id/content" }
                ?.parentView
                ?: return

            val last = viewGroup.children.last()
            if (last.tag != addedFlag) {
                val view = TextView(itemView.context)
                view.text = "已撤回"
                view.setTextColor(Color.GRAY)
                view.textSize = 12f
                view.isVisible = false
                view.gravity = Gravity.CENTER
                view.tag = addedFlag
                viewGroup.addView(view)
            }

            if (ext.containsKey("f:prevent_recalled")) {
                if (last.tag == addedFlag) {
                    last.isVisible = true
                    last.asOrNull<TextView>()?.gravity = if (isSelf == true) {
                        Gravity.START
                    } else {
                        Gravity.END
                    }
                }
            } else {
                if (last.tag == addedFlag) {
                    last.isVisible = false
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}