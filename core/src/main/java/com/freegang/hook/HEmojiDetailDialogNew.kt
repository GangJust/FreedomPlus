package com.freegang.hook

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.SaveEmojiLogic
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.reflect.fields
import com.freegang.ktutils.view.KViewUtils
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.emoji.base.BaseEmoji
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import com.ss.android.ugc.aweme.emoji.utils.EmojiApi
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HEmojiDetailDialogNew(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmojiDetailDialogNew>(lpparam) {
    private val config get() = ConfigV1.get()
    private var urlList: List<String> = emptyList()
    private var popUrlList: List<String> = emptyList()

    override fun onInit() {
        // 该类是 retrofit2 代理类的Hook, 直接通过实例获取class进行hook
        if (DouYinMain.emojiApiProxyClazz != null) {
            val emojiApiField = DouYinMain.emojiApiProxyClazz?.fields(type = EmojiApi::class.java)?.firstOrNull()
            val emojiApi = emojiApiField?.get(null)
            if (emojiApi != null) {
                lpparam.hookClass(emojiApi::class.java)
                    .methodAll {
                        onAfter {
                            if (method.name.contains("getSimilarEmoji")) {
                                urlList = mutableListOf(args[7] as String)
                            }
                        }
                    }
            }
        }

        lpparam.hookClass(EmojiBottomSheetDialog::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmoji) return@onAfter
                    rebuildView(this)
                }
            }

        // 表情弹层
        if (DouYinMain.emojiPopupWindowClazz != null) {
            lpparam.hookClass(DouYinMain.emojiPopupWindowClazz!!)
                .methodAll {
                    onAfter {
                        if (argsOrEmpty.isNotEmpty()) {
                            val emoji = argsOrEmpty[0].asOrNull<BaseEmoji>()
                            popUrlList = emoji?.detailEmoji?.animateUrl?.urlList
                                ?: emoji?.detailEmoji?.staticUrl?.urlList ?: emptyList()
                        }

                        if (popUrlList.isEmpty()) return@onAfter

                        val view = result.asOrNull<View>() ?: return@onAfter
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

    // 重构表情布局
    private fun rebuildView(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            if (!targetClazz.isInstance(thisObject)) return  // 非 EmojiDetailDialogNew, 直接结束

            launch {
                delay(500L)

                val emojiDialog = thisObject as EmojiDetailDialogNew
                if (urlList.isEmpty()) return@launch

                val contentView = emojiDialog.window?.contentView ?: return@launch
                val views = KViewUtils.findViewsExact(contentView, TextView::class.java) { it.text.contains("添加") }
                if (views.isEmpty()) return@launch
                views.first().apply {
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