package io.github.fplus.core.hook.logic

import android.content.Context
import com.freegang.extension.child
import com.freegang.extension.need
import com.freegang.ktutils.media.KMediaUtils
import com.freegang.ktutils.net.KHttpUtils
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.log.XplerLog
import java.io.File
import java.io.FileOutputStream

// 保存表情逻辑
class SaveEmojiLogic(
    private val hook: BaseHook,
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
            XplerLog.e(it)
            hook.showToast(context, "表情内容获取失败")
        }
    }

    private fun onSaveEmoji(urlList: List<String>) {
        hook.singleLaunchIO("SaveEmoji") {
            // 默认保存路径: `/外置存储器/Download/Freedom/emoji`
            val parentPath = ConfigV1.getFreedomDir(context).child("emoji").need()

            // 构建保存文件名
            hook.showToast(context, "保存表情, 请稍后..")
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.gif")
            val result = KHttpUtils.download(urlList.random(), FileOutputStream(file))
            if (result) {
                hook.showToast(context, "保存成功!")
                KMediaUtils.notifyMediaUpdate(context, file.absolutePath)
                if (config.vibrate) hook.vibrate(context, 5L)
            } else {
                hook.showToast(context, "保存失败!")
            }
        }
    }
}