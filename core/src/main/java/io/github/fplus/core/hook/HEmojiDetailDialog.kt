package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import com.freegang.extension.contentView
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.firstOrNull
import com.ss.android.ugc.aweme.base.model.UrlModel
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import com.ss.android.ugc.aweme.emoji.views.EmojiDetailDialog
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.SaveEmojiLogic
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.CallMethods
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookClass
import io.github.xpler.core.lparam
import io.github.xpler.core.proxy.MethodParam
import kotlinx.coroutines.delay

class HEmojiDetailDialog : BaseHook(), CallMethods {
    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()

    override fun setTargetClass(): Class<*> {
        return EmojiDetailDialog::class.java
    }

    @SuppressLint("SetTextI18n")
    override fun onInit() {
        lparam.hookClass(EmojiBottomSheetDialog::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmojiDownload) return@onAfter
                    if (!targetClass.isInstance(thisObject)) return@onAfter  // 非 EmojiDetailDialog, 直接结束

                    singleLaunchMain {
                        delay(500L)

                        val emojiDialog = thisObject as EmojiDetailDialog
                        if (urlList.isEmpty())
                            return@singleLaunchMain

                        val contentView = emojiDialog.window?.contentView ?: return@singleLaunchMain
                        contentView
                            .firstOrNull(TextView::class.java) {
                                "${it.text}".contains("添加")
                            }
                            ?.apply {
                                text = "添加表情 (长按保存)"
                                isHapticFeedbackEnabled = false
                                setOnLongClickListener {
                                    SaveEmojiLogic(this@HEmojiDetailDialog, it.context, urlList)
                                    true
                                }
                            }
                    }
                }
            }
    }

    override fun callOnBeforeMethods(params: MethodParam) {

    }

    override fun callOnAfterMethods(params: MethodParam) {
        hookBlockRunning(params) {
            if (!config.isEmojiDownload) return
            if (urlList.isNotEmpty()) return

            val urlModel = thisObject?.findFieldGetValue<UrlModel> { type(UrlModel::class.java) }
            urlList = urlModel?.urlList ?: emptyList()
        }.onFailure {
            XplerLog.e(it)
        }
    }
}