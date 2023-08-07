package com.freegang.hook

import android.os.Bundle
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.SaveEmojiLogic
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.view.KViewUtils
import com.freegang.xpler.core.hook
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.hookConstructorsAll
import com.ss.android.ugc.aweme.emoji.model.Emoji
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay
import java.lang.reflect.Modifier

class HEmojiDetailDialogNew(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmojiDetailDialogNew>(lpparam) {
    private val config get() = ConfigV1.get()
    private var urlList: List<String> = emptyList()

    override fun onInit() {
        targetClazz.hookConstructorsAll {
            for (method in DouYinMain.emojiMethods) {
                if (Modifier.isAbstract(method.modifiers)) continue
                method.hook {
                    onBefore {
                        val emoji = args[0] as Emoji? ?: return@onBefore
                        urlList = emoji.animateUrl?.urlList ?: emoji.staticUrl?.urlList ?: emptyList()
                    }
                    //onUnhook { _, _ -> }
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
    }

    //重构表情布局
    private fun rebuildView(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            if (!targetClazz.isInstance(thisObject)) return  //非 EmojiDetailDialogNew, 直接结束

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