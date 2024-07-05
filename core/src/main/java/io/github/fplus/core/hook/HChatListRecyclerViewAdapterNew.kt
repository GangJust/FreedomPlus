package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.bytedance.im.core.model.Message
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.firstOrNull
import com.freegang.extension.idName
import com.freegang.extension.parentView
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HChatListRecyclerViewAdapterNew : BaseHook() {
    companion object {
        const val TAG = "HChatListRecyclerViewAdapter"
    }

    private val recallMsgId = View.generateViewId()

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.chatListRecyclerViewAdapterNewClazz ?: NoneHook::class.java
    }

    @OnAfter("onCreateViewHolder")
    fun onCreateViewHolderAfter(
        params: XC_MethodHook.MethodHookParam,
        parent: ViewGroup,
        viewType: Int,
    ) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            val vh = result
            val itemView = vh.findFieldGetValue<ViewGroup> { name("itemView") }
            val contentContainer = itemView
                ?.firstOrNull(ViewGroup::class.java) { it.idName == "@id/content" }
                ?.parentView

            contentContainer?.run {
                val textView = TextView(context)
                textView.id = recallMsgId
                textView.setTextColor(Color.GRAY)
                textView.textSize = 10f
                textView.isVisible = false
                textView.gravity = Gravity.CENTER
                addView(textView)
            }
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    @SuppressLint("SetTextI18n")
    @OnAfter("onBindViewHolder")
    fun onBindViewHolderAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isPreventRecalled) {
                return
            }

            val vh = args[0] // ViewHolder
            val itemView = args[0]?.findFieldGetValue<FrameLayout> { name("itemView") } ?: return
            val recallMsg = itemView.findViewById<TextView>(recallMsgId) ?: return

            val message = vh.findFieldGetValue<Message> { type(Message::class.java, true) }
                ?: throw NullPointerException("message is null.")

            if (message.ext.containsKey("f:prevent_recalled")) {
                recallMsg.gravity = if (message.isSelf) Gravity.START else Gravity.END
                recallMsg.isVisible = true
                recallMsg.text = "已撤回"
            } else {
                recallMsg.isVisible = false
                recallMsg.text = ""
            }

        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }
}