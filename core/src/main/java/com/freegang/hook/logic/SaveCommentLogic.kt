package com.freegang.hook.logic

import android.content.Context
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.media.KMediaUtils
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.need
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.net.KHttpUtils
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

    companion object {
        private val config get() = ConfigV1.get()
    }

    init {
        runCatching {
            if (aweme != null) {
                val imageUrl = getImagesUrlList(aweme)
                val videoUrl = getVideoUrlList(aweme)
                if (imageUrl.isNotEmpty()) {
                    onSaveCommentImage(imageUrl)
                } else if (videoUrl.isNotEmpty()) {
                    onSaveCommentVideo(videoUrl)
                } else {
                    hook.showToast(context, "未获取到基本信息")
                }
            } else {
                hook.showToast(context, "未获取到基本信息")
            }
        }.onFailure {
            KLogCat.e(it)
            hook.showToast(context, "基本信息获取失败")
        }
    }

    private fun getVideoUrlList(aweme: Aweme): List<String> {
        val video = aweme.video ?: return emptyList()
        return video.playAddrH265?.urlList ?: video.h264PlayAddr?.urlList ?: emptyList()
    }

    private fun getImagesUrlList(aweme: Aweme): List<String> {
        val image = aweme.images ?: return emptyList()
        if (image.isEmpty()) return emptyList()
        return image.firstOrNull()?.urlList ?: return emptyList()
    }

    // 保存评论区图片
    private fun onSaveCommentImage(urlList: List<String>) {
        hook.launch {
            //默认保存路径: `/外置存储器/Download/Freedom/picture/comment`
            val parentPath = ConfigV1.getFreedomDir(context).child("picture").child("comment").need()

            //构建保存文件名
            hook.showToast(context, "保存图片, 请稍后..")
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.png")
            withContext(Dispatchers.IO) {
                KHttpUtils.download(urlList.first(), FileOutputStream(file)) { real, total, isInterrupt ->
                    if (real >= total) {
                        hook.showToast(context, "保存成功!")
                        KMediaUtils.notifyMediaUpdate(context, file.absolutePath)
                        if (config.isVibrate) hook.vibrate(context, 5L)
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
            //默认保存路径: `/外置存储器/Download/Freedom/video/comment`
            val parentPath = ConfigV1.getFreedomDir(context).child("video").child("comment").need()

            //构建保存文件名
            hook.showToast(context, "保存视频, 请稍后..")
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.mp4")
            withContext(Dispatchers.IO) {
                KHttpUtils.download(urlList.first(), FileOutputStream(file)) { real, total, isInterrupt ->
                    if (real >= total) {
                        hook.showToast(context, "保存成功!")
                        KMediaUtils.notifyMediaUpdate(context, file.absolutePath)
                        if (config.isVibrate) hook.vibrate(context, 5L)
                    }
                    if (isInterrupt) {
                        hook.showToast(context, "保存失败!")
                    }
                }
            }
        }
    }
}