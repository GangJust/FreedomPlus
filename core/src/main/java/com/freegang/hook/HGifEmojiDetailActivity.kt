package com.freegang.hook

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.SaveEmojiLogic
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.view.findViewsByExact
import com.freegang.ktutils.view.idName
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.getObjectField
import com.freegang.xpler.core.thisActivity
import com.ss.android.ugc.aweme.comment.ui.GifEmojiDetailActivity
import com.ss.android.ugc.aweme.emoji.model.Emoji
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HGifEmojiDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<GifEmojiDetailActivity>(lpparam) {
    private val config get() = ConfigV1.get()
    private var urlList: List<String> = emptyList()

    @OnBefore("onCreate")
    fun onCreate(params: XC_MethodHook.MethodHookParam, bundle: Bundle?) {
        hookBlock(params) {
            if (!config.isEmoji) return
            val gifEmoji = thisActivity.intent.getSerializableExtra("gif_emoji") as Emoji? ?: return
            val animateUrl = gifEmoji.getObjectField<Any>("animateUrl")
            urlList = animateUrl?.getObjectField<List<String>>("urlList") ?: emptyList()
            if (urlList.isEmpty()) return

            rebuildView(thisActivity as GifEmojiDetailActivity)
        }
    }

    //重构布局
    private fun rebuildView(activity: GifEmojiDetailActivity) {
        launch {
            delay(200L)
            val views = activity.contentView.findViewsByExact(TextView::class.java) {
                it.idName.contains("text_right")
            }
            if (views.isEmpty()) return@launch
            views.first().apply {
                isVisible = true
                text = "保存"
                setOnClickListener {
                    SaveEmojiLogic(this@HGifEmojiDetailActivity, activity, urlList)
                }
            }
        }
    }
}