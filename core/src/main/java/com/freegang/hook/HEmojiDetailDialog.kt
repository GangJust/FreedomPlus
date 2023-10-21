package com.freegang.hook

import android.os.Bundle
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.SaveEmojiLogic
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.collection.ifNotEmpty
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.view.KViewUtils
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.base.model.UrlModel
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import com.ss.android.ugc.aweme.emoji.views.EmojiDetailDialog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HEmojiDetailDialog(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmojiDetailDialog>(lpparam), CallMethods {
    companion object {
        const val TAG = "HEmojiDetailDialog"
    }

    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()

    override fun onInit() {
        lpparam.hookClass(EmojiBottomSheetDialog::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmoji) return@onAfter
                    if (!targetClazz.isInstance(thisObject)) return@onAfter  // 非 EmojiDetailDialog, 直接结束

                    launch {
                        delay(500L)

                        val emojiDialog = thisObject as EmojiDetailDialog
                        if (urlList.isEmpty()) return@launch

                        val contentView = emojiDialog.window?.contentView ?: return@launch
                        KViewUtils.findViewsExact(contentView, TextView::class.java) {
                            it.text.contains("添加")
                        }.ifNotEmpty {
                            first().apply {
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
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (!config.isEmoji) return
            if (urlList.isNotEmpty()) return

            val urlModel = thisObject.fieldGetFirst(type = UrlModel::class.java)
            // deprecated
            //urlList = urlModel?.getObjectField<List<String>>("urlList") ?: listOf()

            // new
            urlList = urlModel?.fieldGetFirst("urlList")?.asOrNull<List<String>>() ?: listOf()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}