package io.github.fplus.core.hook

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.collection.ifNotEmpty
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.view.findViewsByExact
import com.freegang.ktutils.view.idName
import com.ss.android.ugc.aweme.comment.ui.GifEmojiDetailActivity
import com.ss.android.ugc.aweme.emoji.model.Emoji
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.SaveEmojiLogic
import io.github.xpler.core.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity
import kotlinx.coroutines.delay

class HGifEmojiDetailActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<GifEmojiDetailActivity>(lpparam) {
    companion object {
        const val TAG = "HGifEmojiDetailActivity"
    }

    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()

    @OnBefore("onCreate")
    fun onCreate(params: XC_MethodHook.MethodHookParam, bundle: Bundle?) {
        hookBlockRunning(params) {
            if (!config.isEmoji) return
            val gifEmoji = thisActivity.intent.getSerializableExtra("gif_emoji") as Emoji? ?: return

            // deprecated
            // val animateUrl = gifEmoji.getObjectField<Any>("animateUrl")
            // urlList = animateUrl?.getObjectField<List<String>>("urlList") ?: emptyList()

            // new
            val animateUrl = gifEmoji.fieldGetFirst("animateUrl")
            urlList = animateUrl?.fieldGetFirst("urlList")?.asOrNull<List<String>>() ?: emptyList()
            if (urlList.isEmpty()) return

            rebuildView(thisActivity as GifEmojiDetailActivity)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    // 重构布局
    private fun rebuildView(activity: GifEmojiDetailActivity) {
        launch {
            delay(200L)

            activity.contentView.findViewsByExact(TextView::class.java) {
                idName.contains("text_right")
            }.ifNotEmpty {
                first().apply {
                    isVisible = true
                    text = "保存"
                    setOnClickListener {
                        SaveEmojiLogic(this@HGifEmojiDetailActivity, activity, urlList)
                    }
                }
            }
        }
    }
}