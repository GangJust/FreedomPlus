package com.freegang.douyin

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import com.freegang.base.BaseHook
import com.freegang.douyin.logic.SaveAudioLogic
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.json.firstJsonObject
import com.freegang.ktutils.json.getJSONArrayOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.json.parseJSON
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.text.KTextUtils
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.thisView
import com.ss.android.ugc.aweme.comment.model.Comment
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HCommentAudioView(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam) {
    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.comment.audiocomment.ui.CommentAudioView")
    }

    override fun onInit() {
        lpparam.hookClass(targetClazz)
            .methodAll {
                onAfter {
                    val result = runCatching {
                        if (argsOrEmpty.size != 4) return@onAfter
                        val value = args[1].fieldGets().filterNotNull().firstOrNull() ?: return@onAfter
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
                            return@onAfter
                        }

                        val content = comment.commentAudio.content
                        val contentJson = content.parseJSON()
                        val mainUrl = contentJson
                            .getJSONArrayOrDefault("video_list")
                            .firstJsonObject()
                            .getStringOrDefault("main_url")

                        if (mainUrl.isEmpty()) {
                            showToast(thisView.context, "未获取到语音内容")
                            return@onAfter
                        }

                        val frameLayout = thisView as FrameLayout
                        val linearLayout = frameLayout.children.first { it is LinearLayout } as LinearLayout


                        val find = linearLayout.children.find { "${it.contentDescription}" == "FAudioSave" }
                        if (find == null) {
                            ImageView(thisView.context).apply {
                                setImageDrawable(KtXposedHelpers.getDrawable(R.mipmap.ic_bubbles))
                                contentDescription = "FAudioSave"
                                layoutParams = ViewGroup.LayoutParams(context.dip2px(12f), context.dip2px(12f))
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
                        }
                    }
                    if (result.isFailure) {
                        showToast(thisView.context, "保存失败，出现错误!")
                    }
                }
            }
    }
}