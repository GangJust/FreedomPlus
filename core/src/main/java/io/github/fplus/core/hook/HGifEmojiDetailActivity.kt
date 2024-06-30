package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.freegang.extension.contentView
import com.freegang.extension.firstOrNull
import com.freegang.extension.idName
import com.ss.android.ugc.aweme.comment.ui.GifEmojiDetailActivity
import com.ss.android.ugc.aweme.emoji.model.Emoji
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.hook.logic.SaveEmojiLogic
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisActivity
import kotlinx.coroutines.delay

class HGifEmojiDetailActivity : BaseHook() {
    companion object {
        const val TAG = "HGifEmojiDetailActivity"
    }

    private val config get() = ConfigV1.get()

    private var urlList: List<String> = emptyList()

    override fun setTargetClass(): Class<*> {
        return GifEmojiDetailActivity::class.java
    }

    @OnBefore("onCreate")
    fun onCreate(params: XC_MethodHook.MethodHookParam, bundle: Bundle?) {
        hookBlockRunning(params) {
            if (!config.isEmojiDownload)
                return
            val gifEmoji = thisActivity.intent.getSerializableExtra("gif_emoji") as Emoji? ?: return

            val animateUrl = gifEmoji.animateUrl
            urlList = animateUrl?.urlList ?: emptyList()

            if (urlList.isEmpty())
                return

            rebuildView(thisActivity as GifEmojiDetailActivity)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // 重构布局
    @SuppressLint("SetTextI18n")
    private fun rebuildView(activity: GifEmojiDetailActivity) {
        singleLaunchMain {
            delay(200L)

            activity.contentView
                .firstOrNull(TextView::class.java) {
                    it.idName.contains("text_right")
                }
                ?.apply {
                    isVisible = true
                    text = "保存"
                    setOnClickListener {
                        SaveEmojiLogic(
                            hook = this@HGifEmojiDetailActivity,
                            context = context,
                            urlList = urlList,
                        )
                    }
                }
        }
    }
}