package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import com.freegang.extension.contentView
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.firstOrNull
import com.freegang.extension.postDelayedRunning
import com.ss.android.ugc.aweme.base.model.UrlModel
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.SaveEmojiLogic
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookClass

class HEmojiDetailDialogNew : BaseHook() {
    companion object {
        const val TAG = "HEmojiDetailDialogNew"
    }

    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()

    override fun setTargetClass(): Class<*> {
        return EmojiDetailDialogNew::class.java
    }

    @SuppressLint("SetTextI18n")
    override fun onInit() {

        lpparam.hookClass(EmojiDetailDialogNew::class.java)
            .constructorAll {
                onBefore {
                    if (argsOrEmpty.isEmpty())
                        return@onBefore

                    val url = args[0].findFieldGetValue<UrlModel> { type(UrlModel::class.java)  }
                    urlList = url?.urlList ?: emptyList()
                }
            }

        lpparam.hookClass(EmojiBottomSheetDialog::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmojiDownload) return@onAfter

                    // 非 EmojiDetailDialogNew, 直接结束
                    if (!targetClass.isInstance(thisObject)) {
                        return@onAfter
                    }

                    val emojiDialog = thisObject as EmojiDetailDialogNew
                    emojiDialog.window?.contentView?.postDelayedRunning(500) {
                        if (urlList.isEmpty())
                            return@postDelayedRunning

                        it
                            .firstOrNull(TextView::class.java) { v ->
                                "${v.text}".contains("添加表情")
                            }
                            ?.apply {
                                text = "添加表情 (长按保存)"
                                isHapticFeedbackEnabled = false
                                setOnLongClickListener {
                                    SaveEmojiLogic(this@HEmojiDetailDialogNew, context, urlList)
                                    true
                                }
                            }
                    }
                }
            }
    }
}