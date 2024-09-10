package io.github.fplus.core.hook.logic

import android.content.Context
import com.freegang.extension.child
import com.freegang.extension.need
import com.freegang.ktutils.media.KMediaUtils
import com.freegang.ktutils.net.KHttpUtils
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import java.io.File

class SaveCommentNewLogic {
    companion object {
        private val config get() = ConfigV1.get()

        // 保存评论区图片
        fun onSaveCommentImage(hook: BaseHook, context: Context, urlList: List<String>) {
            if (urlList.isEmpty()) {
                hook.showToast(context, "未获取到图片信息")
                return
            }

            val url = urlList.first()
            hook.singleLaunchIO(url) {
                // 默认保存路径: `/外置存储器/Download/Freedom/picture/comment`
                val parentPath = ConfigV1.getFreedomDir(context).child("picture").child("comment").need()

                // 构建保存文件名
                hook.showToast(context, "保存图片, 请稍后..")
                val resultFile = KHttpUtils.download(
                    sourceUrl = url,
                    file = File(parentPath, "${System.currentTimeMillis() / 1000}.png"),
                )
                if (resultFile != null) {
                    hook.showToast(context, "保存成功!")
                    KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)
                    if (config.vibrate) hook.vibrate(context, 5L)
                } else {
                    hook.showToast(context, "保存失败!")
                }
            }
        }

        // 保存评论区视频
        fun onSaveCommentVideo(hook: BaseHook, context: Context, urlList: List<String>) {
            if (urlList.isEmpty()) {
                hook.showToast(context, "未获取到视频信息")
                return
            }

            val url = urlList.first()
            hook.singleLaunchIO(url) {
                // 默认保存路径: `/外置存储器/Download/Freedom/video/comment`
                val parentPath = ConfigV1.getFreedomDir(context).child("video").child("comment").need()

                // 构建保存文件名
                hook.showToast(context, "保存视频, 请稍后..")
                val resultFile = KHttpUtils.download(
                    sourceUrl = urlList.first(),
                    file = File(parentPath, "${System.currentTimeMillis() / 1000}.mp4"),
                )
                if (resultFile != null) {
                    hook.showToast(context, "保存成功!")
                    KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)
                    if (config.vibrate) hook.vibrate(context, 5L)
                } else {
                    hook.showToast(context, "保存失败!")
                }
            }
        }
    }
}