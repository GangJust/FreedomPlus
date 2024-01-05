package io.github.fplus.core.hook.logic

import android.content.Context
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.need
import com.freegang.ktutils.media.KMediaUtils
import com.freegang.ktutils.net.KHttpUtils
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SaveAudioLogic(
    private val hook: BaseHook<*>,
    private val context: Context,
    private val url: String,
    private val filename: String,
) {
    companion object {
        private val config get() = ConfigV1.get()
    }

    init {
        hook.launch {
            // 默认保存路径: `/外置存储器/Download/Freedom/audio`
            val parentPath = ConfigV1.getFreedomDir(context).child("audio").need()

            // 构建保存文件名
            hook.showToast(context, "保存语音, 请稍后..")
            val file = File(parentPath, "${filename}.mp3")
            withContext(Dispatchers.IO) {
                val result = KHttpUtils.download(url, FileOutputStream(file))
                if (result) {
                    hook.showToast(context, "保存成功!")
                    KMediaUtils.notifyMediaUpdate(context, file.absolutePath)
                    if (config.isVibrate) hook.vibrate(context, 5L)
                } else {
                    hook.showToast(context, "保存失败!")
                }
            }
        }
    }
}