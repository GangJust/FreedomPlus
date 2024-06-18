package io.github.fplus.core.hook

import android.widget.TextView
import com.freegang.extension.asOrNull
import com.ss.android.ugc.aweme.emoji.base.BaseEmoji
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.hook.logic.SaveEmojiLogic
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.ReturnType
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HEmojiPopupWindow : BaseHook() {
    companion object {
        const val TAG = "HEmojiPopupWindow"
    }

    private val config get() = ConfigV1.get()

    private var popUrlList: List<String> = emptyList()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.emojiPopupWindowClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun emojiAfter(
        params: XC_MethodHook.MethodHookParam,
        emoji: BaseEmoji?,
    ) {
        hookBlockRunning(params) {
            if (!config.isEmojiDownload)
                return

            popUrlList = emoji?.detailEmoji?.animateUrl?.urlList
                ?: emoji?.detailEmoji?.staticUrl?.urlList
                        ?: emptyList()
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter
    @ReturnType("Lcom/bytedance/ies/dmt/ui/widget/DmtTextView;")
    fun textViewAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isEmojiDownload)
                return

            val view = result?.asOrNull<TextView>() ?: return
            if (view.text == "添加到表情") {
                view.text = "添加到表情 (长按保存)"
                view.isLongClickable = true
                view.isHapticFeedbackEnabled = false
                view.setOnLongClickListener {
                    SaveEmojiLogic(this@HEmojiPopupWindow, it.context, popUrlList)
                    true
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}