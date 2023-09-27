package com.freegang.hook

import android.os.Bundle
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.SaveEmojiLogic
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.view.KViewUtils
import com.freegang.xpler.core.getObjectField
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.hookMethodAll
import com.ss.android.ugc.aweme.base.model.UrlModel
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import com.ss.android.ugc.aweme.emoji.views.EmojiDetailDialog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HEmojiDetailDialog(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmojiDetailDialog>(lpparam) {
    private val config get() = ConfigV1.get()
    private var urlList: List<String> = emptyList()

    override fun onInit() {
        targetClazz.hookMethodAll {
            onAfter {
                if (!config.isEmoji) return@onAfter
                if (urlList.isNotEmpty()) return@onAfter

                val urlModel = thisObject.fieldGetFirst(type = UrlModel::class.java)
                urlList = urlModel?.getObjectField<List<String>>("urlList") ?: listOf()
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

    private fun rebuildView(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            if (!targetClazz.isInstance(thisObject)) return  // 非 EmojiDetailDialog, 直接结束
            launch {
                delay(500L)

                val emojiDialog = thisObject as EmojiDetailDialog
                if (urlList.isEmpty()) return@launch

                val contentView = emojiDialog.window?.contentView ?: return@launch
                val views = KViewUtils.findViewsExact(contentView, TextView::class.java) { it.text.contains("添加") }
                if (views.isEmpty()) return@launch
                views.first().apply {
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