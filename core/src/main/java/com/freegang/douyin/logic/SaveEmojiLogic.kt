package com.freegang.douyin.logic

import android.content.Context
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.KMediaUtils
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.need
import com.freegang.ktutils.net.KHttpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

//保存表情逻辑
class SaveEmojiLogic(
    private val hook: BaseHook<*>,
    private val context: Context,
    private val urlList: List<String>,
) {
    companion object {
        private val config get() = ConfigV1.get()
    }

    init {
        runCatching {
            if (urlList.isEmpty()) {
                hook.showToast(context, "未获取到表情内容")
                return@runCatching
            }
            onSaveEmoji(urlList)
        }.onFailure {
            hook.showToast(context, "未获取到表情内容")
        }
    }

    private fun onSaveEmoji(urlList: List<String>) {
        hook.launch {
            //默认保存路径: `/外置存储器/DCIM/Freedom/emoji`
            val parentPath = ConfigV1.getFreedomDir(context).child("emoji").need()

            //构建保存文件名
            hook.showToast(context, "保存表情, 请稍后..")
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.gif")
            withContext(Dispatchers.IO) {
                KHttpUtils.download(urlList.first(), FileOutputStream(file)) { real, total, isInterrupt ->
                    if (real >= total) {
                        hook.showToast(context, "保存成功!")
                        KMediaUtils.notifyGallery(context, file.absolutePath)
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