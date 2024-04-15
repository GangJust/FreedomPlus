package io.github.fplus.core.hook

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import com.freegang.extension.dip2px
import com.freegang.extension.fieldGets
import com.freegang.extension.firstJsonObject
import com.freegang.extension.getJSONArrayOrDefault
import com.freegang.extension.getStringOrDefault
import com.freegang.extension.parseJSON
import com.freegang.ktutils.text.KTextUtils
import com.ss.android.ugc.aweme.comment.model.Comment
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.hook.logic.SaveAudioLogic
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisView
import io.github.xpler.core.wrapper.CallMethods

@Deprecated("暂存区, 评论区语音保存")
class HCommentAudioView : BaseHook<Any>(), CallMethods {
    companion object {
        const val TAG = "HCommentAudioView"
    }

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.comment.audiocomment.ui.CommentAudioView")
    }

    override fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam) {}

    override fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (argsOrEmpty.size != 4) {
                return
            }

            val value = args[1].fieldGets().filterNotNull().firstOrNull() ?: return
            val gets = value.fieldGets().filter { it?.javaClass?.`package`?.name == "X" }

            var comment: Comment? = null
            for (get in gets) {
                if (get == null) continue
                val fields = get.fieldGets(type = Comment::class.java)
                if (fields.isEmpty()) continue
                comment = fields.firstOrNull() as? Comment
            }
            if (comment == null) {
                showToast(thisView.context, "未获取到评论内容")
                return
            }

            val content = comment.commentAudio.content
            val contentJson = content.parseJSON()
            val mainUrl = contentJson
                .getJSONArrayOrDefault("video_list")
                .firstJsonObject()
                .getStringOrDefault("main_url")

            if (mainUrl.isEmpty()) {
                showToast(thisView.context, "未获取到语音内容")
                return
            }

            val frameLayout = thisView as FrameLayout
            val linearLayout = frameLayout.children.first { it is LinearLayout } as LinearLayout

            val find = linearLayout.children.find { "${it.contentDescription}" == "FAudioSave" }
            if (find != null) linearLayout.removeView(find)
            ImageView(thisView.context).apply {
                setImageDrawable(KtXposedHelpers.getDrawable(R.mipmap.ic_bubbles))
                contentDescription = "FAudioSave"
                layoutParams = ViewGroup.LayoutParams(context.dip2px(16f), context.dip2px(12f))
                setOnClickListener {
                    SaveAudioLogic(
                        hook = this@HCommentAudioView,
                        context = it.context,
                        url = mainUrl,
                        filename = KTextUtils.get(comment.text, "${System.currentTimeMillis() / 1000}"),
                    )
                }
                linearLayout.addView(this)
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}