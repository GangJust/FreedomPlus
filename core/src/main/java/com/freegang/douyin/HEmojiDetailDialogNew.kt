package com.freegang.douyin

import android.os.Bundle
import android.widget.TextView
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.SaveEmojiLogic
import com.freegang.xpler.utils.app.KActivityUtils.contentView
import com.freegang.xpler.utils.view.KViewUtils
import com.freegang.xpler.xp.findFieldByType
import com.freegang.xpler.xp.getObjectField
import com.freegang.xpler.xp.hookClass
import com.freegang.xpler.xp.hookMethodAll
import com.ss.android.ugc.aweme.emoji.model.Emoji
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HEmojiDetailDialogNew(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmojiDetailDialogNew>(lpparam) {
    private val config get() = Config.get()
    private var urlList: List<String> = listOf()

    override fun onInit() {
        targetClazz.hookMethodAll {
            onAfter {
                if (!config.isEmoji) return@onAfter
                if (urlList.isNotEmpty()) return@onAfter

                if (args.isEmpty()) return@onAfter
                val first = args.first()
                val emojiList = first.findFieldByType(Emoji::class.java)
                if (emojiList.isEmpty()) return@onAfter
                val firstEmoji = emojiList.first().get(first) ?: return@onAfter

                urlList = firstEmoji.getObjectField<Any>("animateUrl")?.getObjectField<List<String>>("urlList") ?: listOf()
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
                delay(200L)

                //重新构建当前视图
                val emojiDialog = thisObject as EmojiDetailDialogNew

                val contentView = emojiDialog.window?.contentView ?: return@launch
                val views = KViewUtils.findViewsExact(contentView, TextView::class.java) { it.text.contains("添加") }
                if (views.isEmpty()) return@launch
                views.first().apply {
                    text = "添加表情 (长按保存)"
                    setOnLongClickListener {
                        SaveEmojiLogic(this@HEmojiDetailDialogNew, it.context, urlList)
                        true
                    }
                }

                //
                ////重构添加表情按钮, 增加保存表情按钮
                //val parent = addEmoji.parent as ViewGroup
                //val rootParent = parent.parent as ViewGroup
                //rootParent.removeView(parent) //移除旧布局
                //
                //val view = KResourceUtils.inflateView<LinearLayout>(parent.context, R.layout.hook_add_save_emoji_layout)
                //val binding = HookAddSaveEmojiLayoutBinding.bind(view)
                //binding.addEmoji.apply {
                //    setTextColor(Color.WHITE)
                //    background = KResourceUtils.getDrawable(R.drawable.hook_add_button)
                //    setOnClickListener {
                //        addEmoji.performClick()
                //    }
                //}
                //binding.saveEmoji.apply {
                //    setTextColor(Color.WHITE)
                //    background = KResourceUtils.getDrawable(R.drawable.hook_save_button)
                //    setOnClickListener { v ->
                //        SaveLogic(this@HEmojiDetailDialogNew, v.context, urlList)
                //    }
                //}
                //
                //rootParent.addView(view, 1) //重新添加新布局
            }
        }
    }
}