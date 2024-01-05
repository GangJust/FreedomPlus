package io.github.fplus.core.hook

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.reflect.fieldFirst
import com.freegang.ktutils.view.firstOrNull
import com.freegang.ktutils.view.postDelayedRunning
import com.ss.android.ugc.aweme.emoji.base.BaseEmoji
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import com.ss.android.ugc.aweme.emoji.utils.EmojiApi
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.hook.logic.SaveEmojiLogic
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookClass

class HEmojiDetailDialogNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<EmojiDetailDialogNew>(lpparam) {
    companion object {
        const val TAG = "HEmojiDetailDialogNew"
    }

    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()
    private var popUrlList: List<String> = emptyList()

    override fun onInit() {
        // 该类是 retrofit2 代理类的Hook, 直接通过实例获取class进行hook
        DexkitBuilder.emojiApiProxyClazz?.let { it ->
            val emojiApiField = it.fieldFirst(type = EmojiApi::class.java)
            val emojiApi = emojiApiField?.get(null)
            if (emojiApi != null) {
                lpparam.hookClass(emojiApi::class.java)
                    .method(
                        "getSimilarEmoji",
                        String::class.java,
                        Int::class.java,
                        String::class.java,
                        Long::class.java,
                        Long::class.java,
                        Long::class.java,
                        String::class.java,
                        String::class.java,
                    ) {
                        onAfter {
                            runCatching {
                                urlList = mutableListOf(args[7] as String)
                            }.onFailure {
                                urlList = emptyList()
                            }
                        }
                    }
            }
        }

        lpparam.hookClass(EmojiBottomSheetDialog::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmoji) return@onAfter

                    // 非 EmojiDetailDialogNew, 直接结束
                    if (!targetClazz.isInstance(thisObject)) {
                        return@onAfter
                    }

                    val emojiDialog = thisObject as EmojiDetailDialogNew
                    emojiDialog.window?.contentView?.postDelayedRunning(500) {
                        if (urlList.isEmpty()) {
                            return@postDelayedRunning
                        }
                        this.firstOrNull<TextView> {
                            "${it.text}".contains("添加表情")
                        }?.apply {
                            text = "添加表情 (长按保存)"
                            isHapticFeedbackEnabled = false
                            setOnLongClickListener {
                                SaveEmojiLogic(this@HEmojiDetailDialogNew, it.context, urlList)
                                true
                            }
                        }
                    }
                }
            }

        // 表情弹层
        DexkitBuilder.emojiPopupWindowClazz?.let { it ->
            lpparam.hookClass(it)
                .methodAll {
                    onAfter {
                        if (!config.isEmoji) return@onAfter
                        if (argsOrEmpty.isNotEmpty()) {
                            val emoji = argsOrEmpty[0].asOrNull<BaseEmoji>()
                            popUrlList = popUrlList.ifEmpty {
                                emoji?.detailEmoji?.animateUrl?.urlList
                                    ?: emoji?.detailEmoji?.staticUrl?.urlList
                                    ?: emptyList()
                            }
                        }

                        if (popUrlList.isEmpty()) return@onAfter
                        val view = result?.asOrNull<View>() ?: return@onAfter
                        if (view is TextView) {
                            if (view.text == "添加到表情") {
                                view.text = "添加到表情 (长按保存)"
                                view.isLongClickable = true
                                view.isHapticFeedbackEnabled = false
                                view.setOnLongClickListener {
                                    SaveEmojiLogic(this@HEmojiDetailDialogNew, it.context, popUrlList)
                                    true
                                }
                            }
                        }
                    }
                }
        }
    }
}