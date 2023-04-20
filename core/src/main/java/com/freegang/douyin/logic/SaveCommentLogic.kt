package com.freegang.douyin.logic

import android.content.Context
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.xpler.utils.io.KFileUtils.child
import com.freegang.xpler.utils.io.KFileUtils.need
import com.freegang.xpler.utils.net.KHttpUtils
import com.ss.android.ugc.aweme.feed.model.Aweme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

//保存评论区(图片/视频)逻辑
class SaveCommentLogic(
    private val hook: BaseHook<*>,
    private val context: Context,
    private val aweme: Aweme?,
) {

    init {
        if (aweme != null) {
            if (aweme.images?.isEmpty() == true) {
                onSaveCommentImage(aweme.images.first().urlList)
            } else {
                onSaveCommentVideo(aweme.video.h264PlayAddr.urlList)
            }
        } else {
            hook.showToast(context, "未获取到基本信息")
        }
    }

    // 保存评论区图片
    private fun onSaveCommentImage(urlList: List<String>) {
        hook.launch {
            //默认保存路径: `/外置存储器/DCIM/Freedom/emoji`
            val parentPath = Config.getFreedomDir(context)
                .child("emoji")
                .need()

            //构建保存文件名
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.gif")
            withContext(Dispatchers.IO) {
                KHttpUtils.download(urlList.first(), FileOutputStream(file)) { real, total, isInterrupt ->
                    if (real >= total) {
                        hook.showToast(context, "保存成功!")
                        hook.vibrate(context, 100L)
                    }
                    if (isInterrupt) {
                        hook.showToast(context, "保存失败!")
                    }
                }
            }
        }
    }

    // 保存评论区视频
    private fun onSaveCommentVideo(urlList: List<String>) {
        hook.launch {
            //默认保存路径: `/外置存储器/DCIM/Freedom/emoji`
            val parentPath = Config.getFreedomDir(context).child("video").child("comment").need()

            //构建保存文件名
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.mp4")
            withContext(Dispatchers.IO) {
                KHttpUtils.download(urlList.first(), FileOutputStream(file)) { real, total, isInterrupt ->
                    if (real >= total) {
                        hook.showToast(context, "保存成功!")
                        hook.vibrate(context, 100L)
                    }
                    if (isInterrupt) {
                        hook.showToast(context, "保存失败!")
                    }
                }
            }
        }
    }
}