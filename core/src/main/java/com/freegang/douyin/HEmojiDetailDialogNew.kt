package com.freegang.douyin

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.SaveLogic
import com.freegang.xpler.R
import com.freegang.xpler.databinding.HookAddSaveEmojiLayoutBinding
import com.freegang.xpler.utils.other.KResourceUtils
import com.freegang.xpler.xp.findFieldByType
import com.freegang.xpler.xp.getObjectField
import com.freegang.xpler.xp.hookClass
import com.ss.android.ugc.aweme.emoji.model.Emoji
import com.ss.android.ugc.aweme.emoji.similaremoji.EmojiDetailDialogNew
import com.ss.android.ugc.aweme.emoji.store.view.EmojiBottomSheetDialog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HEmojiDetailDialogNew(
    lpparam: XC_LoadPackage.LoadPackageParam,
) : BaseHook(lpparam) {
    var urlList: List<String> = listOf()

    override fun onHook() {
        val config = Config.get()

        lpparam.hookClass(EmojiDetailDialogNew::class.java)
            .constructorsAll {
                onAfter {
                    if (!config.isEmoji) return@onAfter
                    getEmojiDetail(this)
                }
            }

        lpparam.hookClass(EmojiBottomSheetDialog::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmoji) return@onAfter
                    rebuildEmojiView(this)
                }
            }
    }

    //获取表情内容
    private fun getEmojiDetail(it: XC_MethodHook.MethodHookParam) {
        if (it.args.isEmpty()) return

        val first = it.args.first()
        val emojiList = first.findFieldByType(Emoji::class.java)

        if (emojiList.isEmpty()) return
        val firstEmoji = emojiList.first().get(first) ?: return

        urlList = firstEmoji.getObjectField<Any>("animateUrl")?.getObjectField<List<String>>("urlList") ?: listOf()
    }

    //重构表情布局
    private fun rebuildEmojiView(it: XC_MethodHook.MethodHookParam) {
        launch {
            delay(200L)

            //重新构建当前视图
            val emojiDialog = it.thisObject as EmojiDetailDialogNew
            val contentView: View = emojiDialog.window?.decorView?.findViewById(android.R.id.content) ?: return@launch

            val outViews = ArrayList<View>()
            contentView.findViewsWithText(outViews, "添加表情", View.FIND_VIEWS_WITH_TEXT)
            if (outViews.isEmpty()) return@launch

            val addEmoji = outViews.first()

            //重构添加表情按钮, 增加保存表情按钮
            val parent = addEmoji.parent as ViewGroup
            val rootParent = parent.parent as ViewGroup
            rootParent.removeView(parent) //移除旧布局

            val view = KResourceUtils.inflateView<LinearLayout>(parent.context, R.layout.hook_add_save_emoji_layout)
            val binding = HookAddSaveEmojiLayoutBinding.bind(view)
            binding.addEmoji.apply {
                setTextColor(Color.WHITE)
                background = KResourceUtils.getDrawable(R.drawable.hook_add_button)
                setOnClickListener {
                    addEmoji.performClick()
                }
            }
            binding.saveEmoji.apply {
                setTextColor(Color.WHITE)
                background = KResourceUtils.getDrawable(R.drawable.hook_save_button)
                setOnClickListener { v ->
                    SaveLogic(this@HEmojiDetailDialogNew, v.context, urlList)
                }
            }

            rootParent.addView(view, 1) //重新添加新布局
        }
    }
}