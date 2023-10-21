package com.freegang.hook

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.hook.logic.SaveEmojiLogic
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.collection.ifNotEmpty
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldFirst
import com.freegang.ktutils.view.findViewsByExact
import com.freegang.ktutils.view.postDelayedRunning
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.emoji.base.BaseEmoji
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import com.ss.android.ugc.aweme.emoji.utils.EmojiApi
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HEmojiDetailDialogNew(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmojiDetailDialogNew>(lpparam) {
    companion object {
        const val TAG = "HEmojiDetailDialogNew"
    }

    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()
    private var popUrlList: List<String> = emptyList()

    override fun onInit() {
        // 该类是 retrofit2 代理类的Hook, 直接通过实例获取class进行hook
        if (DexkitBuilder.emojiApiProxyClazz != null) {
            val emojiApiField = DexkitBuilder.emojiApiProxyClazz?.fieldFirst(type = EmojiApi::class.java)
            val emojiApi = emojiApiField?.get(null)
            if (emojiApi != null) {
                lpparam.hookClass(emojiApi::class.java)
                    .methodAll {
                        onAfter {
                            runCatching {
                                if (method.name.contains("getSimilarEmoji")) {
                                    urlList = mutableListOf(args[7] as String)
                                }
                            }.onFailure {
                                KLogCat.tagE(TAG, it)
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
                        findViewsByExact(TextView::class.java) {
                            "$text".contains("添加表情")
                        }.ifNotEmpty {
                            first().apply {
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
            }

        // 表情弹层
        DexkitBuilder.emojiPopupWindowClazz?.runCatching {
            lpparam.hookClass(this)
                .methodAll {
                    onAfter {
                        if (!config.isEmoji) return@onAfter
                        if (argsOrEmpty.isNotEmpty()) {
                            val emoji = argsOrEmpty[0].asOrNull<BaseEmoji>()
                            popUrlList = popUrlList.ifEmpty {
                                emoji?.detailEmoji?.animateUrl?.urlList
                                    ?: emoji?.detailEmoji?.staticUrl?.urlList ?: emptyList()
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
        }?.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}